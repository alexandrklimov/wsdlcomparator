package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import ru.aklimov.wsdlcomparator.domain.descriptors.ElementDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.IndicatorDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Alexandr Klimov
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class IndicatorDescriptorTest {

    TypeDescriptor td1 = new TypeDescriptor();
    TypeDescriptor td2 = new TypeDescriptor();

    //flatContent11 and flatContetn12 have descriptors with the same name
    List<ParticleContent> flatContent11 = new LinkedList<>();
    List<ParticleContent> flatContent12 = new LinkedList<>();
    //flatContent13 have descriptors with names that aren't contained in neither flatContent11 or flat
    List<ParticleContent> flatContent13 = new LinkedList<>();

    //content11 and content12 have nested indicators (sequence) that have descriptors with the same name
    List<ParticleContent> content11 = new LinkedList<>();
    List<ParticleContent> content12 = new LinkedList<>();
    //content13 have an indicator (sequence) that has descriptors aren't contained neither content11 or content12
    List<ParticleContent> content13 = new LinkedList<>();
    //content14 is the same as content13 but nested indicator type is All
    List<ParticleContent> content14 = new LinkedList<>();

    @Before
    public void init(){
        td1.setQName( QName.valueOf("typeDescriptor1"));
        td2.setQName( QName.valueOf("typeDescriptor2"));

        ElementDescriptor ed1 = new ElementDescriptor();
        ed1.setQname(QName.valueOf("QName_elemOne"));
        ElementDescriptor ed2 = new ElementDescriptor();
        ed2.setQname( QName.valueOf("QName_elemTwo"));
        ElementDescriptor ed3 = new ElementDescriptor();
        ed3.setQname( QName.valueOf("QName_elemThree"));
        ElementDescriptor ed4 = new ElementDescriptor();
        ed4.setQname( QName.valueOf("QName_elemFour"));
        ElementDescriptor ed5 = new ElementDescriptor();
        ed5.setQname( QName.valueOf("QName_elemFive"));
        ElementDescriptor ed6 = new ElementDescriptor();
        ed6.setQname( QName.valueOf("QName_elemSix"));
        ElementDescriptor ed7 = new ElementDescriptor();
        ed7.setQname( QName.valueOf("QName_elemSeven"));
        ElementDescriptor ed8 = new ElementDescriptor();
        ed8.setQname( QName.valueOf("QName_elemEight"));

        //Flat content initialization
        flatContent11.add(ed1);
        flatContent11.add(ed2);
        flatContent11.add(ed3);

        flatContent12.add(ed4);
        flatContent12.add(ed3);
        flatContent12.add(ed5);

        flatContent13.add(ed6);
        flatContent13.add(ed7);
        flatContent13.add(ed8);

        //Hierarchical content initialization
        content11.add(ed1);
        content11.add(ed2);
        IndicatorDescriptor content11IndDescr = new IndicatorDescriptor(XmlSchemaSequence.class, Arrays.asList( new ParticleContent[]{ed3}) );
        content11.add(content11IndDescr);

        content12.add(ed4);
        IndicatorDescriptor content12IndDescr = new IndicatorDescriptor(XmlSchemaSequence.class, Arrays.asList( new ParticleContent[]{ed3}) );
        content12.add(content12IndDescr);
        content12.add(ed5);

        content13.add(ed6);
        IndicatorDescriptor content13IndDescr = new IndicatorDescriptor(XmlSchemaSequence.class, Arrays.asList( new ParticleContent[]{ed7}) );
        content13.add(content13IndDescr);
        content13.add(ed8);

        content14.add(ed6);
        IndicatorDescriptor content14IndDescr = new IndicatorDescriptor(XmlSchemaAll.class, Arrays.asList( new ParticleContent[]{ed7}) );
        content14.add(content14IndDescr);
        content14.add(ed8);


    }

    /**
     * This function checks two IndicatorDescriptor-s (sequence and sequence) for equality
     * Compared indicators have flat content - without nested indicators, ElementDescriptor-s only.
     */
    @Test
    public void testEqualitySeqSeq(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, flatContent11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, flatContent12);
        assertTrue( indDescr1.equals(indDescr2) );
    }

    /**
     * This function checks two IndicatorDescriptor-s (sequence and sequence) for non equality
     * Compared indicators have flat content - without nested indicators, ElementDescriptor-s only.
     */
    @Test
    public void testNonEqualitySeqSeq(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, flatContent11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, flatContent13);
        assertFalse(indDescr1.equals(indDescr2));
    }

    /**
     * This function checks two IndicatorDescriptor-s (sequence and choice) for non equality
     * Compared indicators have flat content - without nested indicators, ElementDescriptor-s only.
     */
    @Test
    public void testNonEqualitySeqCh(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, flatContent11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaChoice.class, flatContent12);
        assertFalse( indDescr1.equals(indDescr2) );
    }

    /**
     * This function checks two IndicatorDescriptor-s (sequence and sequence) for equality
     * Compared indicators have hierarchical content - with nested indicators.
     */
    @Test
    public void testEqualitySeqSeqAndSeqSeq(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, content12);
        assertTrue( indDescr1.equals(indDescr2) );
    }

    /**
    * This function checks two IndicatorDescriptor-s (sequence and sequence) for non equality
    * Compared indicators have hierarchical content - with nested indicators.
    */
    @Test
    public void testNonEqualitySeqSeqAndSeqSeq(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, content13);
        assertFalse( indDescr1.equals(indDescr2) );
    }

    /*
    * This function checks two IndicatorDescriptor-s (sequence_sequence and sequence_all) for non equality
    * Compared indicators have hierarchical content - with nested indicators.
    */
    @Test
    public void testEqualitySeqSeqAndSeqAll(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, content14);
        assertFalse( indDescr1.equals(indDescr2) );
    }

    @Test
    public void testAttributesEquality(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        indDescr1.setMaxOccurs(5L);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        indDescr2.setMaxOccurs(5L);
        assertTrue( indDescr1.equals(indDescr2) );
    }

    @Test
    public void testAttributesNonEquality(){
        IndicatorDescriptor indDescr1 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        indDescr1.setMinOccurs(1L);
        indDescr1.setMaxOccurs(5L);
        IndicatorDescriptor indDescr2 = new IndicatorDescriptor(XmlSchemaSequence.class, content11);
        indDescr2.setMaxOccurs(5L);
        assertFalse( indDescr1.attributesEqual(indDescr2).isEmpty() );
    }

}
