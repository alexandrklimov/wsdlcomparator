package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.utils.XmlSchemaNamedImpl;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;
import ru.aklimov.wsdlcomparator.domain.ProcessCntx;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This class does XML-schema type processing for a XSD type descriptor building.<br/>
 * "Entry point" of processing an XSD-type is getDescriptorContainer method. This method consumes a results of a pre-processing
 * (either a WSDL pre-processing result or a Xml-schema pre-processing result)
 *
 */
public class XmlSchemaItemsProcessor {
    static private Logger LOG = LoggerFactory.getLogger(XmlSchemaItemsProcessor.class);

    static public TypeDescriptor getTypeDescriptor(ProcessCntx context){
        XmlSchemaType processedType = context.getType();
        TypeDescriptor resTd = context.getTypeToDescriptorMap().get(processedType);

        //multi-processing prevention
        if(context.getTypeToDescriptorMap().get( processedType ) != null){
            if(context.getOwnerPair() != null){
                resTd.getOwnerInfoLst().add( context.getOwnerPair() );
            }
            return resTd;
        }

        if( processedType instanceof XmlSchemaSimpleType){
            Map<QName, XmlSchemaType> xsdQNameToTypeMap = context.getXsdQNameToTypeMap();
            OwnerInfo ownerPair = context.getOwnerPair();
            Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap = context.getTypeToDescriptorMap();
            
            return getSimpleTypeDescriptor((XmlSchemaSimpleType) processedType, xsdQNameToTypeMap, ownerPair, typeToDescriptorMap);

        } else {
            return getComplexTypeDescriptor(context);

        }

    }

    static private TypeDescriptor getSimpleTypeDescriptor(XmlSchemaSimpleType simpleType,
                                                          Map<QName, XmlSchemaType> xsdTypeMap,
                                                          OwnerInfo ownerPair,
                                                          Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap){

        TypeDescriptor res = new TypeDescriptor(
                                    false,
                                    simpleType.getQName(),
                                    simpleType.getName(),
                                    getTargetUriSchemaTypeObj(simpleType),
                                    ownerPair
                                );

        res.setBaseXsdType( isBaseType(simpleType) );
        res.setBaseTypeQName( getBaseSimpleTypeQName(simpleType) );
        res.setBaseTypeName( cleanStringQName(res.getBaseTypeQName()) );
        //TODO: load annotation for type

        if( simpleType.getContent()!=null ){
            res.setSimpleContentType(simpleType.getContent().getClass());
        }

        res.setFacets(getSimpleTypeFacets(simpleType));

        //For preventing probable multi-processing of this type during descriptors processing
        typeToDescriptorMap.put(simpleType, res);

        //Here is a recursion if a current analysed type is not base XSD type only (f.e. xsd:string or xsd:integer)
        if(!isBaseType(simpleType) && res.getBaseTypeQName()!=null){
            OwnerInfo baseOwnerPair = new OwnerInfo();
            baseOwnerPair.setTypeDescriptor(res);
            //a base type of simple type can be an other simple type only
            XmlSchemaSimpleType baseType = (XmlSchemaSimpleType) xsdTypeMap.get(res.getBaseTypeQName());
            TypeDescriptor baseTypeDesc = getSimpleTypeDescriptor( baseType, xsdTypeMap, baseOwnerPair, typeToDescriptorMap);
            res.setBaseType(baseTypeDesc);
        }

        return res;
    }


