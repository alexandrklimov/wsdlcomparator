package ru.aklimov.wsdlcomparator.domain.tblmodel;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 05.05.13
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class TypeDescrTable implements TablePresentedDescriptor {
    static public enum SIMPLE_CONTENT_TYPE{UNION, LIST, RESTRICTION}
    static public enum COMPLEX_CONTENT_TYPE{RESTRICTION, EXTENSION}
    static public enum INDICATOR_TYPE{ALL, SEQUENCE, CHOICE}

    private List<String> documentations;
    private List<String> appinfos;

    private boolean complexType;
    private boolean isNew;
    private boolean removed;

    /**
     * A table may describes not changed type too. This field may be useful during rendering a table.*/
    private boolean changed;

    /**This field corresponds to the {@link ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo#changedBaseType TypeDiffInfo#changedBaseType} field*/
    private boolean changedBaseType;

    /**This field corresponds to the {@link ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo#affectedByBaseChanges TypeDiffInfo#affectedByBaseChanges} field*/
    private boolean affectedByBaseChanges;

    /**
     * This field has got string value of a type QName. A base type can't be anonymous.
     */
    private String baseTypeTableId ="";
    private String baseTypeName;
    /**Whether a base type info should be display as a part of this type*/
    private boolean baseTypeIncluded;

    /**
     * This field indicates that the baseTypeTableId field contains QName of one of XSD built-in type (string, fouble, decimal and so on)
     * */
    private boolean baseTypeIsBaseXsdType;

    /**
     * This field has got string value of a type QName or <strong>"owner_type_qname-type"+"owner_element_name-element"+"_anon_type"</strong>,
     * if a described type is anonymous
     */
    private String id = "";

    private String title = "";
    /**
     * This field presents a namespace of a type described by the table
     * This field is useful for obtain namespace of an ordinal element anonymous type.
     * */
    private String typeNamespace;

    /**This field is valid only if a described type is a simple one*/
    private SIMPLE_CONTENT_TYPE simpleContentType;

    /**
     * This field valid only if described type is simple one.
     * Contains information about each facet of the described type in <strong>facetType->facetValue</strong> form.
     */
    private List<String[]> facets = new LinkedList<>();

    /**This field is valid only if a described type is a complex one
     * May be null.
     * */
    //todo: I think this field is unnecessary, because this information is contained in a table root row
     private COMPLEX_CONTENT_TYPE complexContentType;

    /**This field is valid only if a described type is a complex one*/
    //todo: I think this field is unnecessary, because this information is contained in a table root row
    private INDICATOR_TYPE rootIndicatorType;

    //describes all table content from one start point
    private TableRow rootRow = new TableRow();
    private List<TableRow> attrRows = new LinkedList<>();
    private Map<String, Object> metaInfo = new HashMap<>();

    /**This field is useful for merge operation and processing for display*/
    private boolean anonymous;

    /**Following fields are useful for generate description an a fallback link for anonymous type not root element*/
    private String ownerElementName;
    private String ownerTableId;
    private String ownerTableTitle;

    ////////////////////// METHODS //////////////////////////////////

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\t").append(super.toString()).append(": ").append("id = "+id);
        sb.append("\n title: "+title);
        sb.append("\n baseTypeTableId: "+ baseTypeTableId);
        sb.append("\n isNew: " + isNew);
        sb.append("\n isComplexType: "+complexType);
        sb.append("\n changedBaseType: "+changedBaseType);
        sb.append("\n affectedByBaseChanges: "+affectedByBaseChanges);
        sb.append("\n simpleContentType: "+simpleContentType);
        sb.append("\n complexContentType: "+complexContentType);
        sb.append("\n rootIndicatorType: "+ rootIndicatorType);
        sb.append("\n facets: "+facets);
        sb.append("\n rootRow: "+ rootRow);
        sb.append("\n attrRows: "+ attrRows);
        sb.append("\n metaInfo: "+metaInfo);

        return sb.toString();
    }

    @Override
    public boolean equals(Object otherTblObj){
        if(this==otherTblObj){
            return true;
        }

        boolean res = false;
        if(otherTblObj instanceof TypeDescrTable){
            TypeDescrTable otherTable = (TypeDescrTable) otherTblObj;
            if(this.id!=null){
                res = this.id.equals(otherTable.id);
            }
        }

        return res;
    }

    @Override
    public int hashCode(){
        String rootRowStr = "";
        if(rootRow !=null ){
            if(rootRow.getIndicatorDescrRow()!=null){
              rootRowStr = rootRow.getIndicatorDescrRow().getId();
            }
        }
        String attrRowsStr = "";
        if(attrRows != null){
            attrRowsStr = Integer.toString( attrRows.size() );
        }
        String strForHash = id + complexContentType + simpleContentType + rootIndicatorType + complexType + isNew + rootRowStr + attrRowsStr;
        return strForHash.hashCode();
    }

    //////////////////// Getters/Setters ///////////////////////////////////////////////

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public TableRow getRootRow() {
        return rootRow;
    }

    public void setRootRow(TableRow rootRow) {
        this.rootRow = rootRow;
    }

    public List<TableRow> getAttrRows() {
        if(attrRows==null){
            attrRows = new LinkedList<>();
        }
        return attrRows;
    }

    public void setAttrRows(List<TableRow> attrRows) {
        this.attrRows = attrRows;
    }

    public Map<String, Object> getMetaInfo() {
        if(metaInfo==null){
            metaInfo = new HashMap<>();
        }
        return metaInfo;
    }

    public void setMetaInfo(Map<String, Object> metaInfo) {
        this.metaInfo = metaInfo;
    }

    public boolean isChangedBaseType() {
        return changedBaseType;
    }

    public void setChangedBaseType(boolean changedBaseType) {
        this.changedBaseType = changedBaseType;
    }

    public boolean isComplexType() {
        return complexType;
    }

    public void setComplexType(boolean complexType) {
        this.complexType = complexType;
    }

    public String getBaseTypeTableId() {
        return baseTypeTableId;
    }

    public void setBaseTypeTableId(String baseTypeTableId) {
        this.baseTypeTableId = baseTypeTableId;
    }

    public SIMPLE_CONTENT_TYPE getSimpleContentType() {
        return simpleContentType;
    }

    public void setSimpleContentType(SIMPLE_CONTENT_TYPE simpleContentType) {
        this.simpleContentType = simpleContentType;
    }

    public COMPLEX_CONTENT_TYPE getComplexContentType() {
        return complexContentType;
    }

    public void setComplexContentType(COMPLEX_CONTENT_TYPE complexContentType) {
        this.complexContentType = complexContentType;
    }

    public List<String[]> getFacets() {
        if(facets==null){
            facets = new LinkedList<>();
        }
        return facets;
    }

    public void setFacets(List<String[]> facets) {
        this.facets = facets;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public INDICATOR_TYPE getRootIndicatorType() {
        return rootIndicatorType;
    }

    public void setRootIndicatorType(INDICATOR_TYPE rootIndicatorType) {
        this.rootIndicatorType = rootIndicatorType;
    }

    public boolean isAffectedByBaseChanges() {
        return affectedByBaseChanges;
    }

    public void setAffectedByBaseChanges(boolean affectedByBaseChanges) {
        this.affectedByBaseChanges = affectedByBaseChanges;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isBaseTypeIsBaseXsdType() {
        return baseTypeIsBaseXsdType;
    }

    public void setBaseTypeIsBaseXsdType(boolean baseTypeIsBaseXsdType) {
        this.baseTypeIsBaseXsdType = baseTypeIsBaseXsdType;
    }

    public String getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(String namespace) {
        this.typeNamespace = namespace;
    }

    public List<String> getDocumentations() {
        if(documentations ==null){
            documentations = new LinkedList<>();
        }
        return documentations;
    }

    public void setDocumentations(List<String> documentations) {
        this.documentations = documentations;
    }

    public List<String> getAppinfos() {
        if(appinfos==null){
            appinfos = new LinkedList<>();
        }
        return appinfos;
    }

    public void setAppinfos(List<String> appinfos) {
        this.appinfos = appinfos;
    }

    public String getOwnerElementName() {
        return ownerElementName;
    }

    public void setOwnerElementName(String ownerElementName) {
        this.ownerElementName = ownerElementName;
    }

    public String getOwnerTableId() {
        return ownerTableId;
    }

    public void setOwnerTableId(String ownerTableId) {
        this.ownerTableId = ownerTableId;
    }

    public String getOwnerTableTitle() {
        return ownerTableTitle;
    }

    public void setOwnerTableTitle(String ownerTableTitle) {
        this.ownerTableTitle = ownerTableTitle;
    }

    public String getBaseTypeName() {
        return baseTypeName;
    }

    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }

    /**
     * Whether a base type info should be display as a part of this type
     *
     * @return
     */
    public boolean isBaseTypeIncluded() {
        return baseTypeIncluded;
    }

    public void setBaseTypeIncluded(boolean baseTypeIncluded) {
        this.baseTypeIncluded = baseTypeIncluded;
    }

    //////////////////////////// OTHER METHODS ////////////////////////////

    /**
     * This method is used from a template
     * @param strQName
     * @return String
     */
    static public String getLocalPartTypeName(String strQName){
        return QName.valueOf(strQName).getLocalPart();
    }

}
