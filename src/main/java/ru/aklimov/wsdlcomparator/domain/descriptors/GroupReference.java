package ru.aklimov.wsdlcomparator.domain.descriptors;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexand Klimov
 */
public class GroupReference implements ParticleContent{
    private QName ref;
    private GroupDescriptor groupDescr;
    private List<OwnerInfo> ownerInfoLst = new LinkedList<>();
    private long minOccurs;
    private long maxOccurs;
    private boolean maxOccUnbound = false;

    public GroupReference(QName ref, GroupDescriptor groupDescr, long minOccurs, long maxOccurs) {
        this.ref = ref;
        this.groupDescr = groupDescr;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        if(Long.MAX_VALUE == maxOccurs){
            maxOccUnbound = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupReference that = (GroupReference) o;

        if (!ref.equals(that.ref)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    public QName getRef() {
        return ref;
    }

    public GroupDescriptor getGroupDescr() {
        return groupDescr;
    }

    public long getMinOccurs() {
        return minOccurs;
    }

    public long getMaxOccurs() {
        return maxOccurs;
    }

    public boolean isMaxOccUnbound() {
        return maxOccUnbound;
    }

    public List<OwnerInfo> getOwnerInfoLst() {
        if(ownerInfoLst==null){
            ownerInfoLst = new LinkedList<>();
        }
        return ownerInfoLst;
    }

    public void setOwnerInfoLst(List<OwnerInfo> ownerInfoLst) {
        this.ownerInfoLst = ownerInfoLst;
    }
}
