package ru.aklimov.wsdlcomparator.domain.tblmodel;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: AKlimov
 * Date: 15.09.13
 * Time: 5:47
 * To change this template use File | Settings | File Templates.
 */
public class IndicatorDescrRow {
    private String id;
    private List<String> changedAttributesNames;
    private List<TableRow> contentRows = new LinkedList<>();
    private boolean isNew;
    private boolean deleted;
    private boolean changedInContent;
    private boolean changedInAttributes;
    private long minOccurs;
    private long maxOccurs;
    private boolean unbounded;
    private String indicatorName;
    private boolean root;

    public List<TableRow> getContentRows() {
        if(contentRows ==null){
            contentRows = new LinkedList<>();
        }
        return contentRows;
    }

    public void setContentRows(List<TableRow> contentRows) {
        this.contentRows = contentRows;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isChangedInContent() {
        return changedInContent;
    }

    public void setChangedInContent(boolean changedInContent) {
        this.changedInContent = changedInContent;
    }

    public long getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(long minOccurs) {
        this.minOccurs = minOccurs;
    }

    public long getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(long maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isUnbounded() {
        return unbounded;
    }

    public void setUnbounded(boolean unbounded) {
        this.unbounded = unbounded;
    }

    public boolean isChangedInAttributes() {
        return changedInAttributes;
    }

    public void setChangedInAttributes(boolean changedInAttributes) {
        this.changedInAttributes = changedInAttributes;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<String> getChangedAttributesNames() {
        if(changedAttributesNames == null){
            changedAttributesNames = new LinkedList<>();
        }
        return changedAttributesNames;
    }

    public void setChangedAttributesNames(List<String> changedAttributesNames) {
        this.changedAttributesNames = changedAttributesNames;
    }
}
