package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.ProcessCntx;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;

import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * At this time a type may be null - XmlSchema:2.0.3 library presents xsd:anyType as null.
 * If we meet this case then assign a type as XmlSchemaType instance with processed element schema as constructor argument.
 *
 * @author Alexandr Klimov
 */
public class WSDLProcessor {
    static private final String INPUT_MESSAGE_KEY = "input";
    static private final String OUTPUT_MESSAGE_KEY = "output";
    static private final String PORT_TYPES_PROCESS_OPERATION_LIST = "operation_list";
    static private final String PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP = "operations_to_messages";
    static private final String PORT_TYPES_OPER_TO_PORT_QNAME = "operations_to_porttype_qname_map";

    static private Logger log = LoggerFactory.getLogger(WSDLProcessor.class);

    /**
     * This method performs WSDL processing and returns three following things:<BR/>
     * <UL>
     *     <LI>A set of TypeDescriptor objects contain information about XSD type in a processed WSDL.</LI>
     *     <LI>A map: Map( Operation->Map("input"->Message, "output"->Message) )</LI>
     *     <LI>A map: Map( TypeDescriptor->List(Message,...) )</LI>
     * </UL>
     *
     *
     * @param definition - A processed WSDL definition
     * @return - A set of TypeDescriptor instances that describe used XSD types
     */
    @SuppressWarnings("unchecked")
    static public WSDLProcessingResult processWSDL(Definition definition){
        if(definition==null){
            throw new IllegalArgumentException("A WSDL defintion must not be NULL!");
        }
        log.info("\n\tSTART WSDL PROCESSING\n");

        Map<String, Object> processPortTypesRes = processPortTypes(definition);
        MessageProcessingResult messagesProcessingRes = messagesProcessing(definition);
        List<QName> rootWSDLMessageElementQNames = messagesProcessingRes.getRootWSDLMessageElementNames();
        List<QName> rootWSDLMessageTypeQNames = messagesProcessingRes.getRootWSDLMessageTypeNames();
        Map<QName, List<Part>> elemQNameToPart = messagesProcessingRes.getElemQNameToPart();
        Map<QName, List<Part>> typeQNameToPart = messagesProcessingRes.getTypeQNameToPart();

        //XSD schemas obtaining
        XmlSchema[] schemas =  getXmlSchemaCollection(definition);

        //Init base xsd schema in the global cache
        if(XsdBaseSchemaHolder.getSchema() == null){
            for(XmlSchema schema : schemas){
                if(Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())){
                    XsdBaseSchemaHolder.setSchema(schema);
                    break;
                }
            }
        }

        List<XmlSchemaType> xsdRootElemTypes = new LinkedList<>();

        Map<QName, XmlSchemaType> xsdTypeMap = new HashMap<>(); //<-- is necessary for resolving a base type by its QName
        Map<QName, XmlSchemaElement> xsdElemQNameToElemMap = new HashMap<>();
        Map<QName, XmlSchemaGroup> xsdGroupQNameToGroupMap = new HashMap<>();
        Map<XmlSchemaType, QName> typeToElementQName = new HashMap<>();
        Map<XmlSchemaType, List<Part>> xsdTypeToPart = new HashMap<>();

        //There is collecting all schema non-anonymous type declarations into a map
        //and all root element declarations into a map
        //Xml schema GROUPS are processed here too.
        for(XmlSchema schema : schemas){
            if( log.isDebugEnabled() ){
                log.debug("Processed schema is " + schema.getTargetNamespace());
                log.debug("Collecting schema types into Map( QName->XmlSchemaType ), schema root elements into Map( QName->XmlSchemaElement )" +
                        "and schema groups into Map(QName->XmlSchemaGroup)");
            }

            xsdGroupQNameToGroupMap.putAll(schema.getGroups());

            xsdTypeMap.putAll( schema.getSchemaTypes() );
            Set<QName> qTypeNames = xsdTypeMap.keySet();
            for(QName typeQName : qTypeNames){
                if(typeQNameToPart.containsKey(typeQName)){
                    xsdTypeToPart.put(xsdTypeMap.get(typeQName), typeQNameToPart.get(typeQName));
                }
            }

            xsdElemQNameToElemMap.putAll( schema.getElements() );

        }

