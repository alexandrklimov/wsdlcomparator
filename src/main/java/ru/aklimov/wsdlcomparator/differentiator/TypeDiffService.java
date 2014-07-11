package ru.aklimov.wsdlcomparator.differentiator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.*;
import static ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo.ROOT_GROUP_INDICATOR_DEFINITION_METHOD.BY_REF_TO_GROUP;
import static ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo.ROOT_GROUP_INDICATOR_DEFINITION_METHOD.DIRECTLY;
import static ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo.ROOT_GROUP_INDICATOR_DEFINITION_METHOD.WITHOUT_ROOT_IND;

/**
 * @author Alexandr Klimov
 */
public class TypeDiffService {
    static private Logger LOG = LoggerFactory.getLogger(TypeDiffService.class);

    /**
     * This method returns TypeDiffInfo object with information about changes in a processed type or NULL, if the type
     * has not been changed.<br/>
     *<br/>
     * Pecularities:
     * <ol>
     *     <li>
     *         This method compares content items(descriptors, indicators) and attributes of compared types but it does not compare types of
     *          elements and attributes in deep.
     *     </li>
     *     <li>
     *         A reference to a group its'n processed here because of comparing of groups is performed as standalone process.
     *     </li>
     * </ol>
     * <br/>
     * It may detect <strong>deleted elemetns/indicators/attributes/reference to a group</strong>, <strong>newly created ones</strong>,
     * <strong>element/attributes have got replaced type</strong> and <strong>element/attributes/reference to a group have got changes in attributes</strong>.<br/>
     *<br/>
     * For understanding indicators comparison see javadoc to {@link ru.aklimov.wsdlcomparator.domain.descriptors.IndicatorDescriptor#equals}.<br/>
     * <br/>
     * <strong>This method can't detect descriptors or attributes have got changed type</strong> because it does not perfom
     * deep compare of an element/attribute type.
     *
     * @see ru.aklimov.wsdlcomparator.DifferenceComputer#processAffectedOwners(ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo, java.util.Set)
     *
     * @param oldTd
     * @return TypeDiffInfo
     */
    @SuppressWarnings("unchecked")
    static public TypeDiffInfo compareComplexTypeWithDetails(TypeDescriptor newTd, TypeDescriptor oldTd){
        if(!newTd.isComplexType() || !oldTd.isComplexType() ){
            String msgErr = "compareComplexTypeWithDetails can process type descriptors for complex type only";
            msgErr += "\n newTd.isCopmplexType: " + newTd.isComplexType();
            msgErr += "\n other.isCopmplexType: " + oldTd.isComplexType();
            throw new IllegalArgumentException(msgErr);
        }

        if( !newTd.equals( oldTd ) ){
            throw new IllegalArgumentException("Method can not compare descriptors of diffirent types!");
        }


        //////////////// START COMPARE ///////////////////////////////////////////////////////
        if(LOG.isDebugEnabled()){
            String debugMsg = "\n\tSTART COMPLEX TYPE COMPARE\n\t"+newTd;
            LOG.debug(debugMsg);
        }

        Map<String, ChangeInfoDetails> changedAttrs = new HashMap<>();

        boolean baseTypeEq = false;
        {
            boolean baseTypeEqNotNullEq = (newTd.getBaseTypeQName()!=null && newTd.getBaseTypeQName().equals(oldTd.getBaseTypeQName()));
            boolean baseTypeBothNullEq = newTd.getBaseTypeQName()==null && newTd.getBaseTypeQName()==oldTd.getBaseTypeQName();
            baseTypeEq = baseTypeBothNullEq || baseTypeEqNotNullEq;
        }

        // simpleContent/complexContent tag
        boolean contentModelTypeEq = false;
        {
            boolean contentModelTypeNotNullEq = (newTd.getContentModelType()!=null && newTd.getContentModelType().equals(oldTd.getContentModelType()) );
            boolean contentModelTypeBothNull = (newTd.getContentModelType()==null && newTd.getContentModelType()==oldTd.getContentModelType());
            contentModelTypeEq = contentModelTypeNotNullEq || contentModelTypeBothNull;
        }

        // restriction/extension tag
        boolean contentTypeEq = false;
        {
            boolean contentTypeNotNullEq = (newTd.getComplexContentType()!=null && newTd.getComplexContentType().equals(oldTd.getComplexContentType()) );
            boolean contentTypeBothNull = (newTd.getComplexContentType()==null && newTd.getComplexContentType()==oldTd.getComplexContentType());
            contentTypeEq = contentTypeNotNullEq || contentTypeBothNull;
        }

        //Root group indicator type equality test
        //This indicator ma be defined both directly and indirectly by a re to a group.

        IndicatorDeclarationInfo indDeclarationInfo = getIndicatorDeclarationInfo(newTd, oldTd);


        //////////////// SEARCH CHANGED ELEMENTS ///////////////////////////////////////////////////////
        /////////// if indicators type is the same /////////////////////////////////////////////////////
        // Searching of changed elements is performed if and only if the root group indicator
        // has not been resolved through a refence to aq group.

        //A complex type may have not got any element at all, then it may have not got root indicator (seq. all, choice)
        // at all.
        Map<ParticleContent, ChangeInfoDetails> changedElements = new HashMap<>();
        ChangeInfoDetails rootGrpRefChangeInfoDetails = null;
        if( ! indDeclarationInfo.isNewRootGrpIndByRef &&
                ! indDeclarationInfo.isOldRootGrpIndByRef &&
                    indDeclarationInfo.indicatorTypeEq &&
                    ! indDeclarationInfo.isBothNull){
            IndicatorDiffService.indicatorCompare(newTd.getRootIndicator(), oldTd.getRootIndicator(), changedElements);

        } else if( indDeclarationInfo.isNewRootGrpIndByRef || indDeclarationInfo.isOldRootGrpIndByRef){
            rootGrpRefChangeInfoDetails = IndicatorDiffService.groupCompare(indDeclarationInfo.newRootGrpRef, indDeclarationInfo.oldRootGrpRef);

        }



        //////////////// Search CHANGED ATTRIBUTES ///////////////////////////////////////////////////////
        //////// Attributes comparing made by analogy with descriptors comparing //////////////////////////

        LOG.debug("Search changed attributes");
        Collection<AttributeDescriptor> attrDescrForDeepCompare = CollectionUtils.intersection(newTd.getAttributes(), oldTd.getAttributes());
        List<AttributeDescriptor> newTdAttrs = newTd.getAttributes();
        List<AttributeDescriptor> oldTdAttrs = oldTd.getAttributes();

        for(AttributeDescriptor attr : attrDescrForDeepCompare){
            AttributeDescriptor newTdAttr = newTdAttrs.get( newTdAttrs.indexOf(attr) );
            AttributeDescriptor otherAttr = oldTdAttrs.get( oldTdAttrs.indexOf(attr) );
            boolean attrTdEq = newTdAttr.getTypeDescr().equals(otherAttr.getTypeDescr());
            boolean attrAttrsEq = newTdAttr.attributeCompare(otherAttr);

            ChangeInfoDetails chngNfo = null;
            if(!attrTdEq && attrAttrsEq){
                chngNfo = new ChangeInfoDetails(REPLACED_TYPE, otherAttr.getTypeDescr());

            }else if(attrTdEq && !attrAttrsEq){
                chngNfo = new ChangeInfoDetails(CHANGE_IN_ATTRIBUTES, otherAttr.getTypeDescr());

            } else if(!attrTdEq && !attrAttrsEq){
                chngNfo = new ChangeInfoDetails(CHANGE_IN_ATTRIBUTES_AND_REPLACED_TYPE, otherAttr.getTypeDescr());

            }
            if(chngNfo!=null){
                changedAttrs.put(newTdAttr.getName(), chngNfo);
            }
        }

        //Search new attributes
        LOG.debug("Search new attributes");
        Collection<AttributeDescriptor> newAttrs = ListUtils.removeAll(newTd.getAttributes(), attrDescrForDeepCompare);
        for(AttributeDescriptor attr : newAttrs){
            changedAttrs.put(attr.getName(), new ChangeInfoDetails(NEW) );
        }

        //Search deleted attributes
        LOG.debug("Search deleted attributes");
        Collection<AttributeDescriptor> delAttrs = ListUtils.removeAll(oldTd.getAttributes(), attrDescrForDeepCompare);
        for(AttributeDescriptor attr : delAttrs){
            ChangeInfoDetails changeInfoDetails = new ChangeInfoDetails(DELETE);
            changeInfoDetails.setDelAttr(attr);
            changedAttrs.put( attr.getName(), changeInfoDetails);
        }

        /////////////////////////// COMPARE END /////////////////////////////////////////////////////////
        boolean rootGrpIndDefinitionMethodEq = (indDeclarationInfo.isNewRootGrpIndByRef == indDeclarationInfo.isNewRootGrpIndByRef);
        TypeDiffInfo resTdi = null;
        if( ! changedElements.isEmpty() ||
                ! changedAttrs.isEmpty() ||
                    ! contentModelTypeEq ||
                        ! contentTypeEq ||
                            ! indDeclarationInfo.indicatorTypeEq ||
                                ! rootGrpIndDefinitionMethodEq ||
                                    rootGrpRefChangeInfoDetails != null ||
                                    ! baseTypeEq){
            resTdi = new TypeDiffInfo(newTd);
            resTdi.setAffectedItems(changedElements);
            resTdi.setAffectedAttributes(changedAttrs);

            resTdi.setChangedBaseType(!baseTypeEq);
            if( ! baseTypeEq){
               resTdi.setOldBaseTd(oldTd.getBaseType());
            }

            resTdi.setChangedContentModelType(!contentModelTypeEq);
            if( ! contentModelTypeEq){
                resTdi.setOldContentModelType(oldTd.getContentModelType());
            }

            resTdi.setChangedContentType(!contentTypeEq);
            if( ! contentTypeEq){
                resTdi.setOldContentType(oldTd.getComplexContentType());
            }

            resTdi.setChangedRootIndicator(!indDeclarationInfo.indicatorTypeEq);
            if( ! indDeclarationInfo.indicatorTypeEq){
                resTdi.setOldIndicator(oldTd.getRootIndicatorType());
            }

            if( ! rootGrpIndDefinitionMethodEq ){
                resTdi.setRootGrpIndDefinitionMethodChanged(true);
                if(indDeclarationInfo.isOldRootGrpIndByRef){
                    resTdi.setOldGrpIndDefinitionMethod(BY_REF_TO_GROUP);
                } else if(indDeclarationInfo.oldRootIndDescr != null){
                    resTdi.setOldGrpIndDefinitionMethod(DIRECTLY);
                } else {
                    resTdi.setOldGrpIndDefinitionMethod(WITHOUT_ROOT_IND);
                }
            }

            if(rootGrpRefChangeInfoDetails != null){
                resTdi.getAffectedItems().put(indDeclarationInfo.newRootGrpRef, rootGrpRefChangeInfoDetails);
            }
        }

        if(LOG.isDebugEnabled()){
            String debugMsg = "\n\tEND COMPLEX TYPE COMPARE\n\t"+newTd+"\n\t";
            if(resTdi == null){
                debugMsg += "Any changes haven't been found\n";
            }
            LOG.debug(debugMsg);
        }

        return resTdi;
    }

