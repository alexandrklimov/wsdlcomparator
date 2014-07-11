package ru.aklimov.wsdlcomparator.domain.diff.impl;

import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfo;

/**
 * @author Alexandr Klimov
 */
public class DiffWSMethodInfo implements IDiffInfo{
    //Request must be presented
    static public enum WSMETHOD_CHANGE_TYPE{
        NEW,
        DELETE,
        CHANGE_MESSAGE_PART,
        RESPONSE_ADD,
        RESPONSE_DEL
    }

    private WSMethodDescr wsMethodDescr;
    private TypeDiffInfo requestTypeDiffInfo;
    private TypeDiffInfo responseTypeDiffInfo;
    private WSMETHOD_CHANGE_TYPE changeType;
    /**This field contain a table describes a type of deleted response**/
    private TypeDescriptor deletedReponseType;

    @Override
    public String toString(){
        String methodName = (wsMethodDescr!=null)?wsMethodDescr.getMethodName():null;
        StringBuilder sb = new StringBuilder();
        sb.append("Diff info about "+wsMethodDescr.getMethodName()+" method");
        sb.append("\n\tRequest is changed: "+(requestTypeDiffInfo !=null) );
        sb.append("\n\tResponse is changed: "+(responseTypeDiffInfo !=null) );
        sb.append("\n\tChange type is: "+changeType );
        return sb.toString();
    }


    public WSMethodDescr getWsMethodDescr() {
        return wsMethodDescr;
    }

    public void setWsMethodDescr(WSMethodDescr wsMethodDescr) {
        this.wsMethodDescr = wsMethodDescr;
    }

    public TypeDiffInfo getRequestTypeDiffInfo() {
        return requestTypeDiffInfo;
    }

    public void setRequestTypeDiffInfo(TypeDiffInfo requestTypeDiffInfo) {
        this.requestTypeDiffInfo = requestTypeDiffInfo;
    }

    public TypeDiffInfo getResponseTypeDiffInfo() {
        return responseTypeDiffInfo;
    }

    public void setResponseTypeDiffInfo(TypeDiffInfo responseTypeDiffInfo) {
        this.responseTypeDiffInfo = responseTypeDiffInfo;
    }

    public WSMETHOD_CHANGE_TYPE getChangeType() {
        return changeType;
    }

    public void setChangeType(WSMETHOD_CHANGE_TYPE changeType) {
        this.changeType = changeType;
    }

    public TypeDescriptor getDeletedReponseType() {
        return deletedReponseType;
    }

    public void setDeletedReponseType(TypeDescriptor deletedReponseType) {
        this.deletedReponseType = deletedReponseType;
    }
}
