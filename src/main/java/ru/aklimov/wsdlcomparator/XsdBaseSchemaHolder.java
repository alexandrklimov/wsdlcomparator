package ru.aklimov.wsdlcomparator;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;

/**
 * This class holds the base xsd schema
 *
 * @author Alexandr Klimov
 */
public class XsdBaseSchemaHolder {
    static public XmlSchema schemaHolder;

    //todo: make doublecheck!
    synchronized static public void setSchema(XmlSchema schema){
        if( !Constants.URI_2001_SCHEMA_XSD.equals( schema.getTargetNamespace() ) ){
            throw new IllegalArgumentException("It is not base XSD schema.");
        }
        if( schemaHolder==null ){
            schemaHolder = schema;
        }
    }

    static public XmlSchema getSchema(){
        return schemaHolder;
    }

    static public XmlSchemaType getAnyType(){
        if( schemaHolder!=null){
            return schemaHolder.getTypeByName(Constants.XSD_ANYTYPE);
        } else {
            return null;
        }
    }
}