    /**
     * This method returns TypeDiffInfo object with information about changes in a processed type or NULL, if the type
     * has not been changed.
     *
     * @param oldTd
     * @return TypeDiffInfo
     */
    public static TypeDiffInfo compareSimpleTypeWithDetails(TypeDescriptor newTd, TypeDescriptor oldTd){
        if(newTd.isComplexType() || oldTd.isComplexType()){
            String msgErr = "compareSimpleTypeWithDetails can process type descriptors for simple type only";
            msgErr += "\n newTd.isCopmplexType: " + newTd.isComplexType();
            msgErr += "\n other.isCopmplexType: " + oldTd.isComplexType();
            throw new IllegalArgumentException(msgErr);
        }

        boolean equals = newTd.equals(oldTd);
        if( !equals ){
            throw new IllegalArgumentException("Method can not compare descriptors of diffirent types!");
        }

        TypeDiffInfo diffNfo = null;

        boolean changedBaseType = false;
        boolean changedContentType = false;
        if(!newTd.isBaseXsdType() && equals){
            if(!newTd.getBaseType().equals(oldTd.getBaseType())){
                changedBaseType = true;

            }else {
                changedContentType = !( newTd.getSimpleContentType().equals(oldTd.getSimpleContentType()) );
                if(!changedContentType){
                    boolean facetsEq = true;
                    XmlSchemaFacet[] newFacets = newTd.getFacets();
                    XmlSchemaFacet[] oldFacets = oldTd.getFacets();
                    if(newFacets.length == oldFacets.length){
                        for(XmlSchemaFacet newFacet : newFacets){
                            boolean isFound=false;
                            for(XmlSchemaFacet oldFacet : oldFacets){
                                boolean clsEq = newFacet.getClass().equals(oldFacet.getClass());
                                boolean valEq = newFacet.getValue().equals(oldFacet.getValue());
                                if( clsEq && valEq ){
                                    isFound = true;
                                    break;
                                }
                            }
                            facetsEq = facetsEq && isFound;
                            if(!facetsEq){
                                break;
                            }
                        }
                    }
                    equals = facetsEq;

                }

            }

        }

        if(!equals || changedBaseType || changedContentType){
            diffNfo = new TypeDiffInfo(newTd);

            diffNfo.setChangedBaseType( changedBaseType );
            diffNfo.setOldBaseTd(oldTd.getBaseType());

            diffNfo.setChangedContentType( changedContentType );
            if(changedContentType){
                diffNfo.setOldContentType(oldTd.getSimpleContentType());
            }

        }

        return diffNfo;
    }


