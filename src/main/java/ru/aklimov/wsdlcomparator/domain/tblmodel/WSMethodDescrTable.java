package ru.aklimov.wsdlcomparator.domain.tblmodel;


public class WSMethodDescrTable{

    private String methodName;
    private TypeDescrTable requestParams;
    /**This field may be null if a response type is void*/
    private TypeDescrTable responseParams;
    /**@see ru.aklimov.wsdlcomparator.domain.diff.impl.DiffWSMethodInfo.WSMETHOD_CHANGE_TYPE*/
    private String changeType;
    private TypeDescrTable deletedResponseTable;


    @Override
    public int hashCode(){
        String requestParamsStr = (requestParams==null)?"":requestParams.toString();
        String responseParamsStr = (responseParams==null)?"":responseParams.toString();
        return ( ""+methodName+changeType+requestParamsStr+responseParamsStr ).hashCode();
    }

    @Override
    public boolean equals(Object wsMethodDescrObj){
        if(this==wsMethodDescrObj){
            return true;
        }

        boolean res = false;
        if(wsMethodDescrObj instanceof WSMethodDescrTable){
            WSMethodDescrTable wsMethodDescrTable = (WSMethodDescrTable) wsMethodDescrObj;
            boolean methodNameEq = (methodName==null && methodName== wsMethodDescrTable.methodName) || methodName.equals(wsMethodDescrTable.methodName);
            boolean methodNamespaceEq = (changeType==null && changeType== wsMethodDescrTable.changeType) || changeType.equals(wsMethodDescrTable.changeType);
            res = methodNameEq && methodNamespaceEq;
        }
        return res;
    }


    ///////////////// GETTERS AND SETTERS ///////////////////////////////////

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public TypeDescrTable getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(TypeDescrTable requestParams) {
        this.requestParams = requestParams;
    }

    public TypeDescrTable getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(TypeDescrTable responseParams) {
        this.responseParams = responseParams;
    }

    /**
     * @see ru.aklimov.wsdlcomparator.domain.diff.impl.DiffWSMethodInfo.WSMETHOD_CHANGE_TYPE
     * @return String
     */
    public String getChangeType() {
        return changeType;
    }

    /**
     * @see ru.aklimov.wsdlcomparator.domain.diff.impl.DiffWSMethodInfo.WSMETHOD_CHANGE_TYPE
     * @param changeType
     */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public TypeDescrTable getDeletedResponseTable() {
        return deletedResponseTable;
    }

    public void setDeletedResponseTable(TypeDescrTable deletedResponseTable) {
        this.deletedResponseTable = deletedResponseTable;
    }
}
