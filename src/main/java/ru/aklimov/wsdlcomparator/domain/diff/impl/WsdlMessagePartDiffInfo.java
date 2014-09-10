package ru.aklimov.wsdlcomparator.domain.diff.impl;

import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfo;

/**
 * This class contains information about any changes of some WSDL message part
 */
public class WsdlMessagePartDiffInfo implements IDiffInfo{
    private WSMethodDescr.MessagePartDescr msgPart;
    private boolean isNew;
    private boolean deleted;
    private boolean affectedByType;
    /**Whether switch from wsdl:part element to wsdl:part type or vice versa has been performed*/
    private boolean xsdSchemaBindingChanged;
    private boolean typeChanged;
    private TypeDescriptor oldTypeDescriptor;
    private WSMethodDescr.MessagePartDescr deletedPart;

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

    public WSMethodDescr.MessagePartDescr getDeletedPart() {
        return deletedPart;
    }

    public void setDeletedPart(WSMethodDescr.MessagePartDescr deletedPart) {
        this.deletedPart = deletedPart;
    }

    public boolean isTypeChanged() {
        return typeChanged;
    }

    public void setTypeChanged(boolean typeChanged) {
        this.typeChanged = typeChanged;
    }

    public TypeDescriptor getOldTypeDescr() {
        return oldTypeDescriptor;
    }

    public void setOldTypeDescr(TypeDescriptor oldTypeDiffInfo) {
        this.oldTypeDescriptor = oldTypeDiffInfo;
    }

    public WSMethodDescr.MessagePartDescr getMsgPart() {
        return msgPart;
    }

    public void setMsgPart(WSMethodDescr.MessagePartDescr msgPart) {
        this.msgPart = msgPart;
    }
}