    /**
     * Complex type may have a complexContent section or may not.<br/>
     * In depending from this fact we should obtain element of this complex type by different ways.<br/>
     *<br/>
     * This method processes XmlSchemaComplexContentRestriction and XmlschemaComplexContentExtension
     * content types.<br/>
     *<br/>
     * This method returns instance of TypeDescriptor class contains some information about complex type.<br/>
     * <br/>
     * <strong>A processing context should be newly created before call of this function.</strong>
     *
     *
     * @param context
     * @return
     */
    static private TypeDescriptor getComplexTypeDescriptor(ProcessCntx context){
        XmlSchemaComplexType xsdComplexType = (XmlSchemaComplexType) context.getType();
        
        TypeDescriptor resultTd = new TypeDescriptor(
                                    true,
                                    xsdComplexType.getQName(),
                                    xsdComplexType.getName(),
                                    getTargetUriSchemaTypeObj(xsdComplexType),
                                    context.getOwnerPair()
                                );
        context.setCurrentProcessedTypeDesc(resultTd);
        //TODO: read annotation for type

        XmlSchemaParticle particle = null;
        if( xsdComplexType.getContentModel() != null ){
            resultTd.setContentModelType(xsdComplexType.getContentModel().getClass());
            resultTd.setComplexContentType(xsdComplexType.getContentModel().getContent().getClass());
            XmlSchemaContent content = xsdComplexType.getContentModel().getContent();

            if( content instanceof XmlSchemaComplexContentRestriction ){
                XmlSchemaComplexContentRestriction contentRestriction = (XmlSchemaComplexContentRestriction) content;
                resultTd.setBaseTypeQName(contentRestriction.getBaseTypeName());
                resultTd.setBaseTypeName(contentRestriction.getBaseTypeName().toString());
                particle = contentRestriction.getParticle();

            } else if( content instanceof XmlSchemaSimpleContentRestriction ){
                XmlSchemaSimpleContentRestriction contentRestriction = (XmlSchemaSimpleContentRestriction) content;
                resultTd.setBaseTypeQName(contentRestriction.getBaseTypeName());
                resultTd.setBaseTypeName(contentRestriction.getBaseTypeName().toString());

            } else if( content instanceof XmlSchemaComplexContentExtension ){
                XmlSchemaComplexContentExtension contentExtension = (XmlSchemaComplexContentExtension) content;
                resultTd.setBaseTypeQName(contentExtension.getBaseTypeName());
                resultTd.setBaseTypeName(contentExtension.getBaseTypeName().toString());
                particle = contentExtension.getParticle();

            } else if( content instanceof XmlSchemaSimpleContentExtension){
                XmlSchemaSimpleContentExtension contentExtension = (XmlSchemaSimpleContentExtension) content;
                resultTd.setBaseTypeQName(contentExtension.getBaseTypeName());
                resultTd.setBaseTypeName(contentExtension.getBaseTypeName().toString());

            }

        } else {
            particle = xsdComplexType.getParticle();

        }

        LOG.debug("Processing complex type: " + xsdComplexType);

        //For preventing probable multi-processing of this type during descriptors processing
        context.getTypeToDescriptorMap().put(xsdComplexType, resultTd);

            //here is processing a base type if presented
        if(resultTd.getBaseTypeQName()!=null){
                //There is a recursion
            OwnerInfo baseOwnerPair = new OwnerInfo();
            baseOwnerPair.setTypeDescriptor(resultTd);
            baseOwnerPair.setChild(true);
            //A base type of some complex type can be either complex type or simple type
            XmlSchemaType baseType = context.getXsdQNameToTypeMap().get(resultTd.getBaseTypeQName());
            ProcessCntx baseTypeProcCntx = context.buildContextForNewType(baseType, baseOwnerPair);
            TypeDescriptor baseTd = getTypeDescriptor(baseTypeProcCntx);
            resultTd.setBaseType(baseTd);
        }

        if( particle != null ){
            if( LOG.isDebugEnabled() ){
                LOG.debug("xsdComplexType Particle type is " + particle.getClass().getName());
            }

            //Despite the fact that a reference to a group is some kind of a particle too, a group is processed in some
            //dedicated way.
            if(particle instanceof XmlSchemaGroupParticle){
                if(particle instanceof XmlSchemaAll){
                    resultTd.setRootIndicatorType(XmlSchemaAll.class);
                } else if( particle instanceof XmlSchemaChoice){
                    resultTd.setRootIndicatorType(XmlSchemaChoice.class);
                } else {
                    resultTd.setRootIndicatorType(XmlSchemaSequence.class);
                }

                IndicatorDescriptor rootIndDescr = particleProcess(context, null, (XmlSchemaGroupParticle) particle, 1);
                resultTd.setRootIndicator(rootIndDescr);

            } else if(particle instanceof XmlSchemaGroupRef){
                LOG.debug("Root group reference processing start. "+resultTd.getId());
                XmlSchemaGroupRef xmlGrpRef = (XmlSchemaGroupRef) particle;
                XmlSchemaGroup xmlSchemaGroup = context.getXsdQNameToGroupMap().get(xmlGrpRef.getRefName());

                //Create new context
                OwnerInfo referOwner = new OwnerInfo();
                referOwner.setTypeDescriptor(resultTd);
                ProcessCntx groupProcessingContext = context.buildContextForNewGroup(xmlSchemaGroup, referOwner);

                GroupDescriptor groupDescr = getGroupDescriptor(groupProcessingContext);
                GroupReference rootGrpRef = 
                       new GroupReference(xmlSchemaGroup.getQName(), groupDescr, xmlGrpRef.getMinOccurs(), xmlGrpRef.getMaxOccurs());
                resultTd.setRootGroupRef(rootGrpRef);

            }
        }

        List<XmlSchemaAttribute> xsdComplexTypeAttrs = new LinkedList<>();
        {
            List<XmlSchemaAttributeOrGroupRef> attributes = xsdComplexType.getAttributes();
            for (XmlSchemaAttributeOrGroupRef attrOrRef : attributes){
                xsdComplexTypeAttrs.add((XmlSchemaAttribute) attrOrRef);
            }
        }

        complexTypeAttributesProcess(resultTd, xsdComplexTypeAttrs, context);

        return resultTd;
    }


