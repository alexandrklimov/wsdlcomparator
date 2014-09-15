package ru.aklimov.wsdlcomparator.modelbuilders;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.ListUtils;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.*;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.MessagePartDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.WSMethodDescrTable;

import javax.xml.namespace.QName;
import java.util.*;

import static ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE;
import static ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable.*;

/**
 * @author Alexandr Klimov
 *
 * TODO: split this class
 */
public class ViewModelCreator {
    public static final String GROUP_TABLE_ID_PREFIX = "group_table|";
    public static final String TYPE_TABLE_ID_PREFIX = "type_table|";
    static private Logger log = LoggerFactory.getLogger(ViewModelCreator.class);

    static private final String ARG_ERR_MSG = "Either type descriptor or group descriptor must not be NULL!";


    /**
     * This method creates a set of view models by a type descriptors set and group descriptors set, without using any diff. info.
     *
     * @param typeDescriptors
     * @param deepCount
     * @return ModelBuildResult
     */
    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors,
                                             Set<GroupDescriptor> groupDescriptors,
                                             final boolean mergeWithBaseType,
                                             final boolean includeRefGroup,
                                             int deepCount){
        log.debug("--- START CREATE MODELS WITHOUT DIFF INFO ---\n");

        //These collections will be filled in cycles below.
        Set<TypeDescrTable> resTableSet = new HashSet<>();
        Set<GroupDescrTable> resGroupSet = new HashSet<>();

        try{
            for(TypeDescriptor td : typeDescriptors){
                ModelBuildCntx cntx = new ModelBuildCntx(td, null, null, null, resTableSet, resGroupSet, mergeWithBaseType, includeRefGroup, deepCount, true);
                createModelByTd(cntx);
            }
            for(GroupDescriptor gd : groupDescriptors){
                ModelBuildCntx cntx = new ModelBuildCntx(null, gd, null, null, resTableSet, resGroupSet, mergeWithBaseType, includeRefGroup, deepCount, true);
                createModelByGd(cntx);
            }
        }catch(Exception ex){
            log.error("Error during #createModelBySet", ex);
            throw new RuntimeException(ex);
        }

        return new ModelBuildResult(resTableSet, resGroupSet);
    }


    /**
     * This method returns a set of TypeDescrTable instances.
     * Each table has got an id, that made by following rules:<br/>
     * it's a string value of a type QName or <strong>"owner_type_qname-type"+"owner_element_name-element"+"_anon_type"</strong>,
     * if a described type is anonymous
     *
     * @param typeDiffSet
     * @param groupDiffSet
     * @param mergeWithBaseType
     * @param includeRefGroup
     * @param deepCount
     * @return
     */
    public ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet,
                                                        Set<GroupDiffInfo> groupDiffSet,
                                                        boolean mergeWithBaseType,
                                                        final boolean includeRefGroup,
                                                        int deepCount){
        log.debug("--- START CREATE MODEL ---\n");

        //These collections will be filled in cycles below.
        Set<TypeDescrTable> resTableSet = new HashSet<>();
        Set<GroupDescrTable> resGroupSet = new HashSet<>();

        final Map<TypeDescriptor, TypeDiffInfo> typeDfMap = ImmutableMap.copyOf(typeDiffSetAsMap(typeDiffSet));
        final Map<GroupDescriptor, GroupDiffInfo> groupDfMap = ImmutableMap.copyOf(groupDiffSetAsMap(groupDiffSet));
        Set<TypeDescrTable> resSet = new HashSet<>();
        try{
            for(TypeDiffInfo tdf : typeDiffSet){
                log.debug("\n### Processing TypeDiffInfo: "+tdf+" ###\n");
                TypeDescriptor tdForProc = tdf.getTypeDescr();
                ModelBuildCntx cntx = new ModelBuildCntx(tdForProc, null, typeDfMap, groupDfMap, resTableSet, resGroupSet, mergeWithBaseType, includeRefGroup, deepCount, true);
                createModelByTd(cntx);
            }
            for(GroupDiffInfo gdf : groupDiffSet){
                log.debug("\n### Processing GroupDiffInfo: "+gdf+" ###\n");
                GroupDescriptor grpDescr = gdf.getGrpDescr();
                ModelBuildCntx cntx = new ModelBuildCntx(null, grpDescr, typeDfMap, groupDfMap, resTableSet, resGroupSet, mergeWithBaseType, includeRefGroup, deepCount, true);
                createModelByGd(cntx);
            }
        }catch(Exception ex){
            log.error("Error during #createModelByDiffInfoSet", ex);
            throw new RuntimeException(ex);
        }

        return new ModelBuildResult(resTableSet, resGroupSet);
    }

    /**
     * This method should be used after both types models and groups models have been created, because this method uses these results.
     *
     * TODO: Some code doubling is in createWSMethodModelByWSMethodDescr method should be reduced.
     *
     * @param wsMethodsDiffs
     * @param typeTbls may be enriched by new type tables if necessary
     * @param groupTbls may be enriched by new group tables if necessary
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @param deepCount
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<WSMethodDiffInfo> wsMethodsDiffs,
                                                                 final Set<TypeDescrTable> typeTbls,
                                                                 final Set<GroupDescrTable> groupTbls,
                                                                 final boolean includeRefGroup,
                                                                 final boolean mergeWithBaseType,
                                                                 final int deepCount){
        Set<WSMethodDescrTable> resSet = new HashSet<>();

        //A single context will be used for message parts processing.
        //It's possible because the context acts as arguments and processing results container irrelative of
        //any concrete processed message part or a type of a processed part.
        final ModelBuildCntx cntx = new ModelBuildCntx(null, null, null, null, typeTbls, groupTbls, mergeWithBaseType, includeRefGroup, deepCount, true);

        for(WSMethodDiffInfo diffWSMethod : wsMethodsDiffs){
            WSMethodDescr method = diffWSMethod.getWsMethodDescr();

            WSMethodDescrTable tmpWsMethTbl = new WSMethodDescrTable();
            tmpWsMethTbl.setMethodName( method.getMethodName() );
            tmpWsMethTbl.setChangeType( diffWSMethod.getChangeType().toString() );

            List<WSMethodDescr.MessagePartDescr> inputMessage = method.getInputMessage();
            if( ! inputMessage.isEmpty()){
                List<MessagePartDescrTable> messagePartDescrTableLst = createMsgPartTables(diffWSMethod, inputMessage, cntx);
                tmpWsMethTbl.setInputMessage(messagePartDescrTableLst);

            }

            List<WSMethodDescr.MessagePartDescr> outputMessage = method.getOutputMessage();
            if( ! outputMessage.isEmpty()){
                List<MessagePartDescrTable> messagePartDescrTableLst = createMsgPartTables(diffWSMethod, outputMessage, cntx);
                tmpWsMethTbl.setOutputMessage(messagePartDescrTableLst);

            } else {
                if( WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.RESPONSE_DEL.equals(diffWSMethod.getChangeType()) ){
                    //Build tables for all parts of a deleted message
                    List<MessagePartDescrTable> deletedMsgPartTables = new LinkedList<>();
                    for(WSMethodDescr.MessagePartDescr deletedPartDescr : diffWSMethod.getDeletedOutputMessage()){
                        deletedMsgPartTables.add( MessagePartDescrTableBuilder.createTableByDescr(deletedPartDescr, cntx) );
                    }
                    tmpWsMethTbl.setDeletedOutputMessage(deletedMsgPartTables);
                }
            }

            resSet.add(tmpWsMethTbl);
        }

        return resSet;
    }


    private List<MessagePartDescrTable> createMsgPartTables(WSMethodDiffInfo diffWSMethod, List<WSMethodDescr.MessagePartDescr> inputMessage, ModelBuildCntx cntx) {
        List<MessagePartDescrTable> messagePartDescrTableLst = new LinkedList<>();

        for(WSMethodDescr.MessagePartDescr msgPartDescr : inputMessage){
            final MessagePartDescrTable table;
            //Changes for message part is processed may be found
            if( diffWSMethod.getChangedInMsgParts().containsKey(msgPartDescr) ){
                table = MessagePartDescrTableBuilder.createTableByDiffInfo(diffWSMethod.getChangedInMsgParts().get(msgPartDescr), cntx);
            } else {
                table = MessagePartDescrTableBuilder.createTableByDescr(msgPartDescr, cntx);
            }

            messagePartDescrTableLst.add(table);
        }

        return messagePartDescrTableLst;
    }


    /**
     * This method should be used after both types models and groups models have been created, because this method uses these results.
     *
     * @param wsMethodDescr
     * @param typeTbls may be enriched by new type tables if necessary
     * @param groupTbls may be enriched by new group tables if necessary
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @param deepCount
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr,
                                                                      final Set<TypeDescrTable> typeTbls,
                                                                      final Set<GroupDescrTable> groupTbls,
                                                                      final boolean includeRefGroup,
                                                                      final boolean mergeWithBaseType,
                                                                      final int deepCount){
        Set<WSMethodDescrTable> resSet = new HashSet<>();

        //A single context will be used for message parts processing.
        //It's possible because the context acts as arguments and processing results container irrelative of
        //any concrete processed message part or a type of a processed part.
        final ModelBuildCntx cntx = new ModelBuildCntx(null, null, null, null, typeTbls, groupTbls, mergeWithBaseType, includeRefGroup, deepCount, true);

        for(WSMethodDescr methodDescr : wsMethodDescr){
            WSMethodDescrTable tmpWsMethTbl = new WSMethodDescrTable();
            tmpWsMethTbl.setMethodName(methodDescr.getMethodName());

            List<WSMethodDescr.MessagePartDescr> inputMessage = methodDescr.getInputMessage();
            if( ! inputMessage.isEmpty()){
                List<MessagePartDescrTable> messagePartDescrTableLst = new LinkedList<>();
                for(WSMethodDescr.MessagePartDescr msgPartDescr : inputMessage){
                    MessagePartDescrTable table = MessagePartDescrTableBuilder.createTableByDescr(msgPartDescr, cntx);
                    messagePartDescrTableLst.add(table);
                }

            }

            List<WSMethodDescr.MessagePartDescr> outputMessage = methodDescr.getOutputMessage();
            if( ! outputMessage.isEmpty()){
                List<MessagePartDescrTable> messagePartDescrTableLst = new LinkedList<>();
                for(WSMethodDescr.MessagePartDescr msgPartDescr : outputMessage){
                    MessagePartDescrTable table = MessagePartDescrTableBuilder.createTableByDescr(msgPartDescr, cntx);
                    messagePartDescrTableLst.add(table);
                }

            }

            resSet.add(tmpWsMethTbl);
        }

        return resSet;
    }


    private Map<TypeDescriptor, TypeDiffInfo> typeDiffSetAsMap(Set<TypeDiffInfo> infoSet){
        Map<TypeDescriptor, TypeDiffInfo> resMap = new HashMap<>();
        for(TypeDiffInfo df : infoSet){
            resMap.put(df.getTypeDescr(), df);
        }
        return resMap;
    }


    private Map<GroupDescriptor, GroupDiffInfo> groupDiffSetAsMap(Set<GroupDiffInfo> infoSet){
        Map<GroupDescriptor, GroupDiffInfo> resMap = new HashMap<>();
        for(GroupDiffInfo df : infoSet){
            resMap.put(df.getGrpDescr(), df);
        }
        return resMap;
    }


    /**
     * This function creates table model by a TypeDescriptor and may use a TypeDiffInfo for supporting.<br/>
     * This function creates table models of base types by processing a TypeDescriptor of base type in recursive manner.<br/>
     * Also one creates table models of descriptors type by {@link ViewModelCreator#buildRootRow buildRootRow}
     * function calls.
     * <br/>
     * <br/>
     * This function may be used for creating a table model by TypeDescriptor only, without any TypeDiffInfo.
     * This capability may be used for creating a table model for unmodified types that are for additional information.
     * In this case <strong>isChanged</strong> field of a table model is set to <strong>false</strong>.
     * <br/>
     *<br/>
     * A parents three of the processed type may be very deep.<br/>
     * For example: 5 levels for a type only plus 5 levels for each type of an element.<br/>
     * This amount of information may be very redundant for us. All changed/affected type are in source set already and
     * we need obtain information about unchanged types of descriptors in some restriction mode.
     * <br/>
     * <br/>
     * For example we need to obtain a table view of a element type(typA) only without any table view of descriptors other
     * types (typB, typC).
     * <BR/><BR/>
     *----------------- <BR/>
     * SOME_TYPE <BR/>
     *  ELEM_A[typA] <BR/>
     * <BR/><BR/>
     * typA <BR/>
     *  ELEM_B[typB] <BR/>
     * <BR/><BR/>
     * typB <BR/>
     *  ELEM_C[typC] <BR/>
     * ----------------
     * <BR/><BR/>
     *
     * deepCount parameter is responsible to this behaviour.
     * More about deepCount see {@link ViewModelCreator#buildRootRow buildRootRow}
     *
     * @param cntx
     */
    public static void createModelByTd(final ModelBuildCntx cntx){
        TypeDescriptor td = cntx.getTd();

        if(td == null){
            throw new IllegalArgumentException("#createModelByTd: type descriptor is NULL!");
        }

        if( log.isDebugEnabled() ){
            log.debug("\n--- CREATE MODEL BY TD [deepCount="+cntx.getDeepCount()+"] [" + td.getId() + "]---\n");
        }

        if(searchTypeTableById(td.getId(), cntx.getTableTypeSet()) != null){
            log.debug("A table for " + td.getId() + " type is created already.");
            return;
        }

        //A tables for base XSD types will not be created (f.e. string, decimal and so on)
        if(!td.isBaseXsdType()){
            log.debug("isBaseXsdType = FALSE");

            TypeDescrTable table = new TypeDescrTable();
            table.setId( buildTypeDescrTableId(td) );
            table.setTitle(createTableTitle(td));

            table.setComplexType( td.isComplexType() );
            table.setAnonymous( td.isAnonymous() );
            table.setTypeNamespace(td.getNamespaceURI());
            if( td.isAnonymous() ){
                //if the type is not a one of a root element
                TypeDescriptor ownTd = td.getOwnerInfoLst().get(0).getTypeDescriptor();
                String ownElemName = td.getOwnerInfoLst().get(0).getElemName();
                if(ownTd!=null){
                    table.setOwnerTableId( buildTypeDescrTableId(ownTd) );
                    table.setOwnerElementName( ownElemName );
                    table.setOwnerTableTitle( createTableTitle(ownTd));
                }

            }

            if(td.getBaseTypeQName()!=null){
                boolean baseIsBaseXsd = td.getBaseType().isBaseXsdType();
                table.setBaseTypeIsBaseXsdType(baseIsBaseXsd);
                table.setBaseTypeTableId( td.getBaseTypeQName().toString() );
                table.setBaseTypeName( td.getBaseTypeQName().getLocalPart() );
            }

            //Sorting annotation for appropriated collections
            for(AnnotationDescriptor annotDescr : td.getAnnotations()){
                AnnotationDescriptor.ANNOTATION_TYPE annotType = annotDescr.getAnnotationType();
                if( AnnotationDescriptor.ANNOTATION_TYPE.APPINFO.equals(annotType) ){
                    table.getAppinfos().add(annotDescr.getValue());
                }else if( AnnotationDescriptor.ANNOTATION_TYPE.DOCUMENTATION.equals(annotType) ){
                    table.getDocumentations().add(annotDescr.getValue());
                }
            }

            //TypeDiffInfo can be null if we are processing a type that has not been modified
            TypeDiffInfo df = cntx.getDfTypeMap().get(td);
            if(df != null){
                table.setNew( df.isNew() );
                table.setRemoved(df.isRemoved());
                if( ! df.isNew() &&
                    ! df.isRemoved()){
                    table.setChanged(true);
                }
                table.setChangedBaseType( df.isChangedBaseType() );
                table.setAffectedByBaseChanges( df.isAffectedByBaseChanges() );
            }

            if(table.isComplexType()){
                log.debug("isComplexType = TRUE");
                table.setComplexContentType( getComplexContentType(td) );
                table.setRootIndicatorType(getRootIndicatorType(td));

                //Including base class information should not be performed if a complexType has the RESTRICTION content model.
                if( COMPLEX_CONTENT_TYPE.RESTRICTION != table.getComplexContentType() ){
                    table.setBaseTypeIncluded( cntx.isMergeWithBaseType() );
                }

                //Put a table into processed table set, before process base type and descriptors types, for preventing double processing
                //in a future possible recursive call
                log.debug("Add table [id="+table.getId()+"] to tableSet");
                cntx.getTableTypeSet().add(table);

                TableRow rootRow = buildRootRow(cntx);
                table.setRootRow(rootRow);
                List<TableRow> attrRows = getCurrentTypeAttrRows(cntx.switchContextForTdAttrs());
                table.setAttrRows(attrRows);

            } else {
                log.debug("isComplexType = FALSE");

                XmlSchemaFacet[] facets = td.getFacets();
                if(facets!=null){
                    List<String[]> facetDescr = getFacetsDescriptions(facets);
                    table.setFacets(facetDescr);
                }


                table.setSimpleContentType( getSimpleContentType(td) );
                //Put a table into processed table set, before process base type, for preventing double processing
                //in a future possible recursive call
                log.debug("Add table [id=" + table.getId() + "] to tableSet");
                cntx.getTableTypeSet().add(table);

            }

            //base type processing
            if(td.getBaseType() != null){
                createModelByTd( cntx.switchContextForNewTd(td.getBaseType(), cntx.getDeepCount()) );
            }

        }

    }


    /**
     * This function creates table model by a TypeDescriptor and GroupDescriptor start from a GroupDescriptor.<br/>
     * It works by analogy with {@link ViewModelCreator#createModelByTd(ru.aklimov.wsdlcomparator.domain.tblmodel.ModelBuildCntx)}.
     *
     * @param cntx
     */
    public static void createModelByGd(ModelBuildCntx cntx){
        GroupDescriptor gd = cntx.getGd();

        if(gd == null){
            throw new IllegalArgumentException("#createModelByGd: group descriptor is NULL!");
        }

        if( log.isDebugEnabled() ){
            log.debug("\n--- CREATE MODEL BY GROUP DESCRIPTOR [deepCount="+cntx.getDeepCount()+"] [" + gd.getId() + "]---\n");
        }

        if(searchGroupTableById(buildGroupDescrTableId(gd), cntx.getTableGroupSet()) != null){
            log.debug("A table for " + gd.getId() + " group is created already.");
            return;
        }

        GroupDescrTable table = new GroupDescrTable();
        table.setId( buildGroupDescrTableId(gd) );
        table.setName(gd.getName());
        table.setNamespace(gd.getQName().getNamespaceURI());
        table.setTitle(createGroupTitle(gd));
        //GroupDiffInfo can be null if we are processing a group that has not been modified
        GroupDiffInfo df = cntx.getDfGroupMap().get(gd);
        if(df != null){
            table.setNew( df.isNew() );
            table.setRemoved( df.isRemoved() );
            if ( ! df.isNew() &&
                ! df.isRemoved()) {
                table.setChanged(true);
            }
        }

        //For prevent double processing in a future possible recursion call
        cntx.getTableGroupSet().add(table);

        TableRow rootRow = buildRootRow(cntx);
        table.setRootRow(rootRow);

    }


    /**
     * This function returns facets description as a map(name->value).
     *
     *
     * @param facets
     * @return List<String[]>
     */
    private static List<String[]> getFacetsDescriptions(XmlSchemaFacet[] facets){
        List<String[]> descr = new LinkedList<>();
        if(facets==null){
            return descr;
        }
        for(XmlSchemaFacet facet : facets){
            String facetClsName = facet.getClass().getSimpleName();

            String facetName = facetClsName.replace("XmlSchema", "").replace("Facet","");
            facetName = Character.toLowerCase(facetName.charAt(0)) + facetName.substring(1);
            descr.add( new String[]{facetName, facet.getValue().toString() });
            log.debug("Facet class name: " + facetClsName + "; Facet name: " + descr);
        }
        return descr;
    }

    /**
     * This function returns <strong>"owner_type_qname-type|"+"owner_element_name-element"+"|of_anon_type"</strong>,
     * if a described type is anonymous
     *
     * @param td
     * @return String
     */
     private static String createTableTitle(TypeDescriptor td){
        QName typeQName = td.getQName();
        if(typeQName!=null){
            return typeQName.getLocalPart();
        } else {
            return "Anonymous type";
        }
    }

    /**
     * @param gd
     * @return String
     */
    private static String createGroupTitle(GroupDescriptor gd){
        QName typeQName = gd.getQName();
        return typeQName.getLocalPart();
    }

    private static SIMPLE_CONTENT_TYPE getSimpleContentType(TypeDescriptor td){
        if(td.isComplexType()){
            throw new IllegalArgumentException("This function can't process complex type!");
        }

        Class<? extends XmlSchemaSimpleTypeContent> simpleContentType = td.getSimpleContentType();
        String contentTypeStr = simpleContentType.getName().replace("XmlSchemaSimpleType", "");

        switch (contentTypeStr){
            case "Union": return SIMPLE_CONTENT_TYPE.UNION;
            case "List": return SIMPLE_CONTENT_TYPE.LIST;
            case "Restriction": return SIMPLE_CONTENT_TYPE.RESTRICTION;
            default: return null;
        }
    }

    private static COMPLEX_CONTENT_TYPE getComplexContentType(TypeDescriptor td){
        if(!td.isComplexType()){
            throw new IllegalArgumentException("This function can't process simple type!");
        }

        COMPLEX_CONTENT_TYPE res = null;
        if(td.getComplexContentType() != null){
            Class<? extends XmlSchemaContent> complexContentType = td.getComplexContentType();
            String contentTypeStr = complexContentType.getName().replace("XmlSchemaComplexContent", "");

            switch (contentTypeStr){
                case "Extension":
                    res = COMPLEX_CONTENT_TYPE.EXTENSION;
                    break;
                case "Restriction":
                    res = COMPLEX_CONTENT_TYPE.RESTRICTION;
                    break;
            }
        }

        return res;
    }

    private static INDICATOR_TYPE getRootIndicatorType(TypeDescriptor td){
        if(!td.isComplexType()){
            throw new IllegalArgumentException("This function can't process simple type!");
        }

        INDICATOR_TYPE res = null;
        //An indicator may be equals null if a complex type has attributes only
        if(td.getRootIndicatorType()!=null){
            Class<? extends XmlSchemaGroupParticle> indicatorType = td.getRootIndicatorType();
            String indicatorTypeStr = indicatorType.getName().replace("XmlSchema","");

            switch (indicatorTypeStr){
                case "All":
                    res = INDICATOR_TYPE.ALL;
                    break;
                case "Choice":
                    res = INDICATOR_TYPE.CHOICE;
                    break;
                case "Sequence":
                    res = INDICATOR_TYPE.SEQUENCE;
                    break;
            }
        }
        return res;
    }


    ///////////////// ELEMENTS/ATTRIBUTES PROCESSING FUNCTIONS /////////////////////////////////////////////////

    /**
     * Return a rows tree for the processed type/group only without any analysing or merging operations
     * for its both base type and referenced group.<br/>
     * This tree is built either by root type/group indicator or by root group reference.<br/>
     *<br/>
     * This method may be used for row collection building of a type that hasn't got any changes - an ordinary type.<br/>
     * In this case an appropriated TypeDiffInfo for the TypeDescription object is absentee. This fact should be taken in account.<br/>
     * <br/>
     * Row of table may describes either an element or a nested particle or a reference to a group.<br/>
     * <br/>
     * A counter of deep isn't taken in account if a reference to a group is processed. A group contains items that are
     * a part of processed type, in a certain sense. If we don't build a table of some GroupDescriptor then we cut a part of
     * type as though.
     *
     * @param cntx
     * @return
     */
    private static TableRow buildRootRow(final ModelBuildCntx cntx){
        TypeDescriptor td = cntx.getTd();
        GroupDescriptor gd = cntx.getGd();

        if( (td == null && gd == null) || (td != null && gd != null)){
            throw new IllegalArgumentException("Either a type descriptor or a group descriptor must be not NULL!");
        }

        if(td != null){
            log.debug("Get rows for type "+td+"; deepCount="+cntx.getDeepCount());
        } else {
            log.debug("Get rows for group "+gd+"; deepCount="+cntx.getDeepCount());
        }


        IndicatorDescriptor rootInd = null;
        GroupReference rootGroupRef = null;
        if(gd != null){
            //If the processed group hasn't any root indicator (sequence, choice or all) then return empty TableRow;
            rootInd = gd.getRootIndDescr();
            if(rootInd == null){
                return new TableRow();
            }

        } else {
            //Processed type may haven't any root group indicator but have reference to a group instead
            rootInd = td.getRootIndicator();
            if(rootInd == null){
                rootGroupRef = td.getRootGroupRef();
                if(rootGroupRef == null){
                    return new TableRow();
                }
            }

        }

        Set<TypeDescriptor> typesForPostProcessing = new HashSet<>();
        Set<GroupDescriptor> groupsForPostProcessing = new HashSet<>();
        TableRow resTableRow = null;
        if(rootInd != null){
            //If descriptors have been removes they are in affectedElements collection only.
            //We should extract they from that collection and add to the rows collection
            //todo: insert deleted element more nearly to old position. At this time it is inserted at the end of elem. list.
            if(cntx.getDfTypeMap().get(td)!=null){
                TypeDiffInfo df = cntx.getDfTypeMap().get(td);
                Collection<ChangeInfoDetails> changes = df.getAffectedItems().values();
                //todo: iterate by keys
                for(ChangeInfoDetails changeInfo : changes){
                    if( ELEM_OR_ATTR_CHANGE_TYPE.DELETE.equals( changeInfo.getChangeType() ) ){
                        //todo: a type is changed here. It's wrong. Propose an algorithm that does not change a root indicator.
                        if( changeInfo.getDelElem() != null){
                            rootInd.getItems().add(changeInfo.getDelElem());
                        } else if( changeInfo.getDelIndicator() != null){
                            IndicatorDescriptor deletedInd = changeInfo.getDelIndicator();
                            rootInd.getItems().add(deletedInd);
                        }

                    }
                }
            }

            resTableRow = getIndicatorDescrRow(rootInd, typesForPostProcessing, groupsForPostProcessing, cntx);

        } else {
            ChangeInfoDetails chid = getChngNfoDetailsInParentScope(rootGroupRef, td, gd, cntx.getDfTypeMap(), cntx.getDfGroupMap());
            ElemAttrDescrRow groupRefDescrRow = getGroupRefDescrRow(rootGroupRef, groupsForPostProcessing, chid, cntx);
            resTableRow = new TableRow(groupRefDescrRow);
            resTableRow.setRootGroupRefRow(true);
        }

        //Post processing
        //Here is processing of element types and referenced groups - for each type and group a table will be created
        if(cntx.getDeepCount() > 0){
            log.debug("Type descriptors for post-processing\n"+typesForPostProcessing+"\ndeepCount="+cntx.getDeepCount());
            for(TypeDescriptor typeForDefProc : typesForPostProcessing){
                createModelByTd( cntx.switchContextForNewTd(typeForDefProc, cntx.getDeepCount() - 1) );
            }
        }
        log.debug("Group descriptors for post-processing\n"+groupsForPostProcessing+"\ndeepCount="+cntx.getDeepCount());
        for(GroupDescriptor groupForDefProc : groupsForPostProcessing){
            createModelByGd( cntx.switchContextForNewGd(groupForDefProc, cntx.getDeepCount()) );
        }

        return resTableRow;
    }


    /**
     * This function return a row describes an IndicatorDescription(sequence, choice, all).<br/>
     * This goes into a recursion if the processed indicator contains nested a indicator(s).
     *
     * @param processedIndicator
     * @param typesForPostProcessing a collection is filled with TypeDescriptor instances which should be processed later.
     * @param groupsForPostProcessing a collection is filled with GroupDescriptor instances which should be processed later.
     * @param cntx
     * @return - a TableRow instance contains information about an indicator.
     */
    private static TableRow getIndicatorDescrRow(IndicatorDescriptor processedIndicator,
                                          Set<TypeDescriptor> typesForPostProcessing,
                                          Set<GroupDescriptor> groupsForPostProcessing,
                                          ModelBuildCntx cntx){
        if( log.isDebugEnabled() ){
            StringBuilder sb = new StringBuilder();
            sb.append("Run getIndicatorDescrRow\n");
            sb.append("\tprocessedIndicator: " + processedIndicator.getType().getName() + "\n");
            sb.append("\tprocessedIndicator id: " + processedIndicator.getId() + "\n");
            if(cntx.getTd() != null){
                sb.append("\ttypeDescriptor: " + cntx.getTd().getId() + "\n");
            } else {
                sb.append("\tgroupDescriptor: " + cntx.getGd().getId() + "\n");
            }
            log.debug(sb.toString());
        }

        TypeDescriptor td = cntx.getTd();
        GroupDescriptor gd = cntx.getGd();
        Map<TypeDescriptor, TypeDiffInfo> dfTypeMap = cntx.getDfTypeMap();
        Map<GroupDescriptor, GroupDiffInfo> dfGroupMap = cntx.getDfGroupMap();

        IndicatorDescrRow indDescrRow = new IndicatorDescrRow();
        TableRow resTableRow = new TableRow(indDescrRow);
        resTableRow.setIndicatorDescrRow(indDescrRow);

        indDescrRow.setId( processedIndicator.getId() );
        indDescrRow.setRoot( processedIndicator.getParentChain().isEmpty() );
        indDescrRow.setMaxOccurs(processedIndicator.getMaxOccurs());
        indDescrRow.setMinOccurs(processedIndicator.getMinOccurs());
        indDescrRow.setUnbounded( processedIndicator.isMaxOccUnbound() );
        indDescrRow.setIndicatorName( getIndicatorName(processedIndicator.getType()) );

        //Check the indicator changes
        ChangeInfoDetails processedIndChngInfoDetails = null;
        processedIndChngInfoDetails = getChngNfoDetailsInParentScope(processedIndicator, td, gd, dfTypeMap, dfGroupMap);

        if (processedIndChngInfoDetails != null) {
            if( log.isDebugEnabled() ){
                log.debug("Change type is " + processedIndChngInfoDetails.getChangeType());
            }
            switch( processedIndChngInfoDetails.getChangeType() ){
                case  NEW:
                    indDescrRow.setNew(true);
                    break;

                case DELETE:
                    indDescrRow.setDeleted(true);
                    break;

                case CHANGE_IN_ATTRIBUTES:
                    indDescrRow.setChangedInAttributes(true);
                    for(AttributeDescriptor changedAttrDescr : processedIndChngInfoDetails.getChangedAttributes()){
                        indDescrRow.getChangedAttributesNames().add( changedAttrDescr.getName() );
                    }
                    break;

                case CHANGE_IN_CONTENT:
                    indDescrRow.setChangedInContent(true);
                    break;

                case CHANGE_IN_CONTENT_AND_ATTR:
                    indDescrRow.setChangedInAttributes(true);
                    indDescrRow.setChangedInContent(true);
                    for(AttributeDescriptor changedAttrDescr : processedIndChngInfoDetails.getChangedAttributes()){
                        indDescrRow.getChangedAttributesNames().add( changedAttrDescr.getName() );
                    }
                    break;

                default:
                    throw new RuntimeException("Unknown indicator change type. " + processedIndChngInfoDetails.getChangeType() );
            }
        }


        //Process indicator content
        List<ParticleContent> itemDescrForProcessing = processedIndicator.getItems();
        for(ParticleContent particleContent : itemDescrForProcessing){
            //Item may be either an element, or a particle, or a group reference
            if( particleContent instanceof ElementDescriptor){
                log.debug("Build a new row for an ElementDescriptor instance.");
                ChangeInfoDetails chid = getChngNfoDetailsInParentScope(particleContent, td, gd, dfTypeMap, dfGroupMap);
                ElemAttrDescrRow elemDescrRow = buildElemDescrRow((ElementDescriptor) particleContent, typesForPostProcessing, cntx);
                indDescrRow.getContentRows().add( new TableRow(elemDescrRow) );

            }else if(particleContent instanceof IndicatorDescriptor){
                log.debug("Build a new row for an IndicatorDescriptor instance.");
                TableRow nestedIndicatorDescrTableRow = getIndicatorDescrRow((IndicatorDescriptor) particleContent, typesForPostProcessing, groupsForPostProcessing, cntx);
                indDescrRow.getContentRows().add( nestedIndicatorDescrTableRow );

            }else if(particleContent instanceof GroupReference){
                log.debug("Build a new row for an GroupReference instance.");
                ChangeInfoDetails chid = getChngNfoDetailsInParentScope(particleContent, td, gd, dfTypeMap, dfGroupMap);
                ElemAttrDescrRow groupDescrRow = getGroupRefDescrRow((GroupReference) particleContent, groupsForPostProcessing, chid, cntx);
                indDescrRow.getContentRows().add( new TableRow(groupDescrRow) );
            }

        }

        return resTableRow;
    }


    /**
     * This function builds an ElemAttrDescrRow by given ElementDescriptor and enriches a set of types for post-processing
     * (creating table models)
     *
     * @param ed - processed element descriptor
     * @param typesForPostProcessing - enriched set of TypeDescriptor-s for post-processing
     * @param cntx
     * @return ElemAttrDescrRow
     */
    private static ElemAttrDescrRow buildElemDescrRow(ElementDescriptor ed,
                                           Set<TypeDescriptor> typesForPostProcessing,
                                           ModelBuildCntx cntx){
        log.debug("\tbuildElemDescrRow | element"+ed.getName());
        ElemAttrDescrRow elemDescrRow = new ElemAttrDescrRow();
        TypeDescriptor elemTd = ed.getTypeDescr();

        elemDescrRow.setName(ed.getName());

        if(!elemTd.isBaseXsdType()){
            //String tableId = createTableId(elemTd);
            if(elemTd.isAnonymous()){
                elemDescrRow.setTypeName("anonymous type");
            } else {
                elemDescrRow.setTypeName(elemTd.getQName().getLocalPart());
                elemDescrRow.setTypeNamespace(elemTd.getQName().getNamespaceURI());
            }
            elemDescrRow.setTypeId( elemTd.getId() );
            elemDescrRow.setTypeIsBaseXSD(false);
            if( findTypeDescrTable(elemTd.getId(), cntx.getTableTypeSet()) == null ){
                typesForPostProcessing.add(elemTd);
            }

        } else {
            elemDescrRow.setTypeName(elemTd.getName());
            elemDescrRow.setTypeIsBaseXSD(true);

        }

        String cardinality = getCardinalityCellVal(ed.getMinOccurs(), ed.getMaxOccurs(), ed.isMaxOccUnbound());
        elemDescrRow.setCardinality(cardinality);
        elemDescrRow.setNillable(String.valueOf(ed.isNillable()));

        //Checking if the element is changed
        TypeDiffInfo df = cntx.getDfTypeMap().get(cntx.getTd());
        //TypeDiffInfo may be null if the type has not been changed
        if(df != null){
            ChangeInfoDetails changeInfoDetails = df.getAffectedItems().get(ed);
            if(changeInfoDetails != null){
                log.debug("ChangeInfo is found.");
                switch( changeInfoDetails.getChangeType() ){
                    case CHANGE_IN_TYPE:
                        elemDescrRow.setChangedInType(true);
                        break;
                    case NEW:
                        elemDescrRow.setNew(true);
                        break;
                    case DELETE:
                        elemDescrRow.setDeleted(true);
                        break;
                    case REPLACED_TYPE:
                        elemDescrRow.setReplacedType(true);
                        break;
                    case CHANGE_IN_ATTRIBUTES:
                        elemDescrRow.setChangeInAttributes(true);
                        break;
                    case CHANGE_IN_ATTRIBUTES_AND_CHANGE_IN_TYPE:
                        elemDescrRow.setChangeInAttributes(true);
                        elemDescrRow.setChangedInType(true);
                        break;
                    case CHANGE_IN_ATTRIBUTES_AND_REPLACED_TYPE:
                        elemDescrRow.setChangeInAttributes(true);
                        elemDescrRow.setReplacedType(true);
                        break;
                }
            }
        }

        //Set default and fixed values if defined
        elemDescrRow.setDefaultValue(ed.getDefaultVal());
        elemDescrRow.setFixedValue(ed.getFixedVal());

        //Sorting annotation for appropriated collections
        for(AnnotationDescriptor annotDescr : ed.getAnnotations()){
            AnnotationDescriptor.ANNOTATION_TYPE annotType = annotDescr.getAnnotationType();
            if( AnnotationDescriptor.ANNOTATION_TYPE.APPINFO.equals(annotType) ){
                elemDescrRow.getAppinfos().add(annotDescr.getValue());
            }else if( AnnotationDescriptor.ANNOTATION_TYPE.DOCUMENTATION.equals(annotType) ){
                elemDescrRow.getDocumentations().add(annotDescr.getValue());
            }
        }

        return elemDescrRow;
    }


    /**
     * This function builds a row describes a reference to a group
     *
     * @param gf
     * @param groupsForPostProcessing
     * @param chid
     * @param cntx
     * @return
     */
    private static ElemAttrDescrRow getGroupRefDescrRow(GroupReference gf,
                                         Set<GroupDescriptor> groupsForPostProcessing,
                                         ChangeInfoDetails chid,
                                         ModelBuildCntx cntx) {
        ElemAttrDescrRow groupRefDescrRow = new ElemAttrDescrRow();

        groupRefDescrRow.setRefToGroup(true);
        groupRefDescrRow.setRefGroupId(GROUP_TABLE_ID_PREFIX + gf.getRef().toString());

        //set cardinality info
        groupRefDescrRow.getMetaInfo().put(ElemAttrDescrRow.META_GROUP_REF_MIN_OCCURS, gf.getMinOccurs());
        if (gf.isMaxOccUnbound()) {
            groupRefDescrRow.getMetaInfo().put(ElemAttrDescrRow.META_GROUP_REF_MAX_OCCURS, "*");
        } else {
            groupRefDescrRow.getMetaInfo().put(ElemAttrDescrRow.META_GROUP_REF_MAX_OCCURS, gf.getMaxOccurs());
        }
        String cardinality = getCardinalityCellVal(gf.getMinOccurs(), gf.getMaxOccurs(), gf.isMaxOccUnbound());
        groupRefDescrRow.setCardinality(cardinality);

        //set change info
        if(chid != null){
            log.debug("ChangeInfo is found.");
            switch( chid.getChangeType() ){
                case CHANGE_BY_REF_GRP:
                    groupRefDescrRow.setChangesInGroup(true);
                    break;
                case NEW:
                    groupRefDescrRow.setNew(true);
                    break;
                case DELETE:
                    groupRefDescrRow.setDeleted(true);
                    break;
                case REPLACED_GRP_REF:
                    groupRefDescrRow.setReplacedGroup(true);
                    break;
                case CHANGE_IN_ATTRIBUTES:
                    groupRefDescrRow.setChangeInAttributes(true);
                    break;
                case CHANGE_BY_REF_GRP_AND_ATTR:
                    groupRefDescrRow.setChangeInAttributes(true);
                    groupRefDescrRow.setChangesInGroup(true);
                    break;
                case REPLACE_GRP_REF_AND_ATTR:
                    groupRefDescrRow.setReplacedGroup(true);
                    groupRefDescrRow.setChangeInAttributes(true);
                    break;
            }
        }

        if( ! cntx.getTableGroupSet().contains(gf.getGroupDescr()) ){
            groupsForPostProcessing.add(gf.getGroupDescr());
        }

        return groupRefDescrRow;
    }

    /**
     * Return attribute rows of the processed type only without any analysing its base type.<br/>
     *<br/>
     * This method may be used for attribute row collection building of a type that hasn't any changes - an ordinary type.<br/>
     * In this case an appropriated TypeDiffInfo for the TypeDescription object is absentee. This fact should be taken in account.
     *
     * @param cntx
     * @return List<TableRow>
     */
    //private List<TableRow> getCurrentTypeAttrRows(TypeDescriptor td, Map<TypeDescriptor, TypeDiffInfo> dfMap, Set<TypeDescrTable> tableSet, int deepCount){
    private static List<TableRow> getCurrentTypeAttrRows(ModelBuildCntx cntx){
        TypeDescriptor td = cntx.getTd();
        log.debug("Get rows for type "+td+"; deepCount="+cntx.getDeepCount());
        Set<TypeDescriptor> typesForPostProcessing = new HashSet<>();
        List<TableRow> resLst = new LinkedList<>();

        List<AttributeDescriptor> edForProcessing = new LinkedList<>();
        edForProcessing.addAll( td.getAttributes() );
        //If attributes have been removes they are in affectedElements collection only.
        //We should extract they from that collection and add to the rows collection
        //todo: insert deleted element more nearly to old position. At this time it is inserted at the end of elem. list.
        if(cntx.getDfTypeMap().get(td)!=null){
            TypeDiffInfo df = cntx.getDfTypeMap().get(td);
            Collection<ChangeInfoDetails> changes = df.getAffectedAttributes().values();
            for(ChangeInfoDetails changeInfo : changes){
                if( ELEM_OR_ATTR_CHANGE_TYPE.DELETE.equals( changeInfo.getChangeType() ) ){
                    edForProcessing.add(changeInfo.getDelAttr());
                }
            }
        }

        log.debug("Attributes");
        for(AttributeDescriptor ad : td.getAttributes()){
            log.debug("\tattribute"+ad.getName());
            ElemAttrDescrRow attrRow = new ElemAttrDescrRow();
            TypeDescriptor attrTd = ad.getTypeDescr();

            attrRow.setName( ad.getName() );
            if(!attrTd.isBaseXsdType()){
                //String tableId = createTableId(attrTd);
                if(attrTd.isAnonymous()){
                    attrRow.setTypeName("anonymous type");
                } else {
                    attrRow.setTypeName(attrTd.getQName().getNamespaceURI());
                    attrRow.setTypeName(attrTd.getQName().getLocalPart());
                }
                attrRow.setTypeId( attrTd.getId() );
                attrRow.setTypeIsBaseXSD(false);
                if( findTypeDescrTable(attrTd.getId(), cntx.getTableTypeSet()) == null){
                    typesForPostProcessing.add(attrTd);
                }

            } else {
                attrRow.setTypeName(attrTd.getName());
                attrRow.setTypeIsBaseXSD(true);

            }
            attrRow.setUse(String.valueOf(ad.getUse()));

            //Checking if the attribute is changed
            TypeDiffInfo df = cntx.getDfTypeMap().get(td);
            //TypeDiffInfo may be null if the type has not been changed
            if(df != null){
                ChangeInfoDetails attrChangeInfo = df.getAffectedAttributes().get(ad);
                if(attrChangeInfo != null){
                    switch (attrChangeInfo.getChangeType()) {
                        case CHANGE_IN_TYPE:
                            attrRow.setChangedInType(true);
                            break;
                        case NEW:
                            attrRow.setNew(true);
                            break;
                        case DELETE:
                            attrRow.setDeleted(true);
                            break;
                        case REPLACED_TYPE:
                            attrRow.setReplacedType(true);
                            break;
                        case CHANGE_IN_ATTRIBUTES:
                            attrRow.setChangeInAttributes(true);
                            break;
                        case CHANGE_IN_ATTRIBUTES_AND_CHANGE_IN_TYPE:
                            attrRow.setChangeInAttributes(true);
                            attrRow.setChangedInType(true);
                            break;
                        case CHANGE_IN_ATTRIBUTES_AND_REPLACED_TYPE:
                            attrRow.setChangeInAttributes(true);
                            attrRow.setReplacedType(true);
                            break;
                    }
                }
            }

            //Set default and fixed values if defined
            attrRow.setDefaultValue( ad.getDefaultVal() );
            attrRow.setFixedValue( ad.getFixedVal() );
            attrRow.setUse( ad.getUse() );

            //Sorting annotation for appropriated collections
            for(AnnotationDescriptor annotDescr : ad.getAnnotations()){
                AnnotationDescriptor.ANNOTATION_TYPE annotType = annotDescr.getAnnotationType();
                if( AnnotationDescriptor.ANNOTATION_TYPE.APPINFO.equals(annotType) ){
                    attrRow.getAppinfos().add(annotDescr.getValue());
                }else if( AnnotationDescriptor.ANNOTATION_TYPE.DOCUMENTATION.equals(annotType) ){
                    attrRow.getDocumentations().add(annotDescr.getValue());
                }
            }

            resLst.add( new TableRow(attrRow) );
        }


        //Post processing
        //Here is processing of attribute types - for each type a table will be created
        if(cntx.getDeepCount() > 0){
            log.debug("Type descriptors for processing\n"+typesForPostProcessing+"\ndeepCount="+cntx.getDeepCount());
            for(TypeDescriptor typeForDefProc : typesForPostProcessing){
                //type of an attribute can't be complex
                createModelByTd( cntx.switchContextForNewTd(typeForDefProc, cntx.getDeepCount()-1) );
            }
        } else {
            log.info("Attribute processing for type"+td+": deepCount=0");
        }

        return resLst;
    }


    public static TypeDescrTable findTypeDescrTableByTd(TypeDescriptor td, Set<TypeDescrTable> tableSet){
        return findTypeDescrTable(buildTypeDescrTableId(td), tableSet);
    }

    public static TypeDescrTable findTypeDescrTable(String tableId, Set<TypeDescrTable> tableSet){
        TypeDescrTable resTable = null;

        if(tableId!=null){
            for(TypeDescrTable table : tableSet){
                if(tableId.equals( table.getId() ) ){
                    resTable = table;
                    break;
                }
            }
        }

        log.debug("Table " + tableId + " is processed: " + resTable);
        return resTable;
    }


    private static ChangeInfoDetails getChngNfoDetailsInParentScope(ParticleContent pc,
                                                                        TypeDescriptor td,
                                                                            GroupDescriptor gd,
                                                                                Map<TypeDescriptor, TypeDiffInfo> dfTypeMap,
                                                                                    Map<GroupDescriptor, GroupDiffInfo> dfGroupMap){
        ChangeInfoDetails chid = null;
        if(td != null){
            if( dfTypeMap.containsKey(td) ){
                TypeDiffInfo diffNfo = dfTypeMap.get(td);
                chid  = diffNfo.getAffectedItems().get(pc);
            }
        } else if(gd != null){
            if( dfGroupMap.containsKey(gd) ){
                GroupDiffInfo diffNfo = dfGroupMap.get(gd);
                chid  = diffNfo.getAffectedItems().get(pc);
            }
        } else {
            throw new IllegalArgumentException(ARG_ERR_MSG);
        }

        return chid;
    }


    private static String getCardinalityCellVal(Long minOcc, Long maxOcc, boolean isMaxOccUnbound){
        String cardinality = String.valueOf(minOcc);
        String maxCard = String.valueOf(maxOcc);
        if(isMaxOccUnbound){
            maxCard = "*";
        }
        return cardinality + ".." + maxCard;
    }


    ////////////////// OTHER METHODS ////////////////////////////////


    /**
     * This is auxiliary method.
     * This function searches "top" tables, that is tables are not base type for other ones (terminate child).
     * For example those are all table that describes an anonymous type.
     * @param model
     * @return Set<TypeDescrTable>
     */
    public Set<TypeDescrTable> searchTopTables(final Set<TypeDescrTable> model){
        Set<TypeDescrTable> clonedModel = new HashSet<>(model);
        Set<TypeDescrTable> resSet = new HashSet<>();

        for(TypeDescrTable tdt : model){
            if(tdt.isAnonymous()){
                resSet.add(tdt);
                clonedModel.remove(tdt);
            }
        }

        for(TypeDescrTable tdt : clonedModel){
            boolean found = false;
            for(TypeDescrTable innerTdt : model){
                if( tdt.getId().equals(innerTdt.getBaseTypeTableId()) ){
                    found=true;
                    break;
                }
            }
            if(!found){
                resSet.add(tdt);
            }
        }

        return resSet;
    }

    /**
     * This auxiliary method performs search in a Set of TypeDescrTable by a table id.
     *
     * @param tableSet
     * @return found table or null
     */
    public static TypeDescrTable searchTypeTableById(final String id, final Set<TypeDescrTable> tableSet){
        if(id == null || tableSet == null){
            return null;
        }

        TypeDescrTable resTdt = null;
        for(TypeDescrTable tdt : tableSet){
            if( id.equals(tdt.getId()) ){
                resTdt = tdt;
                break;
            }
        }
        return resTdt;
    }


    public static GroupDescrTable searchGroupTableById(final String id, final Set<GroupDescrTable> tableSet){
        if(id == null || tableSet == null){
            return null;
        }

        GroupDescrTable resGd = null;
        for(GroupDescrTable gdt : tableSet){
            if( id.equals(gdt.getId()) ){
                resGd = gdt;
                break;
            }
        }
        return resGd;
    }


    /**
     * This is auxiliary method.
     * This method return a set of "survived" tables:
     * <ul>
     *      <li>
     *          this set doesn't contains tables that describe an anonymous simple type of ordinary element(not root element)
     *          or an attribute.
     *      </li>
     *      <li>this set doesn't contains tables that describe groups that are referenced.</li>
     * </ul>
     * <br/>
     * The method moves those tables into <strong>metaInfo</strong> map of an ElemAttrDescrRow.<br/>
     * This method is useful if we want to display information into DESCRIPTION column
     * instead of display the information as standalone table about following items:
     * <ul>
     *     <li>an anonymous type of an ordinary element(not root element) or an attribute</li>
     *     <li>an element group</li>
     * </ul>
     *
     * @param tables
     * @return Set<TypeDescrTable>
     */
    @SuppressWarnings("unchecked")
    public Set<TablePresentedDescriptor> compactModel(Set<? extends TablePresentedDescriptor> tables){
        Map<String, TablePresentedDescriptor> tableMap = tableModelAsMap(tables);

        Set<TablePresentedDescriptor> resSet = new HashSet<>();
        Set<TablePresentedDescriptor> setForDelete = new HashSet<>();

        for(TablePresentedDescriptor table : tables){
            boolean isAllowedType = (table instanceof TypeDescrTable) && ((TypeDescrTable)table).isComplexType();
            boolean isGroup = (table instanceof GroupDescrTable);
            if(isAllowedType || isGroup){
                List<ElemAttrDescrRow> rows = new LinkedList<>();
                //Add descriptors for analyzing.
                if( ! table.getRootRow().isEmpty() ){
                    TableRow rootRow = table.getRootRow();
                    if(rootRow.isRootGroupRefRow()){
                        rows.add(rootRow.getElemRow());
                    } else {
                        rows.addAll( flatRootIndicator(rootRow.getIndicatorDescrRow()) );
                    }
                }
                //Add attributes for analyzing
                if (isAllowedType){
                    List<TableRow> attrRows = ((TypeDescrTable) table).getAttrRows();
                    for(TableRow attrItem : attrRows){
                        rows.add( attrItem.getElemRow() );
                    }
                }

                //Do analyzing
                for(ElemAttrDescrRow row : rows){
                    if( ! row.isTypeIsBaseXSD() && ! row.isRefToGroup()){
                        String rowTypeId = row.getTypeId();
                        TypeDescrTable rowTypeTbl = (TypeDescrTable) tableMap.get(rowTypeId);
                        //May be NULL either if we have restricted deep of types analysis or
                        //if a given set of tables doesn't contain this type description table.
                        //See createModelByTd JavaDoc about deepCount parameter.
                        if(rowTypeTbl != null){
                            if( !rowTypeTbl.isComplexType() && rowTypeTbl.isAnonymous() ){
                                row.getMetaInfo().put(ElemAttrDescrRow.META_ELEM_TYPE_TABLE, rowTypeTbl);
                                setForDelete.add(rowTypeTbl);
                            }
                        }

                    } else if(row.isRefToGroup()){
                        GroupDescrTable gdt = (GroupDescrTable) tableMap.get(row.getRefGroupId());
                        //Shouldn't be NULL because of all belonged to a type groups are loaded always
                        //But may be NULL if a given for filtration set of tables doesn't contain this group description table.
                        if(gdt == null){
                            log.warn("A defined by a group part of a type has not been loaded!" +
                                    " Group table id: "+row.getRefGroupId());
                        }
                        row.getMetaInfo().put(ElemAttrDescrRow.META_GROUP_TYPE_TABLE, gdt);
                        row.setGrpIncluded(true);
                        setForDelete.add(gdt);
                    }

                }
            }
        }

        //Using ListUtils.removeAll because of CollectionUtils.removeAll contains a bug that
        //only fixed in 4.0 branch of commons-collections
        List diffList = ListUtils.removeAll(new LinkedList(tables), new LinkedList(setForDelete));
        resSet = new HashSet(diffList);

        return resSet;
    }

    /**
     * This auxiliary method extract all ElemAttrDescrRow from an IndicatorDecrRow into flat list in recursive manner.
     *
     * @param indicatorDescrRow
     * @return List<ElemAttrDescrRow>
     */
    private List<ElemAttrDescrRow> flatRootIndicator(IndicatorDescrRow indicatorDescrRow){
        List<ElemAttrDescrRow> resLst = new LinkedList<>();
        if(indicatorDescrRow==null){
            return resLst;
        }

        for( TableRow contentItem : indicatorDescrRow.getContentRows() ){
            if( ! contentItem.isEmpty() ){
                if( contentItem.getElemRow() != null){
                    resLst.add( contentItem.getElemRow() );

                } else {
                    List<ElemAttrDescrRow> tmpList = flatRootIndicator( contentItem.getIndicatorDescrRow() );
                    resLst.addAll(tmpList);
                }
            }
        }

        return resLst;
    }

    /**
     * This method transforms a Set of TypeDescrTable to a Map(table_id->table_instance)
     *
     * @param tableModelSet
     * @return Map<String, TablePresentedDescriptor>
     */
    public Map<String, TablePresentedDescriptor> tableModelAsMap(final Set<? extends TablePresentedDescriptor> tableModelSet){
        Map<String, TablePresentedDescriptor> resMap = new HashMap<>();
        for(TablePresentedDescriptor tpd : tableModelSet){
            resMap.put(tpd.getId(), tpd);
        }
        return resMap;
    }

    /**
     * This method returns a set of TypeDescrTable objects exception table used for WS-methods.
     * This method does not affect a source type type table set.
     *
     * @param typeTbls
     * @param methodTbls
     * @return new filtered Set instance
     */
    @SuppressWarnings("unchecked")
    public Set<TypeDescrTable> filterTableSetFromMessagePartTypes(Set<TypeDescrTable> typeTbls, Set<WSMethodDescrTable> methodTbls){
        Set<TypeDescrTable> methodTypeTbls = new HashSet<>();

        for (WSMethodDescrTable methodTbl : methodTbls){
            if( ! methodTbl.getInputMessage().isEmpty() ){
                for(MessagePartDescrTable msgPartTbl : methodTbl.getInputMessage()) {
                    methodTypeTbls.add(msgPartTbl.getTypeDescr());
                }
            }
            if( ! methodTbl.getOutputMessage().isEmpty() ){
                for(MessagePartDescrTable msgPartTbl : methodTbl.getOutputMessage()) {
                    methodTypeTbls.add(msgPartTbl.getTypeDescr());
                }
            }
        }

        List<TypeDescrTable> sourceTypesLst = new LinkedList<>();
        sourceTypesLst.addAll(typeTbls);
        List<TypeDescrTable> methodTypeTblsLst = new LinkedList<>();
        methodTypeTblsLst.addAll(methodTypeTbls);
        List filteredTypeLst = ListUtils.removeAll(sourceTypesLst, methodTypeTblsLst);

        Set<TypeDescrTable> resSet = new HashSet<>();
        resSet.addAll(filteredTypeLst);
        return resSet;
    }

    /**
     * This auxiliary function merges root indicators content or attributes of base and child types.<br/>
     * This function takes in account next cases:<br/>
     * <ol>
     *     <li>that base type may haven't got any root indicator and have attributes only;</li>
     *     <li>either child type, or base one, or both types may haven't got any attributes.</li>
     * </ol>
     *
     * @param rows
     * @param baseRows
     * @param elementAsRow
     * @return List<TableRow>
     */
    private List<TableRow> mergeContent(final List<TableRow> rows, final List<TableRow> baseRows, final boolean elementAsRow){
        List<TableRow> reslList;
        //Content of a base type should follow the content any child type
        if(elementAsRow){
            //Merge root indicators
            if(baseRows.get(0).getIndicatorDescrRow() != null &&
                    rows.get(0).getIndicatorDescrRow() !=null ){
                baseRows.get(0).getIndicatorDescrRow().getContentRows().addAll( rows.get(0).getIndicatorDescrRow().getContentRows() );
                reslList = baseRows;

            } else if(baseRows.get(0).getIndicatorDescrRow() == null &&
                    rows.get(0).getIndicatorDescrRow() != null){
                reslList = rows;

            } else if( baseRows.get(0).getIndicatorDescrRow() != null &&
                    rows.get(0).getIndicatorDescrRow() == null){
                throw new RuntimeException("Current type must have an root indicator because its a base type has one.");

            } else {
                reslList = new LinkedList<>();
                reslList.add( new TableRow() );
            }

        } else {
            //Merge attributes rows
            if(!baseRows.isEmpty() && !rows.isEmpty() ){
                baseRows.addAll( rows );
                reslList = baseRows;

            } else if(baseRows.isEmpty() && !rows.isEmpty()){
                reslList = rows;

            } else if( !baseRows.isEmpty() && rows.isEmpty() ){
                reslList = baseRows;

            } else {
                reslList = new LinkedList<>();
            }
        }

        return reslList;
    }

    /**
     * This method is used from a template
     * @param strQName
     * @return String
     */
    public static String getLocalPartTypeName(String strQName){
        return TypeDescrTable.getLocalPartTypeName(strQName);
    }

    private static String getIndicatorName(Class<? extends XmlSchemaGroupParticle> groupParticleType){
        return groupParticleType.getSimpleName().replace("XmlSchema", "");
    }

    private static String buildGroupDescrTableId(GroupDescriptor gd){
        return GROUP_TABLE_ID_PREFIX + gd.getId();
    }

    private static String buildTypeDescrTableId(TypeDescriptor td){
        return TYPE_TABLE_ID_PREFIX + td.getId();
    }
}
