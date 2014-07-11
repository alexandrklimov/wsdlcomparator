package ru.aklimov.wsdlcomparator;

import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *This test checks difference computing for Xml schemas without nested indicators
 * @author Alexandr Klimov
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class SimpleXmlSchemaDiffTest {
    @Test
    public void testEqualsXsdCompare(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);
        assertTrue( typeDiffInfos.isEmpty() );
    }

    @Test
    public void testDiffXsd2(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/2.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Room, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(3, typeDiffInfos.size());

        boolean room = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Room".equals(df.getTypeDescr().getName()) ){
                room = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(room);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd3(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/3.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(3, typeDiffInfos.size());

        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd4(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/4.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(1, typeDiffInfos.size());

        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd5(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/5.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(3, typeDiffInfos.size());

        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd6(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/6.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(2, typeDiffInfos.size());

        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd7(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/7.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Country_Enum, Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(4, typeDiffInfos.size());

        boolean country_enum = false;
        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Country_Enum".equals(df.getTypeDescr().getName()) ){
                country_enum = true;
            } else if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(country_enum);
        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd8(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/8.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Country_Enum_as_anon, Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(4, typeDiffInfos.size());

        boolean anon_country_enum_type = false;
        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( df.getTypeDescr().isAnonymous() ){
                TypeDescriptor td = df.getTypeDescr();
                List<OwnerInfo> ownerInfoLst = td.getOwnerInfoLst();
                for(OwnerInfo pair : ownerInfoLst){
                    if( "city".equals(pair.getElemName()) ){
                        anon_country_enum_type = true;
                    }
                }
            } else if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(anon_country_enum_type);
        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd9(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/9.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(3, typeDiffInfos.size());

        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            }
        }

        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
    }

    @Test
    public void testDiffXsd10(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/10.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Room, Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(4, typeDiffInfos.size());

        boolean address = false;
        boolean house = false;
        boolean house_builder = false;
        boolean room = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( "Address".equals(df.getTypeDescr().getName()) ){
                address = true;
            } else if( "House".equals(df.getTypeDescr().getName()) ){
                house = true;
            } else if ( "House_Builder".equals(df.getTypeDescr().getName()) ){
                house_builder = true;
            } else if ( "Room".equals(df.getTypeDescr().getName()) ){
                room = true;
            }

        }

        assertTrue(address);
        assertTrue(house);
        assertTrue(house_builder);
        assertTrue(room);
    }

    @Test
    public void testDiffXsdComposite(){
        InputStream newIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/composite.xsd");
        InputStream oldIS = this.getClass().getResourceAsStream("/xsd/schema_without_nested_indicators/generic.xsd");
        Set<TypeDiffInfo> typeDiffInfos = XmlSchemaProcessor.compareXmlSchemaTypes(newIS, oldIS);

        //Diff info set should contains following types: Room, Address, House, House_Builder
        assertTrue( !typeDiffInfos.isEmpty() );
        assertEquals(1, typeDiffInfos.size());

        boolean new_anon_root_type = false;
        for(TypeDiffInfo df : typeDiffInfos){
            if( df.getTypeDescr().isAnonymous() ){
                TypeDescriptor td = df.getTypeDescr();
                String ownerElemName = td.getOwnerInfoLst().get(0).getElemName();
                boolean trueOwner = "{http://aklimov.ru/wsdlcomparator}Some_Element".equals(ownerElemName);
                if(trueOwner){
                    new_anon_root_type = true;
                }
            }

        }
        assertTrue(new_anon_root_type);

    }
}