    /**
     *  <strong>A processing context should be newly created before call of this function.</strong>
     *
     * @param context
     * @return
     */
    static public GroupDescriptor getGroupDescriptor(ProcessCntx context){
        XmlSchemaGroup groupForProcessing = context.getGroup();
        if(groupForProcessing == null){
            throw  new IllegalArgumentException("A group for processing is NULL!");
        }

        if( context.getGroupToDescriptorMap().get(groupForProcessing) != null){
            GroupDescriptor tmpGd = context.getGroupToDescriptorMap().get(groupForProcessing);
            if(context.getOwnerPair() != null){
                tmpGd.getRefBy().add( context.getOwnerPair() );
            }
            return tmpGd;
        }

        GroupDescriptor groupDescr = new GroupDescriptor();
        QName qName = groupForProcessing.getQName();
        groupDescr.setQName(qName);
        groupDescr.setName((qName != null) ? qName.getLocalPart() : null);
        groupDescr.setId(qName.toString());
        if(context.getOwnerPair() != null){
            groupDescr.getRefBy().add( context.getOwnerPair() );
        }
        context.setCurrentProcessedGroupDesc(groupDescr);
        //For preventing double processing this group declaration and for supporting process children items
        //put this uncompleted group into map
        context.getGroupToDescriptorMap().put(groupForProcessing, groupDescr);

        XmlSchemaGroupParticle xmlSchemaGroupRootParticle = groupForProcessing.getParticle();
        if(xmlSchemaGroupRootParticle != null){
            groupDescr.setRootIndicatorType( xmlSchemaGroupRootParticle.getClass() );
            IndicatorDescriptor indicatorDescriptor = particleProcess(context, null, xmlSchemaGroupRootParticle, 1);
            groupDescr.setRootIndDescr(indicatorDescriptor);
        }
        //TODO: annotation process
        return groupDescr;
    }


