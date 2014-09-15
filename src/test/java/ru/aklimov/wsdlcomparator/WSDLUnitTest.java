package ru.aklimov.wsdlcomparator;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.xml.sax.InputSource;
import ru.aklimov.wsdlcomparator.domain.CompareResult;
import ru.aklimov.wsdlcomparator.domain.tblmodel.ModelBuildResult;
import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.WSMethodDescrTable;
import ru.aklimov.wsdlcomparator.facades.impl.CompFacade;
import ru.aklimov.wsdlcomparator.facades.impl.MethodModelCreatorFacade;
import ru.aklimov.wsdlcomparator.facades.impl.TypeModelCreatorFacade;
import ru.aklimov.wsdlcomparator.modelbuilders.ViewModelCreator;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.InputStream;
import java.util.Set;

public class WSDLUnitTest {

    private CompFacade compFacade = new CompFacade();
    private TypeModelCreatorFacade typeModelCreatorFacade = new TypeModelCreatorFacade();
    private MethodModelCreatorFacade methodModelCreatorFacade = new MethodModelCreatorFacade();
    private ViewModelCreator viewModelCreator = new ViewModelCreator();

    @Test
    public void testWSDLCompare() throws WSDLException {
        InputStream oldWSDL = this.getClass().getResourceAsStream("/wsdl/PCUrlWS.wsdl");
        InputStream newWSDL = this.getClass().getResourceAsStream("/wsdl/PCUrlWS_group.wsdl");

        InputSource oldIS = new InputSource(oldWSDL);
        InputSource newIS = new InputSource(newWSDL);

        CompareResult compareResult = compFacade.fullCompare(newIS, null, oldIS, null);
        System.out.println(compareResult);

        ModelBuildResult modelByDiffInfoSet = typeModelCreatorFacade.createModelByDiffInfoSet(compareResult.getTypesDiff(), compareResult.getGroupsDiff());
        Set<WSMethodDescrTable> wsMethods = methodModelCreatorFacade.createWSMethodModelByDiffInfo(compareResult.getWsMethodDiff(), modelByDiffInfoSet.getTableTypeSet(), modelByDiffInfoSet.getTableGroupSet());
        Set<TypeDescrTable> filteredTables = viewModelCreator.filterTableSetFromMessagePartTypes(modelByDiffInfoSet.getTableTypeSet(), wsMethods);

        System.out.println(modelByDiffInfoSet);
        System.out.println(wsMethods);
        System.out.println(filteredTables);
    }

    @Test
    public void testWSDLProcessorMoreThenOnePart() throws WSDLException {
        InputStream wsdlInputStream = this.getClass().getResourceAsStream("/wsdl/DeFactoSF1.wsdl");
        InputSource is = new InputSource(wsdlInputStream);

        WSDLFactory factory = WSDLFactory.newInstance("com.ibm.wsdl.factory.WSDLFactoryImpl");
        WSDLReader reader = factory.newWSDLReader();
        Definition definition = reader.readWSDL(null, is);

        WSDLProcessor.WSDLProcessingResult wsdlProcessingResult = WSDLProcessor.processWSDL(definition);

        String infoForOutStr = ReflectionToStringBuilder.reflectionToString(wsdlProcessingResult, ToStringStyle.MULTI_LINE_STYLE);
        System.out.println("\n\n######################\n\n"+infoForOutStr);
    }

}
