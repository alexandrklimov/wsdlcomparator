package ru.aklimov.wsdlcomparator;

import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.ProcessCntx;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.XmlSchemaPreprocessRes;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * This class contains methods that perform either pre-processing xml-schema definition or processing about set of type descriptors obtaining
 * or XSD-types comparing.<br/>
 * These methods intended for building descriptors set or compare XSD-schemas.
 */
public class XmlSchemaProcessor {
    private static Logger log = LoggerFactory.getLogger(XmlSchemaProcessor.class);

    /**
     * This method performs some pre-processing of an xml-schema collection.<br/>
     * Result of this method is used for performing Xml-schemas comparison.
     *
     * @param xmlSchemaCollection
     * @return XmlSchemaPreprocessRes
     */
    public static XmlSchemaPreprocessRes xmlSchemaPreprocess(final XmlSchemaCollection xmlSchemaCollection){
        Set<XmlSchemaType> xsdTypes = new HashSet<>();
        Map<QName, XmlSchemaType> xsdQNameToTypeMap = new HashMap<>();
        Map<QName, XmlSchemaElement> xsdQNameToElemMap = new HashMap<>();
        Map<XmlSchemaType, OwnerInfo> typeToOwnerElems = new HashMap<>();
        Map<QName, XmlSchemaGroup> xsdQNameToGroupMap = new HashMap<>();

        XmlSchema[] schemas =  xmlSchemaCollection.getXmlSchemas();

        //TODO: Are we really using this item?
        XmlSchema baseXsdSchema = XsdBaseSchemaHolder.getSchema();
        //Init base xsd schema in the global cache
        if(baseXsdSchema==null){
            for(XmlSchema schema : schemas){
                if(Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())){
                    XsdBaseSchemaHolder.setSchema(schema);
                    baseXsdSchema = schema;
                    break;
                }
            }
        }

        //There is collecting all schema non-anonymous type declarations
        //and all top element declarations into a map
        //GROUP declarations is processed here too.
        log.debug("\n\tCOLLECTING ALL SCHEMA NON ANONYMOUS TYPE AND TOP ELEMENTS DECLARATIONS\n");
        for(XmlSchema schema : schemas){
            log.debug("Processed schema is " + schema.getTargetNamespace());
            log.debug("Collecting schema types into Map( QName->XmlSchemaType )");
            xsdQNameToTypeMap.putAll( schema.getSchemaTypes() );
            xsdTypes.addAll( xsdQNameToTypeMap.values() );

            log.debug("Collecting schema root elements into Map( QName->XmlSchemaElement ) ");
            xsdQNameToElemMap.putAll( schema.getElements() );

            log.debug("Collecting schema group declaration into Map( QName->XmlSchemaGroup ) ");
            xsdQNameToGroupMap.putAll( schema.getGroups() );
        }

        //Find all anonymous type declarations of root elements and owner root element for each type at the same time.
        log.debug("\n\tFIND OWNER ELEMENTS FOR EACH TYPE AND ANONYMOUS TYPE DECLARATIONS\n");
        Collection<XmlSchemaElement> rootElements = xsdQNameToElemMap.values();
        for(XmlSchemaElement elem : rootElements){
            log.debug("Element for processing: "+elem.getName());
            OwnerInfo pair = new OwnerInfo();
            pair.setElemName(elem.getQName().toString());

            XmlSchemaType elemSchemaType = elem.getSchemaType();
            //At this time a type may be null - XmlSchema:2.0.3 library presents xsd:anyType as null
            if(elemSchemaType==null){
                elemSchemaType = XsdBaseSchemaHolder.getAnyType();
            }

            typeToOwnerElems.put(elemSchemaType, pair);
            if( !xsdTypes.contains(elemSchemaType) ){
                xsdTypes.add(elemSchemaType);
            }

        }

