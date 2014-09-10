package ru.aklimov.wsdlcomparator.domain;

import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 05.07.13
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class CompareResult {
    Set<TypeDiffInfo> typesDiff;
    Set<GroupDiffInfo> groupsDiff;
    Set<WSMethodDiffInfo> wsMethodDiff;

    public CompareResult() {
    }

    public CompareResult(Set<TypeDiffInfo> typesDiff, Set<GroupDiffInfo> groupsDiff, Set<WSMethodDiffInfo> wsMethodDiff) {
        this.typesDiff = typesDiff;
        this.groupsDiff = groupsDiff;
        this.wsMethodDiff = wsMethodDiff;
    }

    public Set<TypeDiffInfo> getTypesDiff() {
        if(typesDiff == null){
            typesDiff = new HashSet<>();
        }
        return typesDiff;
    }

    public void setTypesDiff(Set<TypeDiffInfo> typesDiff) {
        this.typesDiff = typesDiff;
    }

    public Set<WSMethodDiffInfo> getWsMethodDiff() {
        if(wsMethodDiff == null){
            wsMethodDiff = new HashSet<>();
        }
        return wsMethodDiff;
    }

    public void setWsMethodDiff(Set<WSMethodDiffInfo> wsMethodDiff) {
        this.wsMethodDiff = wsMethodDiff;
    }

    public Set<GroupDiffInfo> getGroupsDiff() {
        if(groupsDiff == null){
            groupsDiff = new HashSet<>();
        }
        return groupsDiff;
    }

    public void setGroupsDiff(Set<GroupDiffInfo> groupsDiff) {
        this.groupsDiff = groupsDiff;
    }
}
