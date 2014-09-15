package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.XmlSchemaPreprocessRes;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * This test checks XML schemas preprocessing.
 *
 * @author Alexandr Klimov
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class XmlSchemaPreprocessorTest {

    @Test
    public void testPreprocess() throws Exception{
        InputStream genericXSDIs = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.read( new StreamSource(genericXSDIs) );
        XmlSchemaPreprocessRes res = XmlSchemaProcessor.xmlSchemaPreprocess(schemaCol);

        //Filter from standart XSD types
        int testTypeCounter = 0;
        for(XmlSchemaType type : res.getXmlSchemaTypeSet()){
            if(!XmlSchemaItemsProcessor.isBaseType(type)){
                testTypeCounter++;
            }
        }

        assertEquals(4, testTypeCounter);

    }

    @Test
    public void testPreprocessComposite(){
        //Load file with a root element
        InputStream genericXSDIs = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/composite.xsd");

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.read( new StreamSource(genericXSDIs) );
        XmlSchemaPreprocessRes res = XmlSchemaProcessor.xmlSchemaPreprocess(schemaCol);

        //Filter from standart XSD types
        int testTypeCounter = 0;
        for(XmlSchemaType type : res.getXmlSchemaTypeSet()){
            if(!XmlSchemaItemsProcessor.isBaseType(type)){
                testTypeCounter++;
            }
        }

        assertEquals(5, testTypeCounter);
    }

    @Test
    public void testGetXmlSchemaTypeDescriptors() throws FileNotFoundException {
        InputStream is = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        DescriptorsContainer xmlSchemaTypeDescriptors = XmlSchemaProcessor.getXmlSchemaTypeDescriptors(is);

        int testTypeCounter = 0;
        for(TypeDescriptor td : xmlSchemaTypeDescriptors.getTypeDescriptors()){
            if(!td.isBaseXsdType()){
                testTypeCounter++;
            }
        }

        assertEquals(4, testTypeCounter);

    }

    @Test
    public void testGetXmlSchemaTypeDescriptorsComposite(){
        //Load TypeDescriptors from file with a root element
        InputStream compositeIs = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/composite.xsd");
        DescriptorsContainer xmlSchemaTypeDescriptorsComposite = XmlSchemaProcessor.getXmlSchemaTypeDescriptors(compositeIs);

        int testTypeCounterComposite = 0;
        for(TypeDescriptor td : xmlSchemaTypeDescriptorsComposite.getTypeDescriptors()){
            if(!td.isBaseXsdType()){
                testTypeCounterComposite++;
            }
        }

        assertEquals(5, testTypeCounterComposite);
    }

    @Test
    public void testPreprocessNestedIndicators() throws Exception{
        InputStream genericXSDIs = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.read( new StreamSource(genericXSDIs) );
        XmlSchemaPreprocessRes res = XmlSchemaProcessor.xmlSchemaPreprocess(schemaCol);

        //Filter from standart XSD types
        int testTypeCounter = 0;
        for(XmlSchemaType type : res.getXmlSchemaTypeSet()){
            if(!XmlSchemaItemsProcessor.isBaseType(type)){
                testTypeCounter++;
            }
        }

        assertEquals(4, testTypeCounter);

    }

    @Test
    public void testGetXmlSchemaTypeDescriptorsNestedIndicators() throws FileNotFoundException {
        InputStream is = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");
        DescriptorsContainer xmlSchemaTypeDescriptors = XmlSchemaProcessor.getXmlSchemaTypeDescriptors(is);

        int testTypeCounter = 0;
        for(TypeDescriptor td : xmlSchemaTypeDescriptors.getTypeDescriptors()){
            if(!td.isBaseXsdType()){
                testTypeCounter++;
            }
        }

        assertEquals(4, testTypeCounter);

    }
}
