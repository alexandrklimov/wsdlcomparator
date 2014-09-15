package ru.aklimov.wsdlcomparator.domain.diff.impl;

import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfoWithAffected;

import java.util.HashMap;
import java.util.Map;

/**
 * This objects contains some information about changes of a type.
 *
 * affectedElementsName field contains a type->changed_element map. It's necessary because of an element may be changed
 * in a base type of this type, or in a base type of a base type of this type e.t.c.
 * A TypeDescriptor object contains information about its base type but we need know a changed type exactly.
 *
 */
public class TypeDiffInfo implements IDiffInfoWithAffected {
    static public enum ROOT_GROUP_INDICATOR_DEFINITION_METHOD {
        DIRECTLY,
        BY_REF_TO_GROUP,
        WITHOUT_ROOT_IND
    }

    private boolean rootGrpIndDefinitionMethodChanged;
    private ROOT_GROUP_INDICATOR_DEFINITION_METHOD oldGrpIndDefinitionMethod;

    private TypeDescriptor typeDescr;
    private boolean isNew;
    private boolean removed;

    /**restriction -> extension or vice versa*/
    private boolean changedContentType;
    /**It corresponds to extension / restriction tag*/
    private Class<? extends XmlSchemaAnnotated> oldContentType;

    /**
     * This field indicates that base type has been replaced by another type.
     */
    private boolean changedBaseType;
    private TypeDescriptor oldBaseTd;

    /**
     * Contrary to changedBaseType field this one indicates that there are some changes in a base type of this type,
     * and so this type is updated too.
     */
    private boolean affectedByBaseChanges;

    ////////// Following info is for complex type //////////

    private boolean changedContentModelType;
    /**It corresponds to simpleContent / complexContent tag*/
    private Class<? extends XmlSchemaAnnotated> oldContentModelType;

    /**This field is valid only if described type is complex*/
    private boolean changedRootIndicator;
    /**This field is valid only if described type is complex*/
    private Class<? extends XmlSchemaGroupParticle> oldIndicator;
    /** this field is valid if described type is only complex */
    private Map<ParticleContent, ChangeInfoDetails> affectedItems;
    /** this field is valid if described type is only complex */
    private Map<String, ChangeInfoDetails> affectedAttributes;


    ///////////// CONSTRUCTORS /////////////////////////////////////////

    public TypeDiffInfo(boolean isNew, TypeDescriptor typeDescr) {
        this.isNew = isNew;
        this.typeDescr = typeDescr;
    }

    public TypeDiffInfo(TypeDescriptor typeDescr) {
        this.typeDescr = typeDescr;
    }

    public TypeDiffInfo(TypeDescriptor typeDescr, Map<ParticleContent, ChangeInfoDetails> affectedItems) {
        this.typeDescr = typeDescr;
        this.affectedItems = affectedItems;
    }


    ///////////// METHODS /////////////////////////////////////////

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("TypeDiffInfo: [");
        if(typeDescr.isComplexType()){
            sb.append("complex type; ");
        } else {
            sb.append("simple type; ");
        }
        if(typeDescr.isAnonymous()){
            sb.append("anonymous type of");
            String elemName = typeDescr.getOwnerInfoLst().get(0).getElemName();
            TypeDescriptor elemOwnerType = typeDescr.getOwnerInfoLst().get(0).getTypeDescriptor();
            sb.append(" " + elemName + " element ");
            if(elemOwnerType!=null){
                if(!elemOwnerType.isAnonymous()){
                    sb.append(elemOwnerType.getQName() + " type");
                } else {
                    sb.append(elemOwnerType + "type");
                }
            }
            sb.append("; ");
        } else {
            sb.append(" type QName = " + typeDescr.getQName() + "; ");
        }

        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Equals by equals TypeDescriptor
     *
     * @param diffInfoObj
     * @return boolean
     */
    @Override
    public boolean equals(Object diffInfoObj){
        if(this==diffInfoObj){
            return true;
        }

        boolean res = false;
        if(diffInfoObj instanceof TypeDiffInfo){
            TypeDiffInfo other = (TypeDiffInfo) diffInfoObj;
            res = this.typeDescr ==other.typeDescr;
        }

        return res;
    }

