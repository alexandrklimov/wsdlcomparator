package ru.aklimov.wsdlcomparator.domain.descriptors;

import ru.aklimov.wsdlcomparator.domain.descriptors.AnnotationDescriptor;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: aklimov
* Date: 29.04.13
* Time: 11:27
* To change this template use File | Settings | File Templates.
*/
public class ElementDescriptor implements ParticleContent{
    private String name;
    private QName qname;
    private long minOccurs;
    private long maxOccurs;
    private boolean isMaxOccUnbound;
    private boolean nillable;
    private String defaultVal;
    private String fixedVal;
    /**This field indicates that the element is a template element using case by the "ref" element attribute*/
    private boolean byRef;
    private QName refName;
    private IndicatorDescriptor indDescr;
    private TypeDescriptor typeDescr;
    private List<AnnotationDescriptor> annotations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
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

    public boolean isMaxOccUnbound() {
        return isMaxOccUnbound;
    }

    public void setMaxOccUnbound(boolean maxOccUnbound) {
        isMaxOccUnbound = maxOccUnbound;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public boolean isByRef() {
        return byRef;
    }

    public void setByRef(boolean byRef) {
        this.byRef = byRef;
    }

    public QName getRefName() {
        return refName;
    }

    public void setRefName(QName refName) {
        this.refName = refName;
    }

    public String getFixedVal() {
        return fixedVal;
    }

    public void setFixedVal(String fixedVal) {
        this.fixedVal = fixedVal;
    }

    public TypeDescriptor getTypeDescr() {
        return typeDescr;
    }

    public void setTypeDescr(TypeDescriptor typeDescr) {
        this.typeDescr = typeDescr;
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

    public IndicatorDescriptor getIndDescr() {
        return indDescr;
    }

    public void setIndDescr(IndicatorDescriptor indDescr) {
        this.indDescr = indDescr;
    }

    /**
     * todo: classify changed more precisely/ return more detail information.
     * @param otherEd
     * @return boolean
     */
    public boolean attributeCompare(ElementDescriptor otherEd){
        if(!this.equals(otherEd)){
            return false;
        }

        boolean res =  this.maxOccurs==otherEd.maxOccurs && this.minOccurs==otherEd.minOccurs &&
        this.isMaxOccUnbound==otherEd.isMaxOccUnbound && this.nillable==otherEd.nillable &&
                ( this.defaultVal==otherEd.defaultVal || (this.defaultVal!=null&& this.defaultVal.equals(otherEd.defaultVal)) ) &&
                ( this.fixedVal==otherEd.fixedVal || (this.fixedVal!=null && this.fixedVal.equals(otherEd.fixedVal)) );

        return res;

    }


    /**
     * Equals without TypeDescriptor comparing
     *
     * @param otherEdObj
     * @return boolean
     */
    @Override
    public boolean equals(Object otherEdObj){
        if(this==otherEdObj){
            return true;
        }

        boolean res = false;
        if(otherEdObj instanceof ElementDescriptor){
            ElementDescriptor otherEd = (ElementDescriptor) otherEdObj;
            res = this.qname.equals(otherEd.qname);
        }

        return res;
    }

    @Override
    public int hashCode(){
        String strForHash = name + qname;
        return strForHash.hashCode();
    }
}
