package ru.aklimov.wsdlcomparator.domain.diff.impl;

import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexandr Klimov
 */
public class WSMethodDiffInfo implements IDiffInfo{

    static public enum WSMETHOD_CHANGE_TYPE{
        NEW,
        DELETE,
        CHANGE_MESSAGE_PART,
        RESPONSE_ADD,
        RESPONSE_DEL
    }

    private WSMethodDescr wsMethodDescr;
    private WSMETHOD_CHANGE_TYPE changeType;
    /**
     * Indicates that any existing part (not newly created one) has been moved to a new position in a list of parts
     * */
    private boolean inMsgPartReordered;
    /**
     * Indicates that any existing part (not newly created one) has been moved to a new position in a list of parts
     */
    private boolean outMsgPartReordered;
    private Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> changedInMsgParts = new HashMap<>();
    private Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> changedOutMsgParts = new HashMap<>();
    private List<WSMethodDescr.MessagePartDescr> deletedOutputMessage = new LinkedList<>();


    @Override
    public String toString(){
        String methodName = (wsMethodDescr!=null)?wsMethodDescr.getMethodName():null;
        StringBuilder sb = new StringBuilder();
        sb.append("Diff info about "+wsMethodDescr.getMethodName()+" method");
        sb.append("\n\tRequest is changed: "+(changedInMsgParts !=null) );
        sb.append("\n\tResponse is changed: "+(changedOutMsgParts !=null) );
        sb.append("\n\tChange type is: "+changeType );
        return sb.toString();
    }


    public WSMethodDescr getWsMethodDescr() {
        return wsMethodDescr;
    }

    public void setWsMethodDescr(WSMethodDescr wsMethodDescr) {
        this.wsMethodDescr = wsMethodDescr;
    }

    public WSMETHOD_CHANGE_TYPE getChangeType() {
        return changeType;
    }

    public void setChangeType(WSMETHOD_CHANGE_TYPE changeType) {
        this.changeType = changeType;
    }

    public boolean isInMsgPartReordered() {
        return inMsgPartReordered;
    }

    public void setInMsgPartReordered(boolean inMsgPartReordered) {
        this.inMsgPartReordered = inMsgPartReordered;
    }

    public boolean isOutMsgPartReordered() {
        return outMsgPartReordered;
    }

    public void setOutMsgPartReordered(boolean outMsgPartReordered) {
        this.outMsgPartReordered = outMsgPartReordered;
    }

    public Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> getChangedInMsgParts() {
        if(changedInMsgParts == null){
            changedInMsgParts = new HashMap<>();
        }
        return changedInMsgParts;
    }

    public Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> getChangedOutMsgParts() {
        if(changedOutMsgParts == null){
            changedOutMsgParts = new HashMap<>();
        }
        return changedOutMsgParts;
    }

    public List<WSMethodDescr.MessagePartDescr> getDeletedOutputMessage() {
        return deletedOutputMessage;
    }
}
