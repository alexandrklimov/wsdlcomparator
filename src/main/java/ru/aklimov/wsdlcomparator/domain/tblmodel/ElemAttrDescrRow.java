package ru.aklimov.wsdlcomparator.domain.tblmodel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class describes either an elemenet of a complex type, or an attribute of one or a reference to a group.
 */
public class ElemAttrDescrRow {
    static public String META_ELEM_TYPE_TABLE = "ELEMENT_TYPE_TABLE";
    static public String META_GROUP_TYPE_TABLE = "GROUP_TYPE_TABLE";
    static public String META_GROUP_REF_MIN_OCCURS = "GROUP_REF_MIN_OCCURS";
    static public String META_GROUP_REF_MAX_OCCURS = "GROUP_REF_MAX_OCCURS";

    private boolean refToGroup;
    /**Whether content of a referenced group should be display in scope of the row*/
    private boolean grpIncluded;
    /**QName of a group that is referenced by this row**/
    private String refGroupId;
    private List<String> documentations;
    private List<String> appinfos;
    private String name;
    private String typeName;
    /**May be null if a type is anonymous. If a type is anonymous it's namespace equals a namespace an owner element*/
    private String typeNamespace;
    /**This field valid for element only*/
    private String cardinality;
    /**This field valid for element only*/
    private String nillable;
    /**This field valid for attribute only*/
    private String use = "optional";

    private String defaultValue;
    private String fixedValue;
    private boolean changedInType;
    private boolean changesInGroup;
    private boolean replacedType;
    /**A target group of the reference to a froup has been changed**/
    private boolean replacedGroup;
    private boolean deleted;
    private boolean isNew;
    private boolean changeInAttributes;


    /**
     * This field has got type table id. The table may not be.
      */
    private String typeId;

    /**
     * A described element is core XSD type one.
     */
    private boolean typeIsBaseXSD;

    /**
     * This field is valid only if type of a described element has been replaced
     * One is depends from
     * {@link ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE#REPLACED_TYPE}
     * value
     * */
    private String oldTypeName;

    private Map<String, Object> metaInfo = new HashMap<>();

    ////////////////// SETTERS AND GETTERS ///////////////////////////////////

    public boolean isRefToGroup() {
        return refToGroup;
    }

    public void setRefToGroup(boolean refToGroup) {
        this.refToGroup = refToGroup;
    }

    public String getRefGroupId() {
        return refGroupId;
    }

    public void setRefGroupId(String refGroupId) {
        this.refGroupId = refGroupId;
    }

    public boolean isChangedInType() {
        return changedInType;
    }

    public void setChangedInType(boolean changedInType) {
        this.changedInType = changedInType;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getOldTypeName() {
        return oldTypeName;
    }

    public void setOldTypeName(String oldTypeName) {
        this.oldTypeName = oldTypeName;
    }

    public boolean isTypeIsBaseXSD() {
        return typeIsBaseXSD;
    }

    public void setTypeIsBaseXSD(boolean typeIsBaseXSD) {
        this.typeIsBaseXSD = typeIsBaseXSD;
    }

    public boolean isReplacedType() {
        return replacedType;
    }

    public void setReplacedType(boolean replacedType) {
        this.replacedType = replacedType;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isChangeInAttributes() {
        return changeInAttributes;
    }

    public void setChangeInAttributes(boolean changeInAttributes) {
        this.changeInAttributes = changeInAttributes;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getNillable() {
        return nillable;
    }

    public void setNillable(String nillable) {
        this.nillable = nillable;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
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

    public String getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(String typeNamespace) {
        this.typeNamespace = typeNamespace;
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

    public boolean isChangesInGroup() {
        return changesInGroup;
    }

    public void setChangesInGroup(boolean changesInGroup) {
        this.changesInGroup = changesInGroup;
    }

    public boolean isReplacedGroup() {
        return replacedGroup;
    }

    public void setReplacedGroup(boolean replacedGroup) {
        this.replacedGroup = replacedGroup;
    }

    /**
     * Whether content of a referenced group should be display in scope of the row
     * @return
     */
    public boolean isGrpIncluded() {
        return grpIncluded;
    }

    public void setGrpIncluded(boolean grpIncluded) {
        this.grpIncluded = grpIncluded;
    }
}
