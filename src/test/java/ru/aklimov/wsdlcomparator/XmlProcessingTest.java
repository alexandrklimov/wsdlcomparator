package ru.aklimov.wsdlcomparator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;

@RunWith(BlockJUnit4ClassRunner.class)
public class XmlProcessingTest {

    @Test
    public void testXmlProcessing() throws Exception{
        DescriptorsContainer xmlSchemaItemsDescriptors = XmlSchemaProcessor.getXmlSchemaTypeDescriptors( XmlProcessingTest.class.getResourceAsStream("/xsd/xmlprocessing/pc.xsd") );
        System.out.println(xmlSchemaItemsDescriptors);
    }

}