        //Resolving XSD type for root descriptors that will be built
        //Transform (messagePart_elementName->message binding to messagePart_typeName->message one.
        if( !rootWSDLMessageElementQNames.isEmpty() ){
            for(XmlSchema schema : schemas){ //<-- for each schema
                for(QName elemQName : rootWSDLMessageElementQNames){ //<--for each WSDL message element
                    XmlSchemaElement element = schema.getElementByName(elemQName);//<--return roots descriptors QName
                    if( element != null){
                        XmlSchemaType schemaType = element.getSchemaType();
                        //At this time a type may be null - XmlSchema:2.0.3 library presents xsd:anyType as null
                        if(schemaType==null){
                            schemaType = XsdBaseSchemaHolder.getAnyType();
                        }
                        typeToElementQName.put(schemaType, elemQName);
                        xsdRootElemTypes.add(schemaType);

                        if(elemQNameToPart.containsKey(elemQName)){
                            xsdTypeToPart.put(schemaType, elemQNameToPart.get(elemQName));
                        }

                    }
                }
            }
        }

        if( !rootWSDLMessageTypeQNames.isEmpty() ){
            for(QName name : rootWSDLMessageTypeQNames){
                XmlSchemaType type = xsdTypeMap.get(name);
                xsdRootElemTypes.add(type);
            }
        }

        //Maps for collection processed types and groups
        Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap = new HashMap<>();
        Map<XmlSchemaGroup, GroupDescriptor> groupToDescriptorMap = new HashMap<>();

        //result sets of processed group and types
        Set<GroupDescriptor> groupDescrSet = new HashSet<>();
        Set<TypeDescriptor> typeDescrSet = new HashSet<>();

        //Processing will start from a root types and types of root descriptors
        //XSD schema types may be linked between them in many ways
        //We will save processed types so as to does not process them twice
        if( !xsdRootElemTypes.isEmpty() ){
            for(XmlSchemaType xsdType : xsdRootElemTypes){

                OwnerInfo rootTypeOwnerPair = null;
                QName elemQName = typeToElementQName.get(xsdType);
                if(elemQName != null){
                    rootTypeOwnerPair = new OwnerInfo();
                    rootTypeOwnerPair.setElemName(elemQName.toString());
                }

                ProcessCntx rootCntx = new ProcessCntx(xsdType,
                                                            null,
                                                                xsdElemQNameToElemMap,
                                                                    xsdTypeMap,
                                                                        xsdGroupQNameToGroupMap,
                                                                            rootTypeOwnerPair,
                                                                                typeToDescriptorMap,
                                                                                    groupToDescriptorMap);
                //processed types and groups will be collected into typeToDescriptorMap and groupToDescriptorMap
                XmlSchemaItemsProcessor.getTypeDescriptor(rootCntx);

            }

            groupDescrSet.addAll( groupToDescriptorMap.values() );
            typeDescrSet.addAll(typeToDescriptorMap.values());
        }

        Map<Part, TypeDescriptor> partToTypeDescr = createPartToTypeDescrMap(typeToDescriptorMap, xsdTypeToPart);
        Map<Operation, Map<String, Message>> operationsToMessages = (Map<Operation, Map<String, Message>>) processPortTypesRes.get(PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP);
        Map<Operation, QName> operationToPortTypeQNameMap = (Map<Operation, QName>) processPortTypesRes.get(PORT_TYPES_OPER_TO_PORT_QNAME);
        Set<WSMethodDescr> wsMethodDescr = buildWSMethodDescription(operationsToMessages, messagesProcessingRes.getMessageToPart(), partToTypeDescr, operationToPortTypeQNameMap);

