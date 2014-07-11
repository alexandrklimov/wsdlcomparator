package ru.aklimov.wsdlcomparator.domain.descriptors;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 04.07.13
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class WSMethodDescr {
    private String methodName;
    /**This field may be null if a method does not take any arguments*/
    private TypeDescriptor requestType;
    private String requestNamespace;
    /**This field may be null if a response type is void*/
    private TypeDescriptor responseType;
    private String responseNamespace;

    @Override
    public int hashCode(){
        String responseParamsStr = (responseType ==null)?"": responseType.toString();
        return ( ""+methodName+responseNamespace+requestNamespace+ requestType.toString()+responseParamsStr ).hashCode();
    }

    @Override
    public boolean equals(Object wsMethodDescrObj){
        if(this==wsMethodDescrObj){
            return true;
        }

        boolean res = false;
        //todo: make more clean
        if(wsMethodDescrObj instanceof WSMethodDescr){
            WSMethodDescr wsMethodDescr = (WSMethodDescr) wsMethodDescrObj;
            boolean methodNameEq = (methodName==null && methodName==wsMethodDescr.methodName) || ( (methodName!=null && wsMethodDescr.methodName!=null) && (methodName.equals(wsMethodDescr.methodName)) );
            boolean requestMethodNamespaceEq = (requestNamespace==null && requestNamespace==wsMethodDescr.requestNamespace) || (  (requestNamespace!=null && wsMethodDescr.requestNamespace!=null) && (requestNamespace.equals(wsMethodDescr.requestNamespace)) );
            boolean responseMethodNamespaceEq = (responseNamespace==null && responseNamespace==wsMethodDescr.responseNamespace) || ( (responseNamespace!=null && wsMethodDescr.responseNamespace!=null) && (responseNamespace.equals(wsMethodDescr.responseNamespace)) );
            res = methodNameEq && requestMethodNamespaceEq && responseMethodNamespaceEq;
        }
        return res;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\tWS METHOD "+methodName);

        if(requestType ==null){
            sb.append("\n\trequestType: "+ requestType.getName());
        }else {
            sb.append("\n\trequestType: "+ requestType);
        }

        sb.append("\n\trequestNamespace: "+requestNamespace);

        if(responseType ==null){
            sb.append("\n\tresponseType: "+ responseType);
        } else {
            sb.append("\n\tresponseType: "+ responseType.getName());
        }

        sb.append("\n\tresponseNamespace: "+responseNamespace);
        sb.append("\n");
        return sb.toString();
    }

    ///////////////// GETTERS AND SETTERS ///////////////////////////////////

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestNamespace() {
        return requestNamespace;
    }

    public void setRequestNamespace(String requestNamespace) {
        this.requestNamespace = requestNamespace;
    }

    public String getResponseNamespace() {
        return responseNamespace;
    }

    public void setResponseNamespace(String responseNamespace) {
        this.responseNamespace = responseNamespace;
    }

    public TypeDescriptor getRequestType() {
        return requestType;
    }

    public void setRequestType(TypeDescriptor requestType) {
        this.requestType = requestType;
    }

    public TypeDescriptor getResponseType() {
        return responseType;
    }

    public void setResponseType(TypeDescriptor responseType) {
        this.responseType = responseType;
    }

}
