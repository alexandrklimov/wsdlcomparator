package ru.aklimov.wsdlcomparator;


import junit.framework.Assert;
import org.junit.Test;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.DiffContainer;
import ru.aklimov.wsdlcomparator.domain.descriptors.ElementDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.IndicatorDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Map;

public class XSDGroupCompareTest {

    @Test
    public void testBaseVsChangeOneDiff(){
        InputStream xsdBaseIs = this.getClass().getResourceAsStream("/xsd/group/group_change_base.xsd");
        InputStream xsdOneIs = this.getClass().getResourceAsStream("/xsd/group/group_change_1.xsd");

        DescriptorsContainer xsdBaseTypeDescriptors = XmlSchemaProcessor.getXmlSchemaTypeDescriptors(xsdBaseIs);
        DescriptorsContainer xsdOneTypeDescriptors = XmlSchemaProcessor.getXmlSchemaTypeDescriptors(xsdOneIs);

        Assert.assertNotNull(xsdBaseTypeDescriptors);
        Assert.assertNotNull(xsdOneTypeDescriptors);
        Assert.assertFalse(xsdBaseTypeDescriptors.getGroupDescriptors().isEmpty() ||
                            xsdBaseTypeDescriptors.getTypeDescriptors().isEmpty());
        Assert.assertFalse(xsdOneTypeDescriptors.getGroupDescriptors().isEmpty() ||
                xsdOneTypeDescriptors.getTypeDescriptors().isEmpty());

        DiffContainer xsdItemsDiffs = DifferenceComputer.getXSDItemsDiffs(xsdOneTypeDescriptors, xsdBaseTypeDescriptors);

        //Check result in deep

        final QName GROUP1_QNAME = QName.valueOf("{http://aklimov.ru/wsdlcomparator}group1");
        final QName BASE_TYPE_QNAME = QName.valueOf("{http://aklimov.ru/wsdlcomparator}baseType");
        final QName DERIVED_TYPE_QNAME = QName.valueOf("{http://aklimov.ru/wsdlcomparator}derivedType");

        boolean group1Found = false;
        boolean baseTypeFound = false;
        boolean derivedTypeFound = false;

        for(GroupDiffInfo gdi : xsdItemsDiffs.getGroupDiffInfos()){
            if( gdi.getGrpDescr().getQName().equals(GROUP1_QNAME) ){
                group1Found = true;
                Map<ParticleContent,ChangeInfoDetails> affectedItems = gdi.getAffectedItems();
                for(ParticleContent pc : affectedItems.keySet()){
                    ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE changeType = affectedItems.get(pc).getChangeType();

                    if(pc instanceof IndicatorDescriptor){
                        Assert.assertTrue( ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.CHANGE_IN_CONTENT == changeType );
                    } else if(pc instanceof ElementDescriptor){
                        Assert.assertTrue(ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.DELETE == changeType);
                        Assert.assertEquals("groupElem2", ((ElementDescriptor) pc).getName());
                    }

                }
            }
        }

        for(TypeDiffInfo tdi : xsdItemsDiffs.getTypeDiffInfos()){
            if( BASE_TYPE_QNAME.equals(tdi.getTypeDescr().getQName()) ){
                baseTypeFound = true;
                Assert.assertEquals(1, tdi.getAffectedItems().size());

                ChangeInfoDetails cid = null;
                for(ParticleContent pc : tdi.getAffectedItems().keySet()){
                    cid = tdi.getAffectedItems().get(pc);
                    break;
                }
                ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE changeType = cid.getChangeType();
                Assert.assertTrue( changeType == ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.CHANGE_BY_REF_GRP );

            }else if( DERIVED_TYPE_QNAME.equals(tdi.getTypeDescr().getQName()) ){
                derivedTypeFound = true;
                Assert.assertEquals(0, tdi.getAffectedItems().size());
                Assert.assertTrue(tdi.isAffectedByBaseChanges());
            }
        }

        Assert.assertTrue(group1Found);
        Assert.assertTrue(baseTypeFound);
        Assert.assertTrue(derivedTypeFound);
    }

}