    @Override
    public int hashCode(){
        if(typeDescr !=null){
            return typeDescr.hashCode();
        } else {
            return super.hashCode();
        }
    }

    /////////////////////////// GETTERS / SETTERS ////////////////////////////////////////

    public TypeDescriptor getTypeDescr() {
        return typeDescr;
    }

    public void setTypeDescr(TypeDescriptor typeDescr) {
        this.typeDescr = typeDescr;
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

    public Map<ParticleContent, ChangeInfoDetails> getAffectedItems() {
        if(this.affectedItems == null){
            this.affectedItems = new HashMap<ParticleContent, ChangeInfoDetails>();
        }
        return affectedItems;

    }

    public void setAffectedItems(Map<ParticleContent, ChangeInfoDetails> affectedItems) {
        this.affectedItems = affectedItems;
    }

    public Map<String, ChangeInfoDetails> getAffectedAttributes() {
        if(affectedAttributes==null){
            affectedAttributes = new HashMap<>();
        }
        return affectedAttributes;
    }

    public void setAffectedAttributes(Map<String, ChangeInfoDetails>  affectedAttributes) {
        this.affectedAttributes = affectedAttributes;
    }

    public boolean isChangedContentType() {
        return changedContentType;
    }

    public void setChangedContentType(boolean changedContentType) {
        this.changedContentType = changedContentType;
    }

    public boolean isChangedBaseType() {
        return changedBaseType;
    }

    public void setChangedBaseType(boolean changedBaseType) {
        this.changedBaseType = changedBaseType;
    }

    public boolean isAffectedByBaseChanges() {
        return affectedByBaseChanges;
    }

    public void setAffectedByBaseChanges(boolean affectedByBaseChanges) {
        this.affectedByBaseChanges = affectedByBaseChanges;
    }

    public boolean isChangedRootIndicator() {
        return changedRootIndicator;
    }

    public void setChangedRootIndicator(boolean changedRootIndicator) {
        this.changedRootIndicator = changedRootIndicator;
    }

    public TypeDescriptor getOldBaseTd() {
        return oldBaseTd;
    }

    public void setOldBaseTd(TypeDescriptor oldBaseTd) {
        this.oldBaseTd = oldBaseTd;
    }

    public Class<? extends XmlSchemaGroupParticle> getOldIndicator() {
        return oldIndicator;
    }

    public void setOldIndicator(Class<? extends XmlSchemaGroupParticle> oldIndicator) {
        this.oldIndicator = oldIndicator;
    }

    public Class<? extends XmlSchemaAnnotated> getOldContentType() {
        return oldContentType;
    }

    public void setOldContentType(Class<? extends XmlSchemaAnnotated> oldContentType) {
        this.oldContentType = oldContentType;
    }

    public boolean isChangedContentModelType() {
        return changedContentModelType;
    }

    public void setChangedContentModelType(boolean changedContentModelType) {
        this.changedContentModelType = changedContentModelType;
    }

    public Class<? extends XmlSchemaAnnotated> getOldContentModelType() {
        return oldContentModelType;
    }

    public void setOldContentModelType(Class<? extends XmlSchemaAnnotated> oldContentModelType) {
        this.oldContentModelType = oldContentModelType;
    }

    public boolean isRootGrpIndDefinitionMethodChanged() {
        return rootGrpIndDefinitionMethodChanged;
    }

    public void setRootGrpIndDefinitionMethodChanged(boolean rootGrpIndDefinitionMethodChanged) {
        this.rootGrpIndDefinitionMethodChanged = rootGrpIndDefinitionMethodChanged;
    }

    public ROOT_GROUP_INDICATOR_DEFINITION_METHOD getOldGrpIndDefinitionMethod() {
        return oldGrpIndDefinitionMethod;
    }

    public void setOldGrpIndDefinitionMethod(ROOT_GROUP_INDICATOR_DEFINITION_METHOD oldGrpIndDefinitionMethod) {
        this.oldGrpIndDefinitionMethod = oldGrpIndDefinitionMethod;
    }
}
