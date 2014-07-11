package ru.aklimov.wsdlcomparator.domain.diff;

import ru.aklimov.wsdlcomparator.domain.descriptors.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class contains information about changes some particle of an indicator: a nested indicator or an element.
 * In opposition to {@link ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo TypeDiffInfo} this class doesn't describe changes
 * in type as a whole but changes in its parts only.
 *
 * @author Alexandr Klimov
 */
public class ChangeInfoDetails {
    static public enum ELEM_OR_ATTR_CHANGE_TYPE {
        NEW,
        DELETE,
        CHANGE_IN_ATTRIBUTES,
        CHANGE_IN_TYPE,
        CHANGE_IN_CONTENT, //<-for group indicators (seq., choice)
        CHANGE_IN_CONTENT_AND_ATTR, //<-for group indicators (seq., choice)
        REPLACED_TYPE,
        CHANGE_IN_ATTRIBUTES_AND_CHANGE_IN_TYPE,
        CHANGE_IN_ATTRIBUTES_AND_REPLACED_TYPE,
        CHANGE_BY_REF_GRP, //<-for group reference
        REPLACED_GRP_REF, //<-for group reference
        CHANGE_BY_REF_GRP_AND_ATTR, //<-for group reference
        REPLACE_GRP_REF_AND_ATTR //<-for group reference
    }

    /**
     * This field is not necessary for change built-in attribute of GROUP tag.
     */
    private ELEM_OR_ATTR_CHANGE_TYPE changeType;

    /**
     * This field is only valid if <b>ChangeInfoDetails.changeType</b> is equals to <b>ELEM_OR_ATTR_CHANGE_TYPE.CHANGE_IN_TYPE</b>
     */
    private TypeDescriptor oldType;
    /**
     * The following fields are valid if <b>ChangeInfoDetails.changeType</b> is equals to <b>ELEM_OR_ATTR_CHANGE_TYPE.DELETE</b> only.
     */
    private ElementDescriptor delElem;
    private GroupReference oldGrpRef;
    private Map<String, String> changedBuiltInAttributes = new HashMap<>();
    private List<AttributeDescriptor> changedAttributes;
    private AttributeDescriptor delAttr;
    private IndicatorDescriptor delIndicator;

    public ChangeInfoDetails() {}

    public ChangeInfoDetails(ELEM_OR_ATTR_CHANGE_TYPE changeType, TypeDescriptor type) {
        this.changeType = changeType;
        this.oldType = type;
    }

    public ChangeInfoDetails(ELEM_OR_ATTR_CHANGE_TYPE changeType) {
        this.changeType = changeType;
    }

    ////////////// GETTERS/SETTERS ////////////////////////////////////////

    public ELEM_OR_ATTR_CHANGE_TYPE getChangeType() {
        return changeType;
    }

    public void setChangeType(ELEM_OR_ATTR_CHANGE_TYPE changeType) {
        this.changeType = changeType;
    }

    public TypeDescriptor getOldType() {
        return oldType;
    }

    public void setOldType(TypeDescriptor oldType) {
        this.oldType = oldType;
    }

    public ElementDescriptor getDelElem() {
        return delElem;
    }

    public void setDelElem(ElementDescriptor delElem) {
        this.delElem = delElem;
    }

    public GroupReference getOldGrpRef() {
        return oldGrpRef;
    }

    public void setOldGrpRef(GroupReference oldGrpRef) {
        this.oldGrpRef = oldGrpRef;
    }

    public AttributeDescriptor getDelAttr() {
        return delAttr;
    }

    public void setDelAttr(AttributeDescriptor delAttr) {
        this.delAttr = delAttr;
    }

    public IndicatorDescriptor getDelIndicator() {
        return delIndicator;
    }

    public void setDelIndicator(IndicatorDescriptor delIndicator) {
        this.delIndicator = delIndicator;
    }

    public List<AttributeDescriptor> getChangedAttributes() {
        if(changedAttributes==null){
            changedAttributes = new LinkedList<>();
        }
        return changedAttributes;
    }

    public void setChangedAttributes(List<AttributeDescriptor> changedAttributes) {
        this.changedAttributes = changedAttributes;
    }

    public Map<String, String> getChangedBuiltInAttributes() {
        if(changedBuiltInAttributes==null){
            changedBuiltInAttributes = new HashMap<>();
        }
        return changedBuiltInAttributes;
    }

    public void setChangedBuiltInAttributes(Map<String, String> changedBuiltInAttributes) {
        this.changedBuiltInAttributes = changedBuiltInAttributes;
    }
}
