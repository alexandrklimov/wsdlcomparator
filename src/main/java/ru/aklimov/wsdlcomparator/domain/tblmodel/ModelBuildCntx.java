package ru.aklimov.wsdlcomparator.domain.tblmodel;

import com.google.common.collect.ImmutableMap;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * It's auxiliary class.
 * Main goal of this class is reducing a number of parameters of a model building method and collect
 * GroupDescrTable and TypeDescrTable that will be built.<br/>
 *
 * @author Alexandr Klimov
 */
public class ModelBuildCntx {
    private  TypeDescriptor td;
    private GroupDescriptor gd;
    private ImmutableMap<TypeDescriptor, TypeDiffInfo> dfTypeMap;
    private ImmutableMap<GroupDescriptor, GroupDiffInfo> dfGroupMap;
    private Set<TypeDescrTable> tableTypeSet = new HashSet<>();
    private Set<GroupDescrTable> tableGroupSet = new HashSet<>();
    private boolean elementAsRow;
    private boolean mergeWithBaseType;
    /**Whether content of a group should be displayed in a scope built model row.*/
    private boolean includeRefGroup;
    private int deepCount;

    /**
     * @param td
     * @param dfTypeMap
     * @param elementAsRow if true then elements of a type/group are processed otherwise attributes are processed.
     * @param tableTypeSet
     * @param mergeWithBaseType whether merge will be performed with the rows of a base type
     * @param includeRefGroup whether referenced group content will be included in rows
     * @param deepCount
     */
    public ModelBuildCntx(final TypeDescriptor td,
                            final GroupDescriptor gd,
                                final Map<TypeDescriptor, TypeDiffInfo> dfTypeMap,
                                    final Map<GroupDescriptor, GroupDiffInfo> dfGroupMap,
                                        final Set<TypeDescrTable> tableTypeSet,
                                            final Set<GroupDescrTable> tableGroupSet,
                                                final boolean mergeWithBaseType,
                                                    final boolean includeRefGroup,
                                                        final int deepCount,
                                                            final boolean elementAsRow) {
        if(td != null && gd != null){
            throw new IllegalArgumentException("TypeDescriptor and GroupDescriptor can't be processed at the same time.");
        }

        this.td = td;
        this.gd = gd;
        this.dfTypeMap = (dfTypeMap==null) ? ImmutableMap.<TypeDescriptor, TypeDiffInfo>of() : ImmutableMap.copyOf(dfTypeMap);
        this.dfGroupMap = (dfGroupMap==null) ? ImmutableMap.<GroupDescriptor, GroupDiffInfo>of() : ImmutableMap.copyOf(dfGroupMap);
        this.elementAsRow = elementAsRow;
        this.tableTypeSet = (tableTypeSet==null)?new HashSet<TypeDescrTable>() : tableTypeSet;
        this.tableGroupSet = (tableGroupSet==null)?new HashSet<GroupDescrTable>() : tableGroupSet;
        this.mergeWithBaseType = mergeWithBaseType;
        this.includeRefGroup = includeRefGroup;
        this.deepCount = deepCount;
    }


    public ModelBuildCntx switchContextForNewTd(TypeDescriptor td, int newDeepCount){
        return new ModelBuildCntx(
                td,
                null,
                this.dfTypeMap,
                this.dfGroupMap,
                this.tableTypeSet,
                this.tableGroupSet,
                this.mergeWithBaseType,
                this.includeRefGroup,
                newDeepCount,
                true);

    }

    /**
     * Returns a new context is used for building rows that presents a type attributes.<br/>
     * Set <strong>elementAsRow</strong> field equals <strong>FALSE</strong>
     *
     * @return
     */
    public ModelBuildCntx switchContextForTdAttrs(){
        return new ModelBuildCntx(
                this.td,
                null,
                this.dfTypeMap,
                this.dfGroupMap,
                this.tableTypeSet,
                this.tableGroupSet,
                this.mergeWithBaseType,
                this.includeRefGroup,
                this.deepCount,
                false);

    }


    public ModelBuildCntx switchContextForNewGd(GroupDescriptor gd, int newDeepCount){
        return new ModelBuildCntx(
                null,
                gd,
                this.dfTypeMap,
                this.dfGroupMap,
                this.tableTypeSet,
                this.tableGroupSet,
                this.mergeWithBaseType,
                this.includeRefGroup,
                newDeepCount,
                true);

    }


    public TypeDescriptor getTd() {
        return td;
    }

    public Map<TypeDescriptor, TypeDiffInfo> getDfTypeMap() {
        return dfTypeMap;
    }

    public Map<GroupDescriptor, GroupDiffInfo> getDfGroupMap() {
        return dfGroupMap;
    }

    public boolean isElementAsRow() {
        return elementAsRow;
    }

    public Set<TypeDescrTable> getTableTypeSet() {
        return tableTypeSet;
    }

    public Set<GroupDescrTable> getTableGroupSet() {
        return tableGroupSet;
    }

    public boolean isMergeWithBaseType() {
        return mergeWithBaseType;
    }

    public boolean isIncludeRefGroup() {
        return includeRefGroup;
    }

    public int getDeepCount() {
        return deepCount;
    }

    public void setDeepCount(int newCountVal){
        this.deepCount = newCountVal;
    }

    public GroupDescriptor getGd() {
        return gd;
    }

    public void setGd(GroupDescriptor gd) {
        this.gd = gd;
    }
}
