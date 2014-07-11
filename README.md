## What is WSDLCOMPARATOR?

WSDLCOMPARATOR is a java library intended for computing difference between two WSDL 1.1 or XSD files.

## Motivation

Comparison of two WSDL or XSD files is a not trivial task.
 
1. We may compare this files by many text editors simply, but found changes may be unuseful: are changes about number of spaces at the end of a line really useful for you? And such comparison method may produce a great number of such dumb changes.

2. Types and elements defined in a XSD file may be reused in some other types, elements and for WSDL message part declaration:
    * a type may be used as a base type for some type and as type of an element of another type at the same time;
    * an element may be used by a reference in some types;
    * both types and elements may be used in a WSDL message part declaration.

If we edit some base type then all based on it types will be changed and types contain an element of the changed type will be changed too. This change may affects some WSDL messages parts and therefore affects a web service method.
Finding out such wide tree of changes is very difficult task especially if a changed XSD file contains a great number of types or/and elements.

**This library is trying to solve this task.**

## Particularities

The current version of this library is based on Apache XmlSchema 2.x  
The current version of this library is based on a well tested code is used in a production invironment, but one contains a lot of not well tested changes.  
The current version of tthe library does not process XSD group element and list-restriction in both simpleType and simpleContent.  
May be I overlook something :)


