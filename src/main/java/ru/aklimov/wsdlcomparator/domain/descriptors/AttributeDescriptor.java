package ru.aklimov.wsdlcomparator.domain.descriptors;

import org.apache.ws.commons.schema.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 *This class describes attributes of two kinds: user-defined and built-in.<br/>
 * <ul>
 * <li>User-defined attributes are ones described by an <strong>&lt;attribute/&gt;</strong> tag in a comlplexType.</li>
 * <li>Built-in attributes, on the other hand, are ones described by XSD specification and don't demand any description
 * in an user's XSD-schema.</li>
 * </ul>
 * Commonly this class isn't used for buit-in attributes description, but we necessary it for describe some changes of values
 * built-in attributes. It's because of {@link ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails ChangeInfoDetails}
 * can refers to a changed attribute by using AttributeDescription only.
 */
public class AttributeDescriptor {
    static private Logger log = LoggerFactory.getLogger(AttributeDescriptor.class);
    static public final String MIN_OCCURS = "minOccurs";
    static public final String MAX_OCCURS = "maxOccurs";
    static public final QName MIN_OCCURS_QNAME = QName.valueOf("{"+ Constants.URI_2001_SCHEMA_XSD+"}"+MIN_OCCURS);
    static public final QName MAX_OCCURS_QNAME = QName.valueOf("{"+ Constants.URI_2001_SCHEMA_XSD+"}"+MAX_OCCURS);

    /**
     * This field signals that the descriptor describes standard built-in attribute. F.e. minOccurs/maxOccurs/name ans so on.
     * This attributes are not defined by a user but are defined by a XSD specification.
     */
    private boolean buildIn;

    private String name;
    private QName qName;
    private String defaultVal;
    private String fixedVal;
    private TypeDescriptor typeDescr;
    private String use;
    private List<AnnotationDescriptor> annotations;

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

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
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

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    /**
     * todo: classify changed more precisely/ return more detail information.
     * @param other
     * @return boolean
     */
    public boolean attributeCompare(AttributeDescriptor other){
        log.debug("Comparing attributes of attributes");

        if(!this.equals(other)){
            log.debug("result: false");
            return false;
        }

        boolean builtInEq = (this.buildIn == other.buildIn);
        boolean fixValEq = (this.fixedVal==other.fixedVal) || (this.fixedVal!=null && this.fixedVal.equals(other.fixedVal));
        boolean defValEq = (this.defaultVal==other.defaultVal) || (this.defaultVal!=null && this.defaultVal.equals(other.defaultVal));
        boolean useEq = ( this.use==other.use ) || (this.use!=null && this.use.equals(other.use));

        boolean res = fixValEq && defValEq && useEq && builtInEq;

        log.debug("result: " + res);
        return res;
    }

    /**
     * Equals without TypeDescriptor comparing
     *
     * @param otherObj
     * @return boolean
     */
    @Override
    public boolean equals(Object otherObj){
        if(this==otherObj){
            return true;
        }

        boolean res = false;
        if(otherObj instanceof AttributeDescriptor){
            AttributeDescriptor other = (AttributeDescriptor) otherObj;
            res = this.qName.equals(other.qName);
        }

        return res;
    }

    @Override
    public int hashCode(){
        String strForHash = name + fixedVal + defaultVal + qName + new Boolean(buildIn);
        return strForHash.hashCode();
    }

    public void setAnnotations(List<AnnotationDescriptor> annotations) {
        this.annotations = annotations;
    }

    public List<AnnotationDescriptor> getAnnotations() {
        if(annotations==null){
            annotations = new LinkedList<>();
        }
        return annotations;
    }

    public boolean isBuildIn() {
        return buildIn;
    }

    public void setBuildIn(boolean buildIn) {
        this.buildIn = buildIn;
    }
}
