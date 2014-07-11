package ru.aklimov.wsdlcomparator.domain.tblmodel;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains a result of view models building
 *
 * @author Alexandr Klimov
 */
public class ModelBuildResult {
    private Set<TypeDescrTable> tableTypeSet;
    private Set<GroupDescrTable> tableGroupSet;

    public ModelBuildResult() {
    }

    public ModelBuildResult(Set<TypeDescrTable> tableTypeSet, Set<GroupDescrTable> tableGroupSet) {
        this.tableTypeSet = tableTypeSet;
        this.tableGroupSet = tableGroupSet;
    }

    public Set<TypeDescrTable> getTableTypeSet() {
        if(tableTypeSet == null){
            tableTypeSet = new HashSet<>();
        }
        return tableTypeSet;
    }

    public void setTableTypeSet(Set<TypeDescrTable> tableTypeSet) {
        this.tableTypeSet = tableTypeSet;
    }

    public Set<GroupDescrTable> getTableGroupSet() {
        if(tableGroupSet == null){
            tableGroupSet = new HashSet<>();
        }
        return tableGroupSet;
    }

    public void setTableGroupSet(Set<GroupDescrTable> tableGroupSet) {
        this.tableGroupSet = tableGroupSet;
    }
}