        XmlSchemaPreprocessRes res = new XmlSchemaPreprocessRes(xsdTypes, xsdQNameToElemMap, typeToOwnerElems, xsdQNameToTypeMap, xsdQNameToGroupMap);
        return res;
    }

    public static DescriptorsContainer getXmlSchemaItemsDescriptors(final XmlSchemaCollection xmlSchemaCollection){
        if( xmlSchemaCollection==null ){
            throw new IllegalArgumentException("preprocessResult is null!");
        }

        XmlSchemaPreprocessRes preprocessRes = XmlSchemaProcessor.xmlSchemaPreprocess(xmlSchemaCollection);

        Map<XmlSchemaType, TypeDescriptor> typeDescriptorMap = new HashMap<>();
        Map<XmlSchemaGroup, GroupDescriptor> groupDescriptorMap = new HashMap<>();

        for(XmlSchemaGroup xsdGrp : preprocessRes.getXsdGroupSet() ){
            ProcessCntx context = new ProcessCntx(xsdGrp, preprocessRes, typeDescriptorMap, groupDescriptorMap);
            XmlSchemaItemsProcessor.getGroupDescriptor(context);
        }

        for(XmlSchemaType xsdType : preprocessRes.getXmlSchemaTypeSet() ){
            ProcessCntx context = new ProcessCntx(xsdType, preprocessRes, typeDescriptorMap, groupDescriptorMap);
            XmlSchemaItemsProcessor.getTypeDescriptor(context);
        }

        Set<TypeDescriptor> tdSet = new HashSet<>();
        tdSet.addAll( typeDescriptorMap.values() );
        Set<GroupDescriptor> gdSet = new HashSet<>();
        gdSet.addAll( groupDescriptorMap.values() );

        DescriptorsContainer dc = new DescriptorsContainer(tdSet, gdSet);

        return dc;
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final StreamSource streamSource,
                                                                   final URIResolver uriResolver){
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        if(uriResolver!=null){
            schemaCol.setSchemaResolver(uriResolver);
        }

        schemaCol.read(streamSource);
        return getXmlSchemaItemsDescriptors(schemaCol);
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final StreamSource streamSource){
        return getXmlSchemaTypeDescriptors(streamSource, null);
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final File xsdFile,
                                                                  final URIResolver uriResolver){
        DescriptorsContainer resultDescrContainer = null;

        try(FileInputStream fis = new FileInputStream( xsdFile)){
            StreamSource ss = new StreamSource( fis );
            resultDescrContainer = getXmlSchemaTypeDescriptors(ss, uriResolver);
        }catch (Exception ex){
            log.error("", ex);
        }

        return resultDescrContainer;
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final File xsdFile){
        DescriptorsContainer res = null;

        try(FileInputStream fis = new FileInputStream( xsdFile)){
            StreamSource ss = new StreamSource( fis );
            res = getXmlSchemaTypeDescriptors(ss, null);
        }catch (Exception ex){
            log.error("", ex);
        }

        return res;
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final InputStream inputStream,
                                                                   final URIResolver uriResolver){
        StreamSource ss = new StreamSource(inputStream);
        return getXmlSchemaTypeDescriptors(ss, uriResolver);
    }

    public static DescriptorsContainer getXmlSchemaTypeDescriptors(final InputStream inputStream){
        return getXmlSchemaTypeDescriptors(inputStream, null);
    }

    public static Set<TypeDiffInfo> compareXmlSchemaTypes(final XmlSchemaCollection newCollection, final XmlSchemaCollection oldCollection){
        if(newCollection==null){
            throw new IllegalArgumentException("newCollection is null.");
        }
        if(oldCollection==null){
            throw new IllegalArgumentException("oldCollection is null");
        }

        DescriptorsContainer newDescrContainer = getXmlSchemaItemsDescriptors(newCollection);
        DescriptorsContainer oldDescrContainer = getXmlSchemaItemsDescriptors(oldCollection);
        Set<TypeDiffInfo> xsdTypesDiffs = DifferenceComputer.getXSDItemsDiffs(newDescrContainer, oldDescrContainer).getTypeDiffInfos();
        return xsdTypesDiffs;
    }

    public static Set<TypeDiffInfo> compareXmlSchemaTypes(final File newXsdFile, final File oldXsdFile){
        if(newXsdFile==null){
            throw new IllegalArgumentException("newXsdFile is null.");
        }
        if(oldXsdFile==null){
            throw new IllegalArgumentException("oldXsdFile is null");
        }

        DescriptorsContainer newDescrContainer = getXmlSchemaTypeDescriptors(newXsdFile);
        DescriptorsContainer oldDescrContainer = getXmlSchemaTypeDescriptors(oldXsdFile);
        Set<TypeDiffInfo> xsdTypesDiffs = DifferenceComputer.getXSDItemsDiffs(newDescrContainer, oldDescrContainer).getTypeDiffInfos();
        return xsdTypesDiffs;
    }

    public static Set<TypeDiffInfo> compareXmlSchemaTypes(final InputStream newXsdInputStream, final InputStream oldXsdInputStream){
        if(newXsdInputStream==null){
            throw new IllegalArgumentException("newXsdInputStream is null.");
        }
        if(oldXsdInputStream==null){
            throw new IllegalArgumentException("oldXsdInputStream is null");
        }

        DescriptorsContainer newDescrContainer = getXmlSchemaTypeDescriptors(newXsdInputStream);
        DescriptorsContainer oldDescrContainer = getXmlSchemaTypeDescriptors(oldXsdInputStream);
        DifferenceComputer.getXSDItemsDiffs(newDescrContainer, oldDescrContainer);
        Set<TypeDiffInfo> xsdTypesDiffs = DifferenceComputer.getXSDItemsDiffs(newDescrContainer, oldDescrContainer).getTypeDiffInfos();
        return xsdTypesDiffs;
    }

    public static Set<TypeDiffInfo> compareXmlSchemaTypes(final StreamSource newXsdSS, final StreamSource oldXsdSS){
        if(newXsdSS==null){
            throw new IllegalArgumentException("newXsdSS is null.");
        }
        if(oldXsdSS==null){
            throw new IllegalArgumentException("oldXsdSS is null");
        }

        DescriptorsContainer newDescrContainer = getXmlSchemaTypeDescriptors(newXsdSS);
        DescriptorsContainer oldDescrContainer = getXmlSchemaTypeDescriptors(oldXsdSS);
        Set<TypeDiffInfo> xsdTypesDiffs = DifferenceComputer.getXSDItemsDiffs(newDescrContainer, oldDescrContainer).getTypeDiffInfos();
        return xsdTypesDiffs;
    }
}
