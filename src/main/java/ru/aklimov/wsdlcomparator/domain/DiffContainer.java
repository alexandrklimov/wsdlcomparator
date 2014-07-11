package ru.aklimov.wsdlcomparator.domain;

import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by aklimov on 05.02.14.
 */
public class DiffContainer {

    private Set<GroupDiffInfo> groupDiffInfos;
    private Set<TypeDiffInfo> typeDiffInfos;

    public DiffContainer() {
    }

    public DiffContainer(Set<GroupDiffInfo> groupDiffInfos, Set<TypeDiffInfo> typeDiffInfos) {
        this.groupDiffInfos = groupDiffInfos;
        this.typeDiffInfos = typeDiffInfos;
    }

    public Set<GroupDiffInfo> getGroupDiffInfos() {
        if(groupDiffInfos == null){
            groupDiffInfos = new HashSet<>();
        }
        return groupDiffInfos;
    }

    public void setGroupDiffInfos(Set<GroupDiffInfo> groupDiffInfos) {
        this.groupDiffInfos = groupDiffInfos;
    }

    public Set<TypeDiffInfo> getTypeDiffInfos() {
        if(typeDiffInfos == null){
            typeDiffInfos = new HashSet<>();
        }
        return typeDiffInfos;
    }

    public void setTypeDiffInfos(Set<TypeDiffInfo> typeDiffInfo) {
        this.typeDiffInfos = typeDiffInfo;
    }
}