    private static IndicatorDeclarationInfo getIndicatorDeclarationInfo(final TypeDescriptor newTd, final TypeDescriptor oldTd){
        IndicatorDeclarationInfo info = new IndicatorDeclarationInfo();
        info.isNewRootGrpIndByRef = (newTd.getRootGroupRef() != null);
        info.isOldRootGrpIndByRef = (oldTd.getRootGroupRef() != null);


        if(info.isNewRootGrpIndByRef){
            info.newRootGrpRef = newTd.getRootGroupRef();
            info.newRootIndDescr = newTd.getRootGroupRef().getGroupDescr().getRootIndDescr();

        } else {
            info.newRootIndDescr = newTd.getRootIndicator();

        }

        if(info.isOldRootGrpIndByRef){
            info.oldRootGrpRef = oldTd.getRootGroupRef();
            info.oldRootIndDescr = oldTd.getRootGroupRef().getGroupDescr().getRootIndDescr();

        } else {
            info.oldRootIndDescr = oldTd.getRootIndicator();

        }

        Class<? extends XmlSchemaGroupParticle> newRootIndType = (info.newRootIndDescr != null)?info.newRootIndDescr.getType():null;
        Class<? extends XmlSchemaGroupParticle> oldRootIndType = (info.oldRootIndDescr != null)?info.oldRootIndDescr.getType():null;

        info.isBothNull = (newRootIndType==null && oldRootIndType==null);
        info.indicatorTypeEq = (newRootIndType == oldRootIndType ) || (newRootIndType != null && newRootIndType.equals(oldRootIndType) );

        return info;
    }


    ///
    /// INNER CLASSES ///
    ///


    private static class IndicatorDeclarationInfo{
        GroupReference newRootGrpRef;
        IndicatorDescriptor newRootIndDescr;
        boolean isNewRootGrpIndByRef;
        GroupReference oldRootGrpRef;
        IndicatorDescriptor oldRootIndDescr;
        boolean isOldRootGrpIndByRef;
        boolean indicatorTypeEq;
        boolean isBothNull;
    }

}
