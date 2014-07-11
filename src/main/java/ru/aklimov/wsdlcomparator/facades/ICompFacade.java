package ru.aklimov.wsdlcomparator.facades;

import ru.aklimov.wsdlcomparator.WSDLProcessor;
import ru.aklimov.wsdlcomparator.domain.CompareResult;
import ru.aklimov.wsdlcomparator.domain.DiffContainer;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 03.05.13
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public interface ICompFacade {

    ///////////////////// PROCESS SECTION //////////////////////////////////////////////

    WSDLProcessor.WSDLProcessingResult processWSDL(InputSource inputSource, String documentBaseURI) throws WSDLException;

    WSDLProcessor.WSDLProcessingResult processWSDL(String wsdlURI) throws WSDLException;

    WSDLProcessor.WSDLProcessingResult processWSDLWithCntx(String wsdlURI, String contextURI) throws WSDLException;

    //////////////////// COMPARE SECTION ////////////////////////////////////////////////////////////////////////

    DiffContainer compare(Definition newWSDL, Definition oldWSDL);

    Set<TypeDiffInfo> compare(String newWSDLURI, String oldWSDLURI) throws WSDLException;

    Set<TypeDiffInfo> compare(InputSource newInputSource, String newDocumentBaseURI,
                          InputSource oldInputSource, String oldDocumentBaseURI) throws WSDLException;

    Set<TypeDiffInfo> compare(String newWsdlURI, String newContextURI,
                          String oldWsdlURI, String oldContextURI) throws WSDLException;


    CompareResult fullCompare(String newWsdlURI, String newContextURI,
                              String oldWsdlURI, String oldContextURI) throws WSDLException;

    CompareResult fullCompare(InputSource newInputSource, String newDocumentBaseURI,
                              InputSource oldInputSource, String oldDocumentBaseURI) throws WSDLException;

    CompareResult fullCompare(String newWSDLURI, String oldWSDLURI) throws WSDLException;

    CompareResult fullCompare(Definition newWSDL, Definition oldWSDL);
}