    /**
     * This function processes a particle of a complex type in a recursive manner.<br/>
     * One obtains indicator type info. and info. about each an element.<br/>
     * <br/>
     * A processing context shouldn't be newly created before call this function as any group particle isn't self-independent
     * entity, hence it's should be processed in a parent items context scope.
     *
     * @param context
     * @param parentGroupIndDescr
     * @param indicator
     * @param indicatorSerialNumberInParent start from 1
     * @return
     */
    static private IndicatorDescriptor particleProcess( final ProcessCntx context,
                                                    final IndicatorDescriptor parentGroupIndDescr,
                                                    final XmlSchemaGroupParticle indicator,
                                                    final int indicatorSerialNumberInParent){

        IndicatorDescriptor indDescr = null;

        //A particle of complex type  may be null if a complex type only has not got any descriptors.
        //For example this type may only has got attributes.
        if( indicator != null ){

            LOG.debug("\n\tSTART CREATE ITEMS DESCRIPTORS\n");

            List items = getParticleItems(indicator);

            //Creating item descriptions
            indDescr = new IndicatorDescriptor();
            indDescr.setType( indicator.getClass() );
            indDescr.setMinOccurs( indicator.getMinOccurs() );
            indDescr.setMaxOccurs( indicator.getMaxOccurs() );
            indDescr.setMaxOccUnbound( Long.MAX_VALUE == indicator.getMaxOccurs() );

            String indicatorId;
            if(parentGroupIndDescr==null){
                if(context.getCurrentProcessedTypeDesc() != null){
                    indDescr.setOwnerTd( context.getCurrentProcessedTypeDesc() );
                    indicatorId = context.getCurrentProcessedTypeDesc().getId() + "|" + indicatorSerialNumberInParent;

                } else if(context.getCurrentProcessedGroupDesc() != null){
                    indDescr.setOwnerGroup(context.getCurrentProcessedGroupDesc());
                    indicatorId = context.getCurrentProcessedGroupDesc().getId() + "|" + indicatorSerialNumberInParent;

                } else {
                    String errMessage = "Context doesn't contains any parent for a processed group particle. "+indicator.getClass();
                    LOG.error(errMessage);
                    throw new IllegalStateException(errMessage);
                }

            } else {
                //Copy parent links! NOT SET PARENT LIST!
                indDescr.getParentChain().addAll( parentGroupIndDescr.getParentChain() );
                indDescr.addParentLink(parentGroupIndDescr);
                indicatorId = parentGroupIndDescr.getId() + "|" + indicatorSerialNumberInParent;
            }
            indDescr.setId(indicatorId);
            List<ParticleContent> groupIndContent = indDescr.getItems();

            /**
             * Child elements of the group indicator processing
             */
            int childIndicatorSerNum = 1;
            for(Object item : items){
                if( item instanceof XmlSchemaGroupParticle){
                    LOG.debug("Particle for processing: " + item.getClass().getSimpleName());
                    IndicatorDescriptor innerGroupIndDescr = new IndicatorDescriptor();
                    //Set type of the inner indDescr
                    if(item instanceof XmlSchemaAll){
                        innerGroupIndDescr.setType(XmlSchemaAll.class);
                    } else if( item instanceof XmlSchemaChoice){
                        innerGroupIndDescr.setType(XmlSchemaChoice.class);
                    } else {
                        innerGroupIndDescr.setType(XmlSchemaSequence.class);
                    }
                    //Set parent
                    IndicatorDescriptor groupIndDescr = particleProcess(context, indDescr, (XmlSchemaGroupParticle)item, childIndicatorSerNum);
                    groupIndContent.add(groupIndDescr);
                    childIndicatorSerNum++;

                } else if(item instanceof XmlSchemaGroupRef){
                    XmlSchemaGroupRef xmlGrpRef = (XmlSchemaGroupRef)item;
                    QName groupRefName = xmlGrpRef.getRefName();
                    LOG.debug("Start an group reference processing. "+groupRefName);
                    XmlSchemaGroup xmlSchemaGroup = context.getXsdQNameToGroupMap().get(groupRefName);

                    //Create new context
                    OwnerInfo parentCntxOwner = context.getOwnerPair();
                    OwnerInfo refOwner = new OwnerInfo();

                    if(context.getCurrentProcessedTypeDesc() != null){
                        refOwner.setTypeDescriptor( context.getCurrentProcessedTypeDesc() );
                    } else {
                        refOwner.setGroupDescriptor(context.getCurrentProcessedGroupDesc());
                    }
                    refOwner.setIndicatorDescriptor(indDescr);
                    ProcessCntx groupProcessingContext = context.buildContextForNewGroup(xmlSchemaGroup, refOwner);

                    GroupDescriptor groupDescriptor = getGroupDescriptor(groupProcessingContext);
                    GroupReference groupReference = new GroupReference(groupRefName, groupDescriptor, xmlGrpRef.getMinOccurs(), xmlGrpRef.getMaxOccurs());
                    groupIndContent.add(groupReference);

                } else {
                    //Original element processing
                    LOG.debug("Start an element processing.");
                    XmlSchemaElement elem = (XmlSchemaElement)item;
                    ElementDescriptor elemDescr = buildElemDescr(elem, indDescr, context);
                    groupIndContent.add(elemDescr);

                }

            }
            LOG.debug("\n\tEND CREATE ITEMS DESCRIPTORS\n");
        }

        return indDescr;
    }

