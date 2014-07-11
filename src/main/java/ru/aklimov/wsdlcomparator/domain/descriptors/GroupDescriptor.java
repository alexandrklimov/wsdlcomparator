package ru.aklimov.wsdlcomparator.domain.descriptors;

import org.apache.ws.commons.schema.XmlSchemaGroupParticle;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes a <strong>group</strong> W3C XmlSchema tag.<br/>
 * This class <strong>isn't contained</strong> in an {@link ru.aklimov.wsdlcomparator.domain.descriptors.IndicatorDescriptor}
 * child items collection <strong>directly</strong>, but one is presented by {@link ru.aklimov.wsdlcomparator.domain.descriptors.GroupReference}
 * class instance there.
 * <br/>
 * GroupDescriptor is a reduced TypeDescriptor in some sense. It does not have any contentModel, complexContentType,
 * attributes and so on.<br/>
 * <br/>
 * It's a wrapper around an group indicator but it can not be replaced by an IndicatorDescriptor because of GROUP has
 * own attributes and may be shared by many complex types.<br/>
 * On the other hand an IndicatorDescriptor may be owned by one parent only: either a type, or a group, or a parent group indicator)
 *
 * @author Alexandr Klimov
 */
public class GroupDescriptor {
    private String name;
    private QName qName;
    private String id;
    private List<AnnotationDescriptor> annotations = new LinkedList<>();
    private Class<? extends XmlSchemaGroupParticle> rootIndicatorType;
    private IndicatorDescriptor rootIndDescr;
    private List<OwnerInfo> refBy;


    ////////////////// CONSTRUCTORS ////////////////////////

    public GroupDescriptor(){}

    public GroupDescriptor(QName qName) {
        this.name = qName.getLocalPart();
        this.qName = qName;
        this.id = createGroupDescriptorId(this);
    }


    ////////////////// METHODS ////////////////////////

    /**
     * This method check equality in a common sense: QName is compared only.<br/>
     * We are not interested in both any difference between root group indicator type and any difference between attributes.<br/>
     * We are not interested in any differences between group indicators tree in-deep too.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if(obj==null){
            return false;

        }else if(this==obj){
            return true;

        } else {
            if( ! (obj instanceof GroupDescriptor) ){
                return false;

            } else {
                GroupDescriptor other = (GroupDescriptor) obj;
                boolean qNameBothNull = (this.qName==null) && (this.qName==other.qName);
                boolean qNameBothNotNull = this.qName!=null && other.qName!=null;
                boolean qNameBothNotNullEq = false;
                if(qNameBothNotNull){
                    qNameBothNotNullEq = this.qName.equals(other.qName);
                }

                boolean qNameEq = (qNameBothNull || qNameBothNotNullEq);
                return qNameEq;
            }

        }
    }

    @Override
    public int hashCode() {
        return (qName.toString() + rootIndicatorType.getName()).hashCode();
    }

    @Override
    public String toString() {
        if(id==null){
            return super.toString();
        } else {
            return id;
        }
    }

////////////////// GETTERS/SETTERS ////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QName getQName() {
        return qName;
    }

    public void setQName(QName qName) {
        this.qName = qName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AnnotationDescriptor> getAnnotations() {
        if(annotations==null){
            annotations = new LinkedList<>();
        }
        return annotations;
    }

    public void setAnnotations(List<AnnotationDescriptor> annotations) {
        this.annotations = annotations;
    }

    public Class<? extends XmlSchemaGroupParticle> getRootIndicatorType() {
        return rootIndicatorType;
    }

    public void setRootIndicatorType(Class<? extends XmlSchemaGroupParticle> rootIndicatorType) {
        this.rootIndicatorType = rootIndicatorType;
    }

    public IndicatorDescriptor getRootIndDescr() {
        return rootIndDescr;
    }

    public void setRootIndDescr(IndicatorDescriptor rootIndDescr) {
        this.rootIndDescr = rootIndDescr;
    }

    public List<OwnerInfo> getRefBy() {
        if(refBy == null){
            refBy = new LinkedList<>();
        }
        return refBy;
    }

    public void setRefBy(List<OwnerInfo> refBy) {
        this.refBy = refBy;
    }

    static public String createGroupDescriptorId(GroupDescriptor gd){
        return gd.getQName().toString();
    }
}
