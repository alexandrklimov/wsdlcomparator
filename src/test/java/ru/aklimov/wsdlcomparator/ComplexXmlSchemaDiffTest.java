package ru.aklimov.wsdlcomparator;

import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 *This test checks difference computing for Xml schemas with nested indicators
 * @author Alexandr Klimov
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ComplexXmlSchemaDiffTest {

    @Test
    public void testEqualsXsdCompare(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.isEmpty() );
    }

    @Test
    public void testDiffXsd1(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/1.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.size()==3 );
    }

    @Test
    public void testDiffXsd2(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/2.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.size()==4 );
    }

    @Test
    public void testDiffXsd3Xsd2(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/3.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/2.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.size()==4 );
    }

    @Test
    public void testDiffChoiceEq(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/choiceEqTest2.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_nested_indicators/choiceEqTest1.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.size()==1 );
        List<TypeDiffInfo>  diffLst = new LinkedList<>();
        diffLst.addAll(typeDiffInfos);
        TypeDiffInfo df = diffLst.get(0);
        assertTrue( df.getAffectedItems().keySet().size()==8 );
    }
}
