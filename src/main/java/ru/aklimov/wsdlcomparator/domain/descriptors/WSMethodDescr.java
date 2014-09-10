package ru.aklimov.wsdlcomparator.domain.descriptors;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
*@author Alexandr Klimov
 */
public class WSMethodDescr {
    private String methodName;
    private List<WSMethodDescr.MessagePartDescr> inputMessage = new LinkedList<>();
    private List<WSMethodDescr.MessagePartDescr> outputMessage = new LinkedList<>();
    private QName portTypeName;


    @Override
    public int hashCode() {
        return Objects.hash(methodName, portTypeName);
    }

    @Override
    public boolean equals(Object wsMethodDescrObj){
        if(this==wsMethodDescrObj){
            return true;
        }

        boolean res = false;
        //todo: make more clean
        if(wsMethodDescrObj instanceof WSMethodDescr){
            WSMethodDescr other = (WSMethodDescr) wsMethodDescrObj;
            boolean methodNameEq = (methodName==null && methodName==other.methodName) || ( (methodName!=null && other.methodName!=null) && (methodName.equals(other.methodName)) );
            boolean portTypeEq = (portTypeName==null && portTypeName==other.portTypeName) || ( (portTypeName!=null && other.portTypeName!=null) && (portTypeName.equals(other.portTypeName)) );
            res = methodNameEq && portTypeEq;
        }
        return res;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\tPORT TYPE NAME "+portTypeName);
        sb.append("\n\tWS METHOD "+methodName);
        sb.append("\n\tinputMessage: " + inputMessage.toString());
        sb.append("\n\toutputMessage: " + outputMessage.toString());
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

    public List<WSMethodDescr.MessagePartDescr> getInputMessage() {
        if(inputMessage == null){
            inputMessage = new LinkedList<>();
        }
        return inputMessage;
    }

    public List<WSMethodDescr.MessagePartDescr> getOutputMessage() {
        if(outputMessage == null){
            outputMessage = new LinkedList<>();
        }
        return outputMessage;
    }

    public QName getPortTypeName() {
        return portTypeName;
    }

    public void setPortTypeName(QName portTypeName) {
        this.portTypeName = portTypeName;
    }



    ///////////////// NESTED CLASS ///////////////////////////////////

    public static class MessagePartDescr{
        private String name;
        private boolean byElementBinding;
        private QName elemQName;
        private TypeDescriptor typeDescr;

        public MessagePartDescr(String name, boolean byElementBinding, QName elemQName, TypeDescriptor typeDescr) {
            this.name = name;
            this.byElementBinding = byElementBinding;
            this.elemQName = elemQName;
            this.typeDescr = typeDescr;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\tWS MESSAGE PART "+name);
            sb.append("\n\tby element binding: "+ byElementBinding);
            sb.append("\n\t\telement QName: "+elemQName);
            sb.append("\n\ttype descriptor: "+typeDescr);
            sb.append("\n");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MessagePartDescr that = (MessagePartDescr) o;

            if (byElementBinding != that.byElementBinding) return false;
            if (elemQName != null ? !elemQName.equals(that.elemQName) : that.elemQName != null) return false;
            if (!name.equals(that.name)) return false;
            if (!typeDescr.equals(that.typeDescr)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (byElementBinding ? 1 : 0);
            result = 31 * result + (elemQName != null ? elemQName.hashCode() : 0);
            result = 31 * result + typeDescr.hashCode();
            return result;
        }

        public String getName() {
            return name;
        }

        public boolean isByElementBinding() {
            return byElementBinding;
        }

        public QName getElemQName() {
            return elemQName;
        }

        public TypeDescriptor getTypeDescr() {
            return typeDescr;
        }

    }
}
