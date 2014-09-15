package ru.aklimov.wsdlcomparator.facades.impl;

import org.xml.sax.InputSource;
import ru.aklimov.wsdlcomparator.DifferenceComputer;
import ru.aklimov.wsdlcomparator.WSDLProcessor;
import ru.aklimov.wsdlcomparator.domain.CompareResult;
import ru.aklimov.wsdlcomparator.domain.DiffContainer;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.facades.ICompFacade;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.util.Set;

/**
 * This class is a gateway to comparison functionality.
 * Here are methods for both WSDL and XSd files comparison in the class.
 */
public class CompFacade implements ICompFacade {

    ///////////////////// PROCESS SECTION //////////////////////////////////////////////

    /**
     *
     * @param inputSource - an InputSource pointing to the WSDL document, an XML document obeying the WSDL schema.
     * @param documentBaseURI - the document base URI of the WSDL definition described by the document.
     *     Will be set as the documentBaseURI of the returned Definition. Can be null, in which case it will be ignored.
     * @return WSDLProcessor.WSDLProcessingResult
     * @throws javax.wsdl.WSDLException
     */
    public WSDLProcessor.WSDLProcessingResult processWSDL(InputSource inputSource, String documentBaseURI) throws WSDLException {
        if(inputSource == null){
            throw new IllegalArgumentException("An InputSource must not be NULL!");
        }
        WSDLFactory factory = WSDLFactory.newInstance("com.ibm.wsdl.factory.WSDLFactoryImpl");
        WSDLReader reader = factory.newWSDLReader();
        Definition definition = reader.readWSDL(documentBaseURI, inputSource);
        return WSDLProcessor.processWSDL(definition);
    }

    /**
     *
     * @param wsdlURI - a URI (can be a filename or URL) pointing to a WSDL XML definition.
     * @return WSDLProcessor.WSDLProcessingResult
     * @throws WSDLException
     */
    public WSDLProcessor.WSDLProcessingResult processWSDL(String wsdlURI) throws WSDLException {
        if(wsdlURI==null){
            throw new IllegalArgumentException("WSDL URI must not be NULL!");
        }
        return processWSDLWithCntx(wsdlURI, null);
    }

    /**
     *
     * @param wsdlURI - a URI (can be a filename or URL) pointing to a WSDL XML definition.
     * @param contextURI - the context in which to resolve the wsdlURI, if the wsdlURI is relative. Can be null, in which case it will be ignored.
     * @return WSDLProcessor.WSDLProcessingResult
     */
    public WSDLProcessor.WSDLProcessingResult processWSDLWithCntx(String wsdlURI, String contextURI) throws WSDLException {
        if(wsdlURI==null){
            throw new IllegalArgumentException("A WSDL URI must not be NULL!");
        }

        WSDLFactory factory = WSDLFactory.newInstance("com.ibm.wsdl.factory.WSDLFactoryImpl");
        WSDLReader reader = factory.newWSDLReader();
        Definition definition = reader.readWSDL(contextURI, wsdlURI);
        return WSDLProcessor.processWSDL(definition);
    }

