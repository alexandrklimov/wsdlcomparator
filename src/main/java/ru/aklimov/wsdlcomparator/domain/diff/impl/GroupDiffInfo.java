package ru.aklimov.wsdlcomparator.domain.diff.impl;

import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.IndicatorDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfoWithAffected;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexandr Klimov
 */
public class GroupDiffInfo implements IDiffInfoWithAffected {
    private GroupDescriptor grpDescr;
    private boolean isNew;
    private boolean removed;

    private Map<ParticleContent, ChangeInfoDetails> affectedItems;
    //TODO: It this field is used now anywhere?
    private Map<String, ChangeInfoDetails> affectedAttributes;

    private boolean changedRootIndicatorType;
    private IndicatorDescriptor oldRootIndicator;


    ////////////// CONSTRUCTORS ///////////////////////

    public GroupDiffInfo(boolean isNew, GroupDescriptor grpDescr) {
        this.isNew = isNew;
        this.grpDescr = grpDescr;
    }

    public GroupDiffInfo(GroupDescriptor grpDescr) {
        this.grpDescr = grpDescr;
    }

    public GroupDiffInfo(GroupDescriptor grpDescr, Map<ParticleContent, ChangeInfoDetails> affectedItems) {
        this.grpDescr = grpDescr;
        this.affectedItems = affectedItems;
    }

    //////////////////// METHODS ///////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupDiffInfo that = (GroupDiffInfo) o;

        if (!grpDescr.equals(that.grpDescr)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return grpDescr.hashCode();
    }


    ////////////// GETTERS/SETTERS ///////////////////////

    public GroupDescriptor getGrpDescr() {
        return grpDescr;
    }

    public void setGrpDescr(GroupDescriptor grpDescr) {
        this.grpDescr = grpDescr;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public Map<ParticleContent, ChangeInfoDetails> getAffectedItems() {
        if(affectedItems==null){
            affectedItems = new HashMap<>();
        }
        return affectedItems;
    }

    public void setAffectedItems(Map<ParticleContent, ChangeInfoDetails> affectedItems) {
        this.affectedItems = affectedItems;
    }

    public Map<String, ChangeInfoDetails> getAffectedAttributes() {
        if(affectedAttributes==null){
            affectedAttributes = new HashMap<>();
        }
        return affectedAttributes;
    }

    public void setAffectedAttributes(Map<String, ChangeInfoDetails> affectedAttributes) {
        this.affectedAttributes = affectedAttributes;
    }

    public boolean isChangedRootIndicatorType() {
        return changedRootIndicatorType;
    }

    public void setChangedRootIndicatorType(boolean changedRootIndicatorType) {
        this.changedRootIndicatorType = changedRootIndicatorType;
    }

    public IndicatorDescriptor getOldRootIndicator() {
        return oldRootIndicator;
    }

    public void setOldRootIndicator(IndicatorDescriptor oldRootIndicator) {
        this.oldRootIndicator = oldRootIndicator;
    }
}