    static private ElementDescriptor buildElemDescr(XmlSchemaElement elem, IndicatorDescriptor elemOwnerIndDescr, ProcessCntx context){
        final String elemName;
        QName refQName = null;
        boolean resolvedByReference = false;
        XmlSchemaElement templateElem = null;

        //Check a reference to an other element
        XmlSchemaRef<XmlSchemaElement> ref = elem.getRef();
        if(ref != null){
            if(ref.getTargetQName() != null){
                refQName = ref.getTargetQName();
                if( refQName != null ){
                    LOG.debug("\tElement is defined by a reference: " + refQName);
                    resolvedByReference = true;
                    templateElem = context.getXsdElemQNameToElemMap().get(refQName);
                }
            }
        }

        if(resolvedByReference){
            //TODO: Pidgin English?
            //If element contains ref attribute then make a decision about where the element name we should load from.
            if(elem.getName() != null && ( !"".equals(elem.getName()) ) ){
                elemName = elem.getName();
            } else {
                //Using a name the referenced element
                elemName = templateElem.getName();
            }
            //From this moment we are processing template element
            elem = templateElem;
        } else {
            elemName = elem.getName();
        }

        LOG.debug("Element for processing: " + elemName);

        ElementDescriptor elemDescr = new ElementDescriptor();
        elemDescr.setName( elem.getName() );
        if(resolvedByReference){
            elemDescr.setByRef(resolvedByReference);
            elemDescr.setRefName(refQName);
        }

        //Process an unqualified namespace case
        QName elemQName;
        if(elem.getQName()==null){
            elemQName = new QName("", elem.getName());
        } else {
            elemQName = elem.getQName();
        }

        elemDescr.setQname( elemQName );
        elemDescr.setMinOccurs( elem.getMinOccurs() );
        elemDescr.setIndDescr(elemOwnerIndDescr);

        long maxOccr = elem.getMaxOccurs();
        elemDescr.setMaxOccurs(maxOccr);
        if(Long.MAX_VALUE==maxOccr){
            elemDescr.setMaxOccUnbound(true);
        }

        elemDescr.setNillable( elem.isNillable() );
        elemDescr.setDefaultVal( elem.getDefaultValue() );
        elemDescr.setFixedVal( elem.getFixedValue() );

        LOG.debug("Obtaining type of " + elem.getName() + " descriptors");
        XmlSchemaType elemType  = elem.getSchemaType();

        //At this time a type may be null - XmlSchema:2.0.3 library presents xsd:anyType as null
        if(elemType==null){
            elemType = XsdBaseSchemaHolder.getAnyType();
        }

        TypeDescriptor elemTypeDescr = runElemTypeProcessing(elem.getName(), elemOwnerIndDescr, elemType, context);
        elemDescr.setTypeDescr( elemTypeDescr );

        //Load annotation of the element
        if(elem.getAnnotation()!=null){
            if(elem.getAnnotation().getItems()!=null){
                List<AnnotationDescriptor> annRes = loadAnnotations(elem.getAnnotation().getItems());
                elemDescr.setAnnotations( annRes );
            }
        }

        return elemDescr;
    }

    /**
     * This is an auxiliary method for obtaining a type descriptor of an element.
     *
     * @param elemName
     * @param elementOwnerIndicator
     * @param xsdElemType
     * @param context
     * @return
     */
    static private TypeDescriptor runElemTypeProcessing(String elemName,
                                                            IndicatorDescriptor elementOwnerIndicator,
                                                                XmlSchemaType xsdElemType,
                                                                    ProcessCntx context ){
        OwnerInfo thatElemTypeOwnerPair = new OwnerInfo();
        thatElemTypeOwnerPair.setElemName( elemName );
        thatElemTypeOwnerPair.setTypeDescriptor( context.getCurrentProcessedTypeDesc() );
        thatElemTypeOwnerPair.setIndicatorDescriptor(elementOwnerIndicator);

        ProcessCntx elemTypeProcCntx = context.buildContextForNewType(xsdElemType, thatElemTypeOwnerPair);

        TypeDescriptor elemTypeDescr = getTypeDescriptor(elemTypeProcCntx);
        return elemTypeDescr;

    }