    //////////////////// XSD TYPES COMPARE SECTION ////////////////////////////////////////////////////////////////////////
    @Override
    public DiffContainer compare(Definition newWSDL, Definition oldWSDL){
        if(newWSDL==null || oldWSDL==null){
            throw new IllegalArgumentException("WSDL Definition must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = WSDLProcessor.processWSDL(newWSDL);
        WSDLProcessor.WSDLProcessingResult oldResult = WSDLProcessor.processWSDL(oldWSDL);
        //return DifferenceComputer.getXSDItemsDiffs(newResult.getDescriptorContainer(), oldResult.getDescriptorContainer());
        return null;
    }

    @Override
    public Set<TypeDiffInfo> compare(String newWSDLURI, String oldWSDLURI) throws WSDLException {
        if(newWSDLURI==null || oldWSDLURI==null){
            throw  new IllegalArgumentException("WSDL URI must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDL(newWSDLURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDL(oldWSDLURI);
        //return DifferenceComputer.getXSDItemsDiffs(newResult.getDescriptorContainer(), oldResult.getDescriptorContainer());
        return null;
    }

    @Override
    public Set<TypeDiffInfo> compare(InputSource newInputSource, String newDocumentBaseURI,
                                        InputSource oldInputSource, String oldDocumentBaseURI) throws WSDLException {
        if(newInputSource==null || oldInputSource==null){
            throw new IllegalArgumentException("InputSource must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDL(newInputSource, newDocumentBaseURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDL(oldInputSource, oldDocumentBaseURI);
        //return DifferenceComputer.getXSDTypesDiffs(newResult.getDescriptorContainer(), oldResult.getDescriptorContainer());
        return null;
    }

    @Override
    public Set<TypeDiffInfo> compare(String newWsdlURI, String newContextURI,
                                        String oldWsdlURI, String oldContextURI) throws WSDLException {
        if(newWsdlURI==null || oldWsdlURI==null){
            throw new IllegalArgumentException("WSDL URI must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDLWithCntx(newWsdlURI, newContextURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDLWithCntx(oldWsdlURI, oldContextURI);
        //return DifferenceComputer.getXSDTypesDiffs(newResult.getDescriptorContainer(), oldResult.getDescriptorContainer());
        return null;
    }


    //////////////////// FULL COMPARE SECTION ////////////////////////////////////////////////////////////////////////

    /**
     *  Method returns a result of a difference opeariont on both types and WS-methods
     *
     * @param newWSDL
     * @param oldWSDL
     * @return CompareResult
     */
    @Override
    public CompareResult fullCompare(Definition newWSDL, Definition oldWSDL){
        if(newWSDL==null || oldWSDL==null){
            throw new IllegalArgumentException("WSDL Definition must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = WSDLProcessor.processWSDL(newWSDL);
        WSDLProcessor.WSDLProcessingResult oldResult = WSDLProcessor.processWSDL(oldWSDL);
        return DifferenceComputer.getFullWsdlDiff(newResult, oldResult);
    }

    /**
     *  Method returns a result of a difference opeariont on both types and WS-methods
     *
     * @param newWSDLURI
     * @param oldWSDLURI
     * @return CompareResult
     * @throws WSDLException
     */
    @Override
    public CompareResult fullCompare(String newWSDLURI, String oldWSDLURI) throws WSDLException {
        if(newWSDLURI==null || oldWSDLURI==null){
            throw  new IllegalArgumentException("WSDL URI must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDL(newWSDLURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDL(oldWSDLURI);
        return DifferenceComputer.getFullWsdlDiff(newResult, oldResult);
    }

    /**
     *  Method returns a result of a difference opeariont on both types and WS-methods
     *
     * @param newInputSource
     * @param newDocumentBaseURI
     * @param oldInputSource
     * @param oldDocumentBaseURI
     * @return CompareResult
     * @throws WSDLException
     */
    @Override
    public CompareResult fullCompare(InputSource newInputSource, String newDocumentBaseURI,
                                 InputSource oldInputSource, String oldDocumentBaseURI) throws WSDLException {
        if(newInputSource==null || oldInputSource==null){
            throw new IllegalArgumentException("InputSource must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDL(newInputSource, newDocumentBaseURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDL(oldInputSource, oldDocumentBaseURI);
        return DifferenceComputer.getFullWsdlDiff(newResult, oldResult);
    }

    /**
     *  Method returns a result of a difference operation on both types and WS-methods
     *
     * @param newWsdlURI
     * @param newContextURI
     * @param oldWsdlURI
     * @param oldContextURI
     * @return CompareResult
     * @throws WSDLException
     */
    @Override
    public CompareResult fullCompare(String newWsdlURI, String newContextURI,
                                 String oldWsdlURI, String oldContextURI) throws WSDLException {
        if(newWsdlURI==null || oldWsdlURI==null){
            throw new IllegalArgumentException("WSDL URI must not be NULL!");
        }

        WSDLProcessor.WSDLProcessingResult newResult = processWSDLWithCntx(newWsdlURI, newContextURI);
        WSDLProcessor.WSDLProcessingResult oldResult = processWSDLWithCntx(oldWsdlURI, oldContextURI);
        return DifferenceComputer.getFullWsdlDiff(newResult, oldResult);
    }

}