        DescriptorsContainer descriptorsContainer = new DescriptorsContainer(typeDescrSet, groupDescrSet);
        WSDLProcessingResult wsdlProcRes = new WSDLProcessingResult(descriptorsContainer, operationsToMessages, wsMethodDescr);

        return wsdlProcRes;
    }


    static private Map<Part, TypeDescriptor> createPartToTypeDescrMap(Map<XmlSchemaType, TypeDescriptor> xsdTypeToTdMap,
                                                                            Map<XmlSchemaType, List<Part>> xsdTypeToPartMap){
        Map<Part, TypeDescriptor> resMap = new HashMap<>();

        for(XmlSchemaType xsdType : xsdTypeToPartMap.keySet()){
            List<Part> parts = xsdTypeToPartMap.get(xsdType);
            for(Part part : parts){
                resMap.put(part, xsdTypeToTdMap.get(xsdType));
            }
        }

        return resMap;
    }


    static public Set<WSMethodDescr> buildWSMethodDescription(Map<Operation, Map<String, Message>> operationsToMessages,
                                                              Map<Message, List<Part>> messageToParts,
                                                              Map<Part, TypeDescriptor> partsToTypeDescr,
                                                              Map<Operation, QName> operationToPortTypeQNameMap){
        log.debug("START buildWSMethodDescription");
        Set<WSMethodDescr> resSet = new HashSet<>();

        for(Operation operation : operationsToMessages.keySet()){
            WSMethodDescr tmpWSMethod = new WSMethodDescr();
            tmpWSMethod.setPortTypeQName(operationToPortTypeQNameMap.get(operation));
            tmpWSMethod.setMethodName( operation.getName() );
            log.debug("Method name(Operation name): " + operation.getName() );

            Map<String, Message> messages = operationsToMessages.get(operation);
            Message inputMsg = messages.get(INPUT_MESSAGE_KEY);
            Message outputMsg = messages.get(OUTPUT_MESSAGE_KEY);

            //Build input message parts descriptors
            List<Part> inputParts = messageToParts.get(inputMsg);
            for(Part inMsgPart : inputParts){
                TypeDescriptor td = partsToTypeDescr.get(inMsgPart);
                WSMethodDescr.MessagePartDescr partDescr =
                                                new WSMethodDescr.MessagePartDescr(inMsgPart.getName(),
                                                                                    (inMsgPart.getElementName()!= null),
                                                                                    inMsgPart.getElementName(),
                                                                                    td);
                tmpWSMethod.getInputMessage().add(partDescr);
            }
            
            //Build output message parts descriptors
            List<Part> outputParts = messageToParts.get(outputMsg);
            for(Part outMsgPart : inputParts){
                TypeDescriptor td = partsToTypeDescr.get(outMsgPart);
                WSMethodDescr.MessagePartDescr partDescr =
                        new WSMethodDescr.MessagePartDescr(outMsgPart.getName(),
                                (outMsgPart.getElementName()!= null),
                                outMsgPart.getElementName(),
                                td);
                tmpWSMethod.getOutputMessage().add(partDescr);
            }

            log.debug(tmpWSMethod.toString());
            resSet.add(tmpWSMethod);
        }

        log.debug("END buildWSMethodDescription");
        return resSet;
    }


    /**
     * This class contain result information about WSDL pre-processing.
     *
     * @author Alexandr Klimov
     */
    static public class WSDLProcessingResult{
        DescriptorsContainer descriptorContainer;

        /**
         * TODO: I can see this field isn't used anywhere.
         * It should be removed after WSDLCabinet application developing will be completed.
         */
        Map<Operation, Map<String, Message>> operationsToMessages = new HashMap<>();
        Set<WSMethodDescr> wsMethodDescr = new HashSet<>();

        /**
         * @param typeDescriptors
         * @param operationsToMessages
         * @param wsMethodDescr
         */
        public WSDLProcessingResult(DescriptorsContainer typeDescriptors,
                                    Map<Operation, Map<String, Message>> operationsToMessages,
                                    Set<WSMethodDescr> wsMethodDescr) {
            this.descriptorContainer = typeDescriptors;
            this.operationsToMessages = operationsToMessages;
            this.wsMethodDescr = wsMethodDescr;
        }

        public DescriptorsContainer getDescriptorContainer() {
            return descriptorContainer;
        }

        public Map<Operation, Map<String, Message>> getOperationsToMessages() {
            return operationsToMessages;
        }

        public Set<WSMethodDescr> getWsMethodDescr() {
            return wsMethodDescr;
        }
    }

    /**
     * This method process a WSDL definition and returns a map that contains:
     * <ul>
     *     <li>
     *         a list contains all port types;
     *     </li>
     *     <li>
     *         a map Operation->INPUT/OUTPUT_msg
     *     </li>
     * </ul>
     *
     * @see ru.aklimov.wsdlcomparator.WSDLProcessor#PORT_TYPES_PROCESS_OPERATION_LIST
     * @see ru.aklimov.wsdlcomparator.WSDLProcessor#PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP
     *
     * @param wsdlDefinition
     * @return a Map contains list all port types and a Map&lt; Operation-&gt;Messages &gt;
     */
    static private Map<String, Object> processPortTypes(final Definition wsdlDefinition){
        //todo: What we have to do if the same type/element is used in both request and response in different operations?
        log.info("Loading all operations from all port types...");
        Map portTypesMap = wsdlDefinition.getPortTypes();
        Collection<PortType> portTypes = portTypesMap.values();

        List<Operation> operations = new LinkedList<>();
        Map<Operation, Map<String, Message>> operationsToMessages = new HashMap<Operation, Map<String, Message>>();
        Map<Operation, QName> operationToPortTypeQNameMap = new HashMap<>();

        for(PortType pti : portTypes){
            log.debug("\n\tPort type: "+pti.getQName());
            List<Operation> tmpOperations = pti.getOperations();
            operations.addAll(tmpOperations);
            for(Operation oper : tmpOperations){
                log.debug("Operation: " + oper.getName());
                Map<String, Message> msgTmpMap = new HashMap<>();

                msgTmpMap.put(INPUT_MESSAGE_KEY, oper.getInput().getMessage());
                log.debug("input message: "+oper.getInput().getMessage().getQName());

                //If method has response type (non one-way)
                if(oper.getOutput()!=null){
                    msgTmpMap.put(OUTPUT_MESSAGE_KEY, oper.getOutput().getMessage());
                    log.debug("output message: " + oper.getOutput().getMessage().getQName());
                }

                operationsToMessages.put(oper, msgTmpMap);
                operationToPortTypeQNameMap.put(oper, pti.getQName());
            }
        }
        log.info("\n\tOperations-to-messages map have been built.\n");

        Map<String, Object> resMap = new HashMap<>();
        resMap.put(PORT_TYPES_PROCESS_OPERATION_LIST, operations);
        resMap.put(PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP, operationsToMessages);
        resMap.put(PORT_TYPES_OPER_TO_PORT_QNAME, operationToPortTypeQNameMap);

        return resMap;
    }


    /**
     * This method process all messages in a WSDL definition.<br/>
     * Returned by this method both types and elements will be used as start points for XSD schemas processing.<br/>
     *
     * @param wsdlDefinition
     * @return
     */
    static private MessageProcessingResult messagesProcessing(Definition wsdlDefinition){
        MessageProcessingResult result = new MessageProcessingResult();

        log.info("Messages processing...");

        Map messages = wsdlDefinition.getMessages();
        if(messages == null){
            log.warn("There isn't any message!");
            return result;
        }

        Collection messageObjects = messages.values();
        for(Object messageObj : messageObjects){
            Message message = (Message) messageObj;
            log.debug("Processed message: "+message.getQName());
            Map parts = message.getParts();
            Collection partObjects = parts.values();

            if( partObjects.isEmpty() ){
                log.warn(message.getQName()+" does not contain any message part!");
                continue;
            }

            for (Object partObj : partObjects) {
                Part messagePart = (Part) partObj;
                //Put part to message->part map
                if (result.getMessageToPart().containsKey(message)) {
                    result.getMessageToPart().get(message).add(messagePart);
                } else {
                    List<Part> tmpPartList = new LinkedList<>();
                    tmpPartList.add(messagePart);
                    result.getMessageToPart().put(message, tmpPartList);
                }
                //Parse message part
                processMessagePart(messagePart, result);
            }

        }

        log.info(messageObjects.size()+" messages have been processed.\n\n");

        return result;

    }

    /**
     * Auxiliary method that process a WSDL message part object.<br>
     * Over processing may be filled following result fields:
     * <ul>
     *     <li>rootWSDLMessageElementNames</li>
     *     <li>rootWSDLMessageTypeNames</li>
     *     <li>elemQNameToPart</li>
     *     <li>typeQNameToPart</li>
     * </ul>
     *
     * @param messagePart part for processing.
     * @param result filled over processing by processing results.
     */
    private static void processMessagePart(Part messagePart, MessageProcessingResult result) {
        if(messagePart.getElementName() != null){
            QName elemQName = messagePart.getElementName();
            result.getRootWSDLMessageElementNames().add(elemQName);

            if( result.getElemQNameToPart().containsKey(elemQName) ){
                result.getElemQNameToPart().get(elemQName).add(messagePart);

            } else {
                List<Part> tmpPartLst = new LinkedList<>();
                tmpPartLst.add(messagePart);
                result.getElemQNameToPart().put(elemQName, tmpPartLst);

            }
            log.debug("Element of part "+messagePart.getName()+": "+elemQName);

        } else {
            QName typeQName = messagePart.getTypeName();
            result.getRootWSDLMessageTypeNames().add(typeQName);

            if( result.getTypeQNameToPart().containsKey(typeQName) ){
                result.getTypeQNameToPart().get(typeQName).add(messagePart);
            }else{
                List<Part> tmpPartLst = new LinkedList<>();
                tmpPartLst.add(messagePart);
                result.getTypeQNameToPart().put(typeQName, tmpPartLst);
            }
            log.debug("Type of part "+messagePart.getName()+": "+typeQName);

        }
    }

    static private XmlSchema[] getXmlSchemaCollection(Definition wsdlDefinition){
        log.info("XSD schemas obtaining");

        if(wsdlDefinition.getTypes() == null){
            log.warn("No one type has been found in the processed WSDL!");
            return new XmlSchema[0];
        }

        List extensibilityElements = wsdlDefinition.getTypes().getExtensibilityElements();

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        for(Object i : extensibilityElements){
            Schema shm = (Schema)i;
            Element element = shm.getElement();
            schemaCol.read(element);
        }

        return schemaCol.getXmlSchemas();
    }


    //////////
    /// Private domain classes ///
    //////////
    
    private static class MessageProcessingResult{
        private final List<QName> rootWSDLMessageElementNames = new LinkedList<>();
        private final List<QName> rootWSDLMessageTypeNames = new LinkedList<>();
        private final Map<QName, List<Part>> elemQNameToPart = new HashMap<>();
        private final Map<QName, List<Part>> typeQNameToPart = new HashMap<>();
        private final Map<Message, List<Part>> messageToPart = new HashMap<>();

        public List<QName> getRootWSDLMessageElementNames() {
            return rootWSDLMessageElementNames;
        }

        public List<QName> getRootWSDLMessageTypeNames() {
            return rootWSDLMessageTypeNames;
        }

        public Map<QName, List<Part>> getElemQNameToPart() {
            return elemQNameToPart;
        }

        public Map<QName, List<Part>> getTypeQNameToPart() {
            return typeQNameToPart;
        }

        public Map<Message, List<Part>> getMessageToPart() {
            return messageToPart;
        }
    }
}
