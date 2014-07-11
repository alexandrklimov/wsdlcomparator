package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is intended for tests on XSD with <strong>group</strong> W3C XMLSchema elements
 *
 * @author Alexandr Klimov
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class XSDWithGroupProcessingTest {

    @Test
    public void preprocessTest(){
        InputStream genericXSDIs = this.getClass().getResourceAsStream("/xsd/group/group.xsd");

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.read( new StreamSource(genericXSDIs) );
        DescriptorsContainer xmlSchemaItemsDescriptors = XmlSchemaProcessor.getXmlSchemaItemsDescriptors(schemaCol);
        Assert.assertNotNull(xmlSchemaItemsDescriptors);

        Set<TypeDescriptor> typeDescriptors = filterBaseXsdSchemaTypes(xmlSchemaItemsDescriptors.getTypeDescriptors());
        Set<GroupDescriptor> groupDescriptors = xmlSchemaItemsDescriptors.getGroupDescriptors();
        Assert.assertNotNull(typeDescriptors);
        Assert.assertNotNull(groupDescriptors);
        Assert.assertEquals(2, typeDescriptors.size());
        Assert.assertEquals(2, groupDescriptors.size());

        TypeDescriptor complexT1 = null;
        TypeDescriptor complexT2 = null;

        //Type checks
        for(TypeDescriptor td : typeDescriptors){
            String name = td.getName();
            if( "complexT1".equals(name) ){
                complexT1 = td;
                Assert.assertNotNull( td.getRootGroupRef() );
                Assert.assertNull( td.getRootIndicator() );
                Assert.assertNull( td.getRootIndicatorType() );
                Assert.assertEquals("{http://aklimov.ru/wsdlcomparator}group1", td.getRootGroupRef().getRef().toString());

            } else if( "complexT2".equals(name) ){
                complexT2 = td;
                Assert.assertNull(td.getRootGroupRef());
                Assert.assertNotNull(td.getRootIndicator());
                Assert.assertNotNull(td.getRootIndicatorType());

                List<ParticleContent> items = td.getRootIndicator().getItems();
                GroupReference gr = null;
                int grCount = 0;
                for(ParticleContent pc : items){
                    if(pc instanceof GroupReference){
                        gr = (GroupReference) pc;
                        grCount++;
                    }
                }

                Assert.assertNotNull(gr);
                Assert.assertEquals(1, grCount);

            }

        }

        Assert.assertNotNull(complexT1);
        Assert.assertNotNull(complexT2);

        //Groups checks
        for(GroupDescriptor gd : groupDescriptors){
            List<OwnerInfo> refBy = gd.getRefBy();
            Assert.assertNotNull(refBy);
            Assert.assertFalse(refBy.size() == 0);

            String name = gd.getName();
            if( "group1".equals(name) ){
                Assert.assertEquals(1, refBy.size());
                OwnerInfo ownerInfo = refBy.get(0);
                Assert.assertEquals( complexT1, ownerInfo.getTypeDescriptor() );
                Assert.assertNull(ownerInfo.getElemName());
                Assert.assertNull( ownerInfo.getGroupDescriptor() );
                Assert.assertNull( ownerInfo.getIndicatorDescriptor() );

            } else if( "group2".equals(name) ){
                Assert.assertEquals(1, refBy.size());
                OwnerInfo ownerInfo1 = refBy.get(0);
                Assert.assertEquals(complexT2, ownerInfo1.getTypeDescriptor());
                Assert.assertNull(ownerInfo1.getElemName());
                Assert.assertNull( ownerInfo1.getGroupDescriptor() );
                Assert.assertNotNull(ownerInfo1.getIndicatorDescriptor());

            }

        }



    }

    private Set<TypeDescriptor> filterBaseXsdSchemaTypes(Set<TypeDescriptor> tdSet){
        Set<TypeDescriptor> resSet = new HashSet<>();
        for(TypeDescriptor td : tdSet){
            if( ! td.isBaseXsdType() ){
                resSet.add(td);
            }
        }
        return resSet;
    }

}
