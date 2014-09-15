package ru.aklimov.wsdlcomparator.domain.descriptors;

import org.apache.ws.commons.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 20.04.13
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public class TypeDescriptor {
    static private Logger log = LoggerFactory.getLogger(TypeDescriptor.class);

    //////////// ordinal fields ///////
    /**This field is filled by the parametrized constructor*/
    private String id;
    private boolean isComplexType;
    private boolean isAnonymous;
    private String name;
    private QName qName;

    private String baseTypeName;
    private QName baseTypeQName;
    private TypeDescriptor baseType;

    /**
     * This field is useful for anonymous types in particular because ones have not got a QName and we can't obtain its namespace from
     * the QName.<br/>
     * <br/>
     * This field is set for both a simple type and a complex type.<br/>
     * This field has a value of a scheme target namespace in which the described type is defined.
     *
     * */
    private String namespaceURI;

    private List<OwnerInfo> ownerInfoLst = new LinkedList<>();
    private List<AnnotationDescriptor> annotations = new LinkedList<>();

    //// Simple type part ////

    /**It corresponds to simpleContent / complexContent child tag of a ComplexType tag*/
    private Class<? extends XmlSchemaContentModel> contentModelType;
    private Class<? extends XmlSchemaSimpleTypeContent> simpleContentType;
    private XmlSchemaFacet[] facets;
    private boolean isBaseXsdType;

    //// Complex type part ////

    /**if this field != null then a complex type definition has either <restriction>.. or <extension>.. child tag*/
    private Class<? extends XmlSchemaContent> complexContentType;
    /**presents either all, or choice, or sequence indicator declaration*/
    private Class<? extends XmlSchemaGroupParticle> rootIndicatorType;

    /**
     * Must be NULL if <strong>rootGroupRef</strong> is set.
     * A complex type can't contain both own group root indicator (ALL, CHOICE, SEQUENCE)
     * and a reference to a group
     */
    private IndicatorDescriptor rootIndicator;

    /**
     * Must be NULL if <strong>rootIndicator</strong> is set.
     * A complex type can't contain both own group root indicator (ALL, CHOICE, SEQUENCE)
     * and a reference to a group
     */
    private GroupReference rootGroupRef;

    private List<AttributeDescriptor> attributes;


    ///////////// CONSTRUCTORS ////////////////////////

    public TypeDescriptor(){}

    public TypeDescriptor(boolean isComplexType, QName qName, String name, String namespaceURI, OwnerInfo ownerInfo){
        if(name == null){
            isAnonymous = true;
        } else{
            this.name = name;
        }
        this.isComplexType = isComplexType;
        this.qName = qName;
        if(ownerInfo != null){
            this.getOwnerInfoLst().add(ownerInfo);
        }
        this.namespaceURI = namespaceURI;
        this.id = createTypeDescriptorId(this);

    }

    ///////////// METHODS ////////////////////////

    /**
     * Equals method compares instances of TypeDescriptor objects for equals in common since - the descriptors describe
     * the same type.
     * Any inner differents of these descriptors are unimportant.
     *
     * Equals made by following steps:
     * - compare anonymous flag
     * - compare complex flag
     * - compare QName, if these types are not anonymous.
     * - compare owner element name, if these types are anonymous
     *
     * @param thatTdObj
     * @return boolean
     */
    @Override
    public boolean equals(Object thatTdObj){
        if(this==thatTdObj){
            return true;
        }

        if(! (thatTdObj instanceof TypeDescriptor) ){
            return false;
        } else {
            TypeDescriptor thatTd = (TypeDescriptor) thatTdObj;
            boolean res = false;
            if( this.isAnonymous == thatTd.isAnonymous &&
                this.isBaseXsdType == thatTd.isBaseXsdType ){

                if(this.isComplexType == thatTd.isComplexType){
                    if(!this.isAnonymous){
                        if( this.qName.equals(thatTd.qName) ){
                            res = true;
                        }
                    } else {
                        //An anonymous type can have only one owner
                        String thisElemName = this.ownerInfoLst.get(0).getElemName();
                        String thatElemName = thatTd.ownerInfoLst.get(0).getElemName();
                        if(thisElemName.equals(thatElemName)){
                            res = true;
                        }
                    }
                }

            }
            return res;

        }

    }

    @Override
    public int hashCode(){
        String strForHash = name + isAnonymous + isBaseXsdType + isComplexType + qName;
        return strForHash.hashCode();
    }

    @Override
    public String toString(){
        if(id != null){
            return id;
        } else {
            return super.toString();
        }
    }

    /**
     * This function returns a string value of a type QName or
     * <strong>"owner_type_qname-type|"+<i>owner_element_name-element</i>+"|of_anon_type"</strong>,
     * if a described type is anonymous
     *
     * @param td a processed type descriptor.
     * @return a string presents an ID of a TypeDescriptor
     */
    static public String createTypeDescriptorId(TypeDescriptor td){
        QName typeQName = td.getQName();
        if(typeQName!=null){
            return typeQName.toString();

        } else {
            String elemName = td.getOwnerInfoLst().get(0).getElemName();
            TypeDescriptor ownerType = td.getOwnerInfoLst().get(0).getTypeDescriptor();

            String typeName = "";
            if(ownerType!=null && !ownerType.isAnonymous()){
                typeName = ownerType.getQName().toString();
            } else if(ownerType!=null && ownerType.isAnonymous()) {
                typeName = ownerType.toString();
            }

            String resStr = typeName + "_-type|" + elemName + "-element" + "|of_anon_type";
            log.debug("typeQName: " + typeQName + "; tableId: " + resStr);
            return resStr;
        }
    }

    ///////////// getters - setters //////////////

    public TypeDescriptor getBaseType() {
        return baseType;
    }

    public void setBaseType(TypeDescriptor baseType) {
        this.baseType = baseType;
    }

    public boolean isBaseXsdType() {
        return isBaseXsdType;
    }

    public void setBaseXsdType(boolean baseXsdType) {
        isBaseXsdType = baseXsdType;
    }

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

    public String getBaseTypeName() {
        return baseTypeName;
    }

    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }

    public QName getBaseTypeQName() {
        return baseTypeQName;
    }

    public void setBaseTypeQName(QName baseTypeQName) {
        this.baseTypeQName = baseTypeQName;
    }

    public Class<? extends XmlSchemaSimpleTypeContent> getSimpleContentType() {
        return simpleContentType;
    }

    public void setSimpleContentType(Class<? extends XmlSchemaSimpleTypeContent> simpleContentType) {
        this.simpleContentType = simpleContentType;
    }

    public Class<? extends XmlSchemaContent> getComplexContentType() {
        return complexContentType;
    }

    public void setComplexContentType(Class<? extends XmlSchemaContent> complexContentType) {
        this.complexContentType = complexContentType;
    }

    public Class<? extends XmlSchemaGroupParticle> getRootIndicatorType() {
        return rootIndicatorType;
    }

    public void setRootIndicatorType(Class<? extends XmlSchemaGroupParticle> rootIndicatorType) {
        this.rootIndicatorType = rootIndicatorType;
    }

    public XmlSchemaFacet[] getFacets() {
        return facets;
    }

    public void setFacets(XmlSchemaFacet[] facets) {
        this.facets = facets;
    }

    public IndicatorDescriptor getRootIndicator() {
        return rootIndicator;
    }

    public void setRootIndicator(IndicatorDescriptor rootIndicator) {
        this.rootIndicator = rootIndicator;
        this.rootGroupRef = null;
    }

    public List<AttributeDescriptor> getAttributes() {
        if(attributes==null){
            attributes = new LinkedList<>();
        }
        return attributes;
    }

    public void setAttributes(List<AttributeDescriptor> attrSet) {
        this.attributes = attrSet;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public List<OwnerInfo> getOwnerInfoLst() {
        if(ownerInfoLst ==null){
            ownerInfoLst = new LinkedList<>();
        }
        return ownerInfoLst;
    }

    public void setOwnerInfoLst(List<OwnerInfo> ownerInfoLst) {
        this.ownerInfoLst = ownerInfoLst;
    }

    public boolean isComplexType() {
        return isComplexType;
    }

    public void setComplexType(boolean complexType) {
        isComplexType = complexType;
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

    /**
     * @see TypeDescriptor#namespaceURI
     * @return String
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class<? extends XmlSchemaContentModel> getContentModelType() {
        return contentModelType;
    }

    public void setContentModelType(Class<? extends XmlSchemaContentModel> contentModelType) {
        this.contentModelType = contentModelType;
    }

    public GroupReference getRootGroupRef() {
        return rootGroupRef;
    }

    public void setRootGroupRef(GroupReference rootGroupRef) {
        this.rootGroupRef = rootGroupRef;
        rootIndicator = null;
        rootIndicatorType = null;
    }
}
