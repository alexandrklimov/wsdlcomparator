package ru.aklimov.wsdlcomparator.domain.tblmodel.method;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class WSMethodDescrTable{

    private String methodName;
    private List<MessagePartDescrTable> inputMessage = new LinkedList<>();
    private List<MessagePartDescrTable> outputMessage = new LinkedList<>();
    /**@see ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE*/
    private String changeType;
    private List<MessagePartDescrTable> deletedOutputMessage;
    private String portTypeNamespace;
    private String portTypeName;


    @Override
    public int hashCode(){
        return Objects.hash(methodName, changeType);
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

    public List<MessagePartDescrTable> getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(List<MessagePartDescrTable> inputMessage) {
        this.inputMessage = inputMessage;
    }

    public List<MessagePartDescrTable> getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(List<MessagePartDescrTable> outputMessage) {
        this.outputMessage = outputMessage;
    }

    /**
     * @see ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE
     * @return String
     */
    public String getChangeType() {
        return changeType;
    }

    /**
     * @see ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE
     * @param changeType
     */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public List<MessagePartDescrTable> getDeletedOutputMessage() {
        return deletedOutputMessage;
    }

    public void setDeletedOutputMessage(List<MessagePartDescrTable> deletedOutputMessage) {
        this.deletedOutputMessage = deletedOutputMessage;
    }

    public String getPortTypeNamespace() {
        return portTypeNamespace;
    }

    public void setPortTypeNamespace(String portTypeNamespace) {
        this.portTypeNamespace = portTypeNamespace;
    }

    public String getPortTypeName() {
        return portTypeName;
    }

    public void setPortTypeName(String portTypeName) {
        this.portTypeName = portTypeName;
    }
}
