package ru.aklimov.wsdlcomparator.modelbuilders;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.domain.tblmodel.*;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.MessagePartDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.WSMethodDescrTable;

import java.util.*;

/**
 * This class contains some auxiliary methods are may be useful
 * for postprocessing a set of built models(filtering, grouping e.t.c).
 * <br>
 * This class intended for using out of scope a models building process.
 *
 * @authos Alexandr Klimov
 */
public class PostCreationUtils {
    static private Logger log = LoggerFactory.getLogger(PostCreationUtils.class);


    /**
     * This method returns a set of TypeDescrTable objects exception table used for WS-methods.
     * This method does not affect a source type type table set.
     *
     * @param typeTbls
     * @param methodTbls
     * @return new filtered Set instance
     */
    @SuppressWarnings("unchecked")
    public static Set<TypeDescrTable> filterTableSetFromMessagePartTypes(Set<TypeDescrTable> typeTbls, Set<WSMethodDescrTable> methodTbls){
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
    public static Set<TablePresentedDescriptor> compactModel(Set<? extends TablePresentedDescriptor> tables){
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
     * This method transforms a Set of TypeDescrTable to a Map(table_id->table_instance)
     *
     * @param tableModelSet
     * @return Map<String, TablePresentedDescriptor>
     */
    private static Map<String, TablePresentedDescriptor> tableModelAsMap(final Set<? extends TablePresentedDescriptor> tableModelSet){
        Map<String, TablePresentedDescriptor> resMap = new HashMap<>();
        for(TablePresentedDescriptor tpd : tableModelSet){
            resMap.put(tpd.getId(), tpd);
        }
        return resMap;
    }


    /**
     * This auxiliary method extract all ElemAttrDescrRow from an IndicatorDecrRow into flat list in recursive manner.
     *
     * @param indicatorDescrRow
     * @return List<ElemAttrDescrRow>
     */
    private static List<ElemAttrDescrRow> flatRootIndicator(IndicatorDescrRow indicatorDescrRow){
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
}
