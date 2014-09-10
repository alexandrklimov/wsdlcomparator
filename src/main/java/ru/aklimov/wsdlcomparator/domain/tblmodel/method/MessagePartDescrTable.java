package ru.aklimov.wsdlcomparator.domain.tblmodel.method;

import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;

/**
 * Created by aklimov on 03.09.2014.
 */
public class MessagePartDescrTable {
    private String name;
    private boolean byElementBinding;
    private String elemName; //<-- isn't NULL if byElementBinding = TRUE only
    private String elemNamespace; //<-- isn't NULL if byElementBinding = TRUE only
    private TypeDescrTable typeDescr;

    private boolean isNew;
    private boolean deleted;
    private boolean affectedByType;
    /**Whether switch from wsdl:part element to wsdl:part type or vice versa has been performed*/
    private boolean xsdSchemaBindingChanged;
    private boolean typeChanged;
    private TypeDescrTable oldTypeDescr;
    private MessagePartDescrTable deletedPart;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isByElementBinding() {
        return byElementBinding;
    }

    public void setByElementBinding(boolean byElementBinding) {
        this.byElementBinding = byElementBinding;
    }

    public String getElemName() {
        return elemName;
    }

    public void setElemName(String elemName) {
        this.elemName = elemName;
    }

    public String getElemNamespace() {
        return elemNamespace;
    }

    public void setElemNamespace(String elemNamespace) {
        this.elemNamespace = elemNamespace;
    }

    public TypeDescrTable getTypeDescr() {
        return typeDescr;
    }

    public void setTypeDescr(TypeDescrTable typeDescr) {
        this.typeDescr = typeDescr;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isAffectedByType() {
        return affectedByType;
    }

    public void setAffectedByType(boolean affectedByType) {
        this.affectedByType = affectedByType;
    }

    public boolean isXsdSchemaBindingChanged() {
        return xsdSchemaBindingChanged;
    }

    public void setXsdSchemaBindingChanged(boolean xsdSchemaBindingChanged) {
        this.xsdSchemaBindingChanged = xsdSchemaBindingChanged;
    }

    public boolean isTypeChanged() {
        return typeChanged;
    }

    public void setTypeChanged(boolean typeChanged) {
        this.typeChanged = typeChanged;
    }

    public TypeDescrTable getOldTypeDescr() {
        return oldTypeDescr;
    }

    public void setOldTypeDescr(TypeDescrTable oldTypeDescr) {
        this.oldTypeDescr = oldTypeDescr;
    }

    public MessagePartDescrTable getDeletedPart() {
        return deletedPart;
    }

    public void setDeletedPart(MessagePartDescrTable deletedPart) {
        this.deletedPart = deletedPart;
    }
}
