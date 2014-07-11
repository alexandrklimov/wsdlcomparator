package ru.aklimov.wsdlcomparator;

import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.ProcessCntx;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

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
    static private final String MESSAGE_PROCESS_ROOT_WSDL_MSG_ELEM_NAMES = "rootWSDLMessageElementNames";
    static private final String MESSAGE_PROCESS_ROOT_WSDL_MSG_TYPE_NAMES = "rootWSDLMessageTypeNames";
    static private final String MESSAGE_PROCESS_ELEM_QNAME_TO_MSG_MAP = "elemQNameToMessage";
    static private final String MESSAGE_PROCESS_TYPE_QNAME_TO_MSG_MAP = "typeQNameToMessage";


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
        Map<String, Object> messagesProcessingRes = messagesProcessing(definition);
        List<QName> rootWSDLMessageElementNames = (List<QName>) messagesProcessingRes.get(MESSAGE_PROCESS_ROOT_WSDL_MSG_ELEM_NAMES);
        List<QName> rootWSDLMessageTypeNames = (List<QName>) messagesProcessingRes.get(MESSAGE_PROCESS_ROOT_WSDL_MSG_TYPE_NAMES);
        Map<QName, List<Message>> elemQNameToMessage = (Map<QName, List<Message>>) messagesProcessingRes.get(MESSAGE_PROCESS_ELEM_QNAME_TO_MSG_MAP);
        Map<QName, List<Message>> typeQNameToMessage = (Map<QName, List<Message>>) messagesProcessingRes.get(MESSAGE_PROCESS_TYPE_QNAME_TO_MSG_MAP);

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
        Map<XmlSchemaType, List<Message>> typeToMessage = new HashMap<>();

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
                if(typeQNameToMessage.containsKey(typeQName)){
                    typeToMessage.put( xsdTypeMap.get(typeQName), typeQNameToMessage.get(typeQName) );
                }
            }

            xsdElemQNameToElemMap.putAll( schema.getElements() );

        }

        //Resolving XSD type for root descriptors will be built
        //Transform (message_part_element_namemessage binding to message-part-type-name-to-message one.
        if( !rootWSDLMessageElementNames.isEmpty() ){
            for(XmlSchema schema : schemas){ //<-- for each schema
                for(QName elemQName : rootWSDLMessageElementNames){ //<--for each WSDL message element
                    XmlSchemaElement element = schema.getElementByName(elemQName);//<--return roots descriptors QName
                    if( element != null){
                        XmlSchemaType schemaType = element.getSchemaType();
                        //At this time a type may be null - XmlSchema:2.0.3 library presents xsd:anyType as null
                        if(schemaType==null){
                            schemaType = XsdBaseSchemaHolder.getAnyType();
                        }
                        typeToElementQName.put(schemaType, elemQName);
                        xsdRootElemTypes.add(schemaType);

                        if(elemQNameToMessage.containsKey(elemQName)){
                            typeToMessage.put(schemaType, elemQNameToMessage.get(elemQName));
                        }

                    }
                }
            }
        }

        if( !rootWSDLMessageTypeNames.isEmpty() ){
            for(QName name : rootWSDLMessageTypeNames){
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

        Map<TypeDescriptor, List<Message>> tdToMessages = createTdToMessageMap(typeToDescriptorMap, typeToMessage);
        Map<Operation, Map<String, Message>> operationsToMessages = (Map<Operation, Map<String, Message>>) processPortTypesRes.get(PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP);
        Set<WSMethodDescr> wsMethodDescr = getWSMethodDescription(operationsToMessages, tdToMessages);

        DescriptorsContainer descriptorsContainer = new DescriptorsContainer(typeDescrSet, groupDescrSet);
        WSDLProcessingResult wsdlProcRes = new WSDLProcessingResult(descriptorsContainer, operationsToMessages, tdToMessages, wsMethodDescr);

        return wsdlProcRes;
    }


    static private Map<TypeDescriptor, List<Message>> createTdToMessageMap(
            Map<XmlSchemaType, TypeDescriptor> typeToTdMap,
            Map<XmlSchemaType, List<Message>> typeToMessageMap){

        Map<TypeDescriptor, List<Message>> resMap = new HashMap<>();
        for(Map.Entry<XmlSchemaType, TypeDescriptor> typeToTdEntry : typeToTdMap.entrySet()){
            XmlSchemaType key = typeToTdEntry.getKey();
            if( typeToMessageMap.containsKey( key ) ){
                List<Message> msgLst = typeToMessageMap.get( key );
                resMap.put(typeToTdEntry.getValue(), msgLst);
            }
        }

        return resMap;
    }


    static public Set<WSMethodDescr> getWSMethodDescription(Map<Operation, Map<String, Message>> operationsToMessages,
                                                            Map<TypeDescriptor, List<Message>> typeDescriptorToMessage){
        log.debug("START getWSMethodDescription");
        Set<WSMethodDescr> resSet = new HashSet<>();

        for(Operation operation : operationsToMessages.keySet()){
            WSMethodDescr tmpWSMethod = new WSMethodDescr();
            tmpWSMethod.setMethodName( operation.getName() );
            log.debug("Method name(Operation name): " + operation.getName() );

            Map<String, Message> messages = operationsToMessages.get(operation);
            Message inputMsg = messages.get(INPUT_MESSAGE_KEY);
            Message outputMsg = messages.get(OUTPUT_MESSAGE_KEY);

            //Find type descriptor by message
            boolean foundInput = false;
            boolean foundOutput = false;
            for(Map.Entry<TypeDescriptor, List<Message>> entry : typeDescriptorToMessage.entrySet() ){
                if(entry.getValue().contains(inputMsg)){
                    TypeDescriptor td = entry.getKey();
                    tmpWSMethod.setRequestType(td);
                    tmpWSMethod.setRequestNamespace( td.getNamespaceURI() );
                    foundInput = true;

                } else if(entry.getValue().contains(outputMsg)){
                    TypeDescriptor td = entry.getKey();
                    tmpWSMethod.setResponseType(td);
                    tmpWSMethod.setResponseNamespace( td.getNamespaceURI() );
                    foundOutput = true;
                }

                if(foundInput && foundOutput){
                    break;
                }

            }
            log.debug("Input type is found: "+foundInput);
            log.debug("Output type is found: "+foundOutput);
            log.debug(tmpWSMethod.toString());

            resSet.add(tmpWSMethod);

        }

        log.debug("END getWSMethodDescription");
        return resSet;
    }


    /**
     * This class contain result information about WSDL pre-processing.
     *
     * @author Alexandr Klimov
     */
    static public class WSDLProcessingResult{
        DescriptorsContainer descriptorContainer;
        Map<Operation, Map<String, Message>> operationsToMessages = new HashMap<>();
        Map<TypeDescriptor, List<Message>> typeDescriptorToMessage = new HashMap<>();
        Set<WSMethodDescr> wsMethodDescr = new HashSet<>();

        /**
         * @param typeDescriptors
         * @param operationsToMessages
         * @param typeDescriptorToMessage
         */
        public WSDLProcessingResult(DescriptorsContainer typeDescriptors,
                                    Map<Operation, Map<String, Message>> operationsToMessages,
                                    Map<TypeDescriptor, List<Message>> typeDescriptorToMessage,
                                    Set<WSMethodDescr> wsMethodDescr) {
            this.descriptorContainer = typeDescriptors;
            this.operationsToMessages = operationsToMessages;
            this.typeDescriptorToMessage = typeDescriptorToMessage;
            this.wsMethodDescr = wsMethodDescr;
        }

        public DescriptorsContainer getDescriptorContainer() {
            return descriptorContainer;
        }

        public Map<Operation, Map<String, Message>> getOperationsToMessages() {
            return operationsToMessages;
        }

        public Map<TypeDescriptor, List<Message>> getTypeDescriptorToMessage() {
            return typeDescriptorToMessage;
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
            }
        }
        log.info("\n\tOperations-to-messages map have been built.\n");

        Map<String, Object> resMap = new HashMap<>();
        resMap.put(PORT_TYPES_PROCESS_OPERATION_LIST, operations);
        resMap.put(PORT_TYPES_PROCESS_OPER_TO_MSGS_MAP, operationsToMessages);

        return resMap;
    }


    /**
     * This method process all messages in a WSDL definition.<br/>
     * Returned by this method both types and elements will be used as start points for XSD schemas processing.<br/>
     *
     * @param wsdlDefinition
     * @return
     */
    static private Map<String, Object> messagesProcessing(Definition wsdlDefinition){
        final List<QName> rootWSDLMessageElementNames = new LinkedList<>();
        final List<QName> rootWSDLMessageTypeNames = new LinkedList<>();
        final Map<QName, List<Message>> elemQNameToMessage = new HashMap<>();
        final Map<QName, List<Message>> typeQNameToMessage = new HashMap<>();

        final HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(MESSAGE_PROCESS_ROOT_WSDL_MSG_ELEM_NAMES, rootWSDLMessageElementNames);
        resultMap.put(MESSAGE_PROCESS_ROOT_WSDL_MSG_TYPE_NAMES, rootWSDLMessageTypeNames);
        resultMap.put(MESSAGE_PROCESS_ELEM_QNAME_TO_MSG_MAP, elemQNameToMessage);
        resultMap.put(MESSAGE_PROCESS_TYPE_QNAME_TO_MSG_MAP, typeQNameToMessage);

        log.info("Messages processing...");

        Map messages = wsdlDefinition.getMessages();
        if(messages == null){
            log.warn("There isn't any message!");
            return resultMap;
        }

        Collection messageObjects = messages.values();
        for(Object messageObj : messageObjects){
            Message message = (Message) messageObj;
            log.debug("Processed message: "+message.getQName());
            Map parts = message.getParts();
            Collection partObjects = parts.values();
            //For simplification we are using only one, the first, part of a message
            if( partObjects.isEmpty() ){
                log.warn(message.getQName()+" does not contain any message part!");
                continue;
            }

            Part part = (Part) partObjects.iterator().next();
            if(part.getElementName() != null){
                QName elemQName = part.getElementName();
                rootWSDLMessageElementNames.add(elemQName);

                if(elemQNameToMessage.containsKey(elemQName)){
                    elemQNameToMessage.get(elemQName).add(message);

                } else {
                    List<Message> tmpMsgLst = new LinkedList<>();
                    tmpMsgLst.add(message);
                    elemQNameToMessage.put(elemQName, tmpMsgLst);

                }
                log.debug("Element of part: "+elemQName);

            } else {
                QName typeQName = part.getTypeName();
                rootWSDLMessageTypeNames.add(typeQName);

                if(typeQNameToMessage.containsKey(typeQName)){
                    typeQNameToMessage.get(typeQName).add(message);
                }else{
                    List<Message> tmpMsgLst = new LinkedList<>();
                    tmpMsgLst.add(message);
                    typeQNameToMessage.put(typeQName, tmpMsgLst);
                }
                log.debug("Type of part: "+typeQName);

            }

        }

        log.info(messageObjects.size()+" messages have been processed.\n\n");

        return resultMap;

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

}