    /**
     * Return items either from a sequence, or from choice, or from all indicator.
     *
     * @param particle
     * @return a list may contain both XmlSchemaElement instances and XmlSchemaGroupParticle child class instance
     */
    static private List getParticleItems(XmlSchemaGroupParticle particle){
        List items = new LinkedList();

        XmlSchemaGroupParticle groupBaseParticle = particle;
        if( particle instanceof XmlSchemaChoice){
            items = ((XmlSchemaChoice) particle).getItems();
        } else if(particle instanceof XmlSchemaAll){
            items = ((XmlSchemaAll) particle).getItems();
        } else {
            items = ((XmlSchemaSequence) particle).getItems();
        }

        return items;
    }

    static private void complexTypeAttributesProcess(TypeDescriptor res,
                                                        List<XmlSchemaAttribute> attributesColl,
                                                            ProcessCntx context){
        if(attributesColl==null){
            return;
        }

        Map<QName, XmlSchemaType> xsdQNameToTypeMap = context.getXsdQNameToTypeMap();
        Map<QName, XmlSchemaElement> xsdElemQNameToElemMap = context.getXsdElemQNameToElemMap();
        Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap = context.getTypeToDescriptorMap();

        List<AttributeDescriptor> attrDescSet = new LinkedList<>();
        for(XmlSchemaAttribute attr : attributesColl){
            //Type of a attribute only can be simpleType(may be anonymous, may be restricted)
            //TODO: attribute anonymous type should able for processing too
            XmlSchemaSimpleType attrType = (XmlSchemaSimpleType) xsdQNameToTypeMap.get(attr.getSchemaTypeName());
            OwnerInfo ownerPair = new OwnerInfo();
            ownerPair.setTypeDescriptor(res);
            ProcessCntx attrTypeProcCntx = context.buildContextForNewType(attrType, ownerPair);
            TypeDescriptor attrTypeDesc = getTypeDescriptor(attrTypeProcCntx);

            AttributeDescriptor attrDesc = new AttributeDescriptor();
            attrDesc.setTypeDescr(attrTypeDesc);
            attrDesc.setName(attr.getName());
            attrDesc.setQName(attr.getQName());
            attrDesc.setDefaultVal(attr.getDefaultValue());
            attrDesc.setFixedVal(attr.getFixedValue());

            XmlSchemaUse useVal = attr.getUse();
            if( !XmlSchemaUse.NONE.equals(useVal) ){
                attrDesc.setUse( useVal.toString() );
            }

            //Load annotation
            if(attr.getAnnotation()!=null){
                if(attr.getAnnotation().getItems()!=null){
                    List<AnnotationDescriptor> annRes = loadAnnotations(attr.getAnnotation().getItems());
                    attrDesc.setAnnotations( annRes );
                }
            }

            attrDescSet.add(attrDesc);
        }

        res.setAttributes(attrDescSet);
    }

    /**
     * Simple type may have facets: some restrictions on it.
     * For ex. length, maxLengthExclusive, pattern, enumeration e.t.c
     * This method returns an array of facet instances.
     *
     * @param simpleType
     * @return XmlSchemaFacet[]
     */
    static private XmlSchemaFacet[] getSimpleTypeFacets(XmlSchemaSimpleType simpleType){
        List<XmlSchemaFacet> res = new LinkedList<>();

        if(simpleType.getContent()!=null){
            XmlSchemaSimpleTypeContent simpleTypeContent = simpleType.getContent();
            if(simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction){
                XmlSchemaSimpleTypeRestriction simpleTypeContRestrict = (XmlSchemaSimpleTypeRestriction) simpleTypeContent;
                List<XmlSchemaFacet> facets = simpleTypeContRestrict.getFacets();
                res.addAll(facets);
            }
        }

        return res.toArray( new XmlSchemaFacet[]{});
    }

