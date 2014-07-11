package ru.aklimov.wsdlcomparator.domain;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaType;
import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * This class contains information about a XSD-schema pre-processing.<br/>
 * The class is used as a data source for XSD comparison.<br/>
 *<br/>
 * In contrast to the <strong>xmlSchemaTypeSet</strong> field we don't need any xmlSchemaGroupSet because a set of
 * group definitions may be obtained from <strong>xsdQNameToGroupMap</strong> map.<br/>
 * It's because of a group can not be anonymously declared and each group is always identified by it's QName.
 */
public class XmlSchemaPreprocessRes {
    private Set<XmlSchemaType> xmlSchemaTypeSet;
    private Map<XmlSchemaType, OwnerInfo> typeToInitOwnerPair;
    private Map<QName, XmlSchemaElement> xsdQNameToElemMap;
    private Map<QName, XmlSchemaGroup> xsdQNameToGroupMap;

    /**For non anonymous type only!*/
    private Map<QName, XmlSchemaType> typeQNameToType;

    public XmlSchemaPreprocessRes(Set<XmlSchemaType> xmlSchemaTypeSet,
                                  Map<QName, XmlSchemaElement> xsdQNameToElemMap,
                                    Map<XmlSchemaType, OwnerInfo> typeToInitOwnerPair,
                                    Map<QName, XmlSchemaType> typeQNameToTypeMap,
                                    Map<QName, XmlSchemaGroup> xsdQNameToGroupMap) {

        this.xmlSchemaTypeSet = xmlSchemaTypeSet;
        this.typeToInitOwnerPair = typeToInitOwnerPair;
        this.typeQNameToType = typeQNameToTypeMap;
        this.xsdQNameToElemMap = xsdQNameToElemMap;
        this.xsdQNameToGroupMap = xsdQNameToGroupMap;

    }

    public Set<XmlSchemaType> getXmlSchemaTypeSet() {
        if(xmlSchemaTypeSet == null){
            xmlSchemaTypeSet = new HashSet<>();
        }
        return xmlSchemaTypeSet;
    }

    public Map<XmlSchemaType, OwnerInfo> getTypeToInitOwnerPair() {
        return typeToInitOwnerPair;
    }

    public Map<QName, XmlSchemaType> getTypeQNameToType() {
        if(typeQNameToType == null){
            typeQNameToType = new HashMap<>();
        }
        return typeQNameToType;
    }

    public Map<QName, XmlSchemaElement> getXsdQNameToElemMap() {
        if(xsdQNameToElemMap == null){
            xsdQNameToElemMap = new HashMap<>();
        }
        return xsdQNameToElemMap;
    }

    public Map<QName, XmlSchemaGroup> getXsdQNameToGroupMap(){
        if(xsdQNameToGroupMap == null){
            xsdQNameToGroupMap = new HashMap<>();
        }
        return xsdQNameToGroupMap;
    }

    public Set<XmlSchemaGroup> getXsdGroupSet(){
        if(xsdQNameToGroupMap != null){
            Set<XmlSchemaGroup> resSet = new HashSet<>();
            resSet.addAll(xsdQNameToGroupMap.values());
            return resSet;
        } else {
            return new HashSet<>();
        }

    }
}