    /**
     * XSD simple type may have content (restriction, list, union) or may not.
     * In depending from it we should obtain base type name by defferent ways
     *
     * If provided simple type does not have content section then return empty null;
     *
     * @param simpleType
     * @return QName
     */
    static private QName getBaseSimpleTypeQName(XmlSchemaSimpleType simpleType){
        QName typeName = null;
        if(simpleType.getContent()!=null){
            XmlSchemaSimpleTypeContent simpleTypeContent = simpleType.getContent();
            if(simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction){
                XmlSchemaSimpleTypeRestriction simpleTypeContRestrict = (XmlSchemaSimpleTypeRestriction) simpleTypeContent;
                typeName = simpleTypeContRestrict.getBaseTypeName();
            } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeList){
                XmlSchemaSimpleTypeList simpleTypeContList = (XmlSchemaSimpleTypeList) simpleTypeContent;
                typeName = simpleTypeContList.getItemType().getQName();
            }
        }

        return typeName;
    }

    /**
     * This function is used for load annotation of an element, a type, and so on.
     *
     * @param items those are items of an annotation. Documentation or appinfo instances collection.
     * @return List<AnnotationDescriptor>
     */
    static private List<AnnotationDescriptor> loadAnnotations(List<XmlSchemaAnnotationItem> items){
        List<AnnotationDescriptor> resLst = new LinkedList<>();

        for(XmlSchemaAnnotationItem annotation : items){
            NodeList markup = null;
            AnnotationDescriptor.ANNOTATION_TYPE annType = null;

            if(annotation instanceof XmlSchemaAppInfo){
                annType = AnnotationDescriptor.ANNOTATION_TYPE.APPINFO;
                XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) annotation;
                if(appInfo.getMarkup()!=null){
                    if(appInfo.getMarkup().getLength()!=0){
                        markup = appInfo.getMarkup();
                    }
                }

            } else if(annotation instanceof XmlSchemaDocumentation){
                annType = AnnotationDescriptor.ANNOTATION_TYPE.DOCUMENTATION;
                XmlSchemaDocumentation documentation = (XmlSchemaDocumentation) annotation;
                if(documentation.getMarkup()!=null){
                    if(documentation.getMarkup().getLength()!=0){
                        markup = documentation.getMarkup();
                    }
                }
            }

            if(markup!=null){
                StringBuilder sb = new StringBuilder();
                for(int c=0; c<markup.getLength(); c++){
                    sb.append(markup.item(c).getNodeValue());
                }
                AnnotationDescriptor annDescr = new AnnotationDescriptor();
                annDescr.setAnnotationType(annType);
                annDescr.setValue(sb.toString());
                resLst.add(annDescr);
            }
        }

        return resLst;
    }


    /**
     * This function return QName.toString if the qname isn't from XSD standart types namespace
     * and local part of the name only if the qname is one of XSD standart types
     *
     * @param qname
     * @return String
     */
    static public String cleanStringQName(QName qname){
        String typeNameStr = "";
        if(qname != null ){
            if( "http://www.w3.org/2001/XMLSchema".equals(qname.getNamespaceURI()) ){
                typeNameStr = qname.getLocalPart();
            } else {
                typeNameStr = qname.toString();
            }
        }
        return typeNameStr;
    }


    /**
     * This function checks if a type is base xsd type like string, decimal and so on.
     *
     * @param type
     * @return boolean
     */
    static public boolean isBaseType(XmlSchemaType type){
        if(type.getQName() == null){
            return false;
        }
        return Constants.URI_2001_SCHEMA_XSD.equals(type.getQName().getNamespaceURI());
    }

    /**
     *It's a hard hack of a xml schema type class otherwise we cant' obtain target namespace value.
     *For example it's necessary for ordinary element anonymous type.
     *
     * @return XmlSchema#targetNamespace or an empty string, if a namespace is unqualified.
     */
    static private <T extends XmlSchemaType> String getTargetUriSchemaTypeObj(T type){
        String res = null;
        try {
            Field namedDelegateFld = type.getClass().getSuperclass().getDeclaredField("namedDelegate");
            namedDelegateFld.setAccessible(true);
            XmlSchemaNamedImpl namedDelegateImpl = (XmlSchemaNamedImpl) namedDelegateFld.get(type);

            Field schemaFld = namedDelegateImpl.getClass().getDeclaredField("parentSchema");
            schemaFld.setAccessible(true);
            XmlSchema schemaObj = (XmlSchema) schemaFld.get(namedDelegateImpl);
            res = schemaObj.getTargetNamespace();
            if(res == null){
                res = "";
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return res;
    }



}
