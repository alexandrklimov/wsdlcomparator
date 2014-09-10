package ru.aklimov.wsdlcomparator.modelbuilders;

import org.apache.commons.collections.ListUtils;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WsdlMessagePartDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.ModelBuildCntx;
import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.MessagePartDescrTable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Alexandr Klimov
 */
public class MessagePartDescrTableBuilder {

    /**
     * This method uses {@link MessagePartDescrTableBuilder#createBaseFilledTable} heavily
     *
     * @param messagePartDescr
     * @param cntx
     * @return
     */
    public static final MessagePartDescrTable createTableByDescr(WSMethodDescr.MessagePartDescr messagePartDescr, ModelBuildCntx cntx){
        return createBaseFilledTable(messagePartDescr, cntx);
    }

    /**
     * This method uses {@link MessagePartDescrTableBuilder#createBaseFilledTable} heavily
     *
     * @param wmpdf
     * @param cntx
     * @return
     */
    public static final MessagePartDescrTable createTableByDiffInfo(WsdlMessagePartDiffInfo wmpdf, ModelBuildCntx cntx) {
        WSMethodDescr.MessagePartDescr msgPart = wmpdf.getMsgPart();
        MessagePartDescrTable table = createBaseFilledTable(msgPart, cntx);

        table.setNew( wmpdf.isNew() );
        table.setAffectedByType( wmpdf.isAffectedByType() );
        table.setDeleted( wmpdf.isDeleted() );
        table.setXsdSchemaBindingChanged( wmpdf.isXsdSchemaBindingChanged() );
        table.setAffectedByType( wmpdf.isAffectedByType() );

        if( wmpdf.isTypeChanged() ){
            table.setTypeChanged(true);
            ModelBuildCntx newTdCntx = cntx.switchContextForNewTd(wmpdf.getOldTypeDescr(), cntx.getDeepCount());
            TypeDescrTable modelNewTd = getModelByNewTd(newTdCntx);
            table.setOldTypeDescr(modelNewTd);
        }

        TypeDescrTable typeDescrTable = ViewModelCreator.findTypeDescrTableByTd(wmpdf.getMsgPart().getTypeDescr(), cntx.getTableTypeSet());
        if(typeDescrTable != null){
            table.setTypeDescr(typeDescrTable);

        } else {
            ModelBuildCntx newCntx = cntx.switchContextForNewTd(wmpdf.getMsgPart().getTypeDescr(), cntx.getDeepCount());
            TypeDescrTable modelNewTd = getModelByNewTd(newCntx);
            table.setTypeDescr(modelNewTd);
        }

        return table;

    }


    private static MessagePartDescrTable createBaseFilledTable(WSMethodDescr.MessagePartDescr messagePartDescr, ModelBuildCntx cntx){
        MessagePartDescrTable table = new MessagePartDescrTable();
        table.setName( messagePartDescr.getName() );

        if( messagePartDescr.isByElementBinding() ){
            table.setByElementBinding(true);
            table.setElemName( messagePartDescr.getElemQName().getLocalPart() );
            table.setElemNamespace( messagePartDescr.getElemQName().getNamespaceURI() );
        }

        TypeDescrTable typeDescrTable = ViewModelCreator.findTypeDescrTableByTd(messagePartDescr.getTypeDescr(), cntx.getTableTypeSet());
        if(typeDescrTable != null){
            table.setTypeDescr(typeDescrTable);

        } else {
            ModelBuildCntx newCntx = cntx.switchContextForNewTd(messagePartDescr.getTypeDescr(), cntx.getDeepCount());
            TypeDescrTable modelByNewTd = getModelByNewTd(newCntx);
            table.setTypeDescr(modelByNewTd);
        }

        return table;
    }


    private static TypeDescrTable getModelByNewTd(ModelBuildCntx cntx){
        Set<TypeDescrTable> processedTableSnapshot = new HashSet<>();
        //Remember processed types at the moment for detecting new types in the future
        processedTableSnapshot.addAll(cntx.getTableTypeSet());
        //processing
        ViewModelCreator.createModelByTd(cntx);
        //detect new types
        Set<TypeDescrTable> newTbls = new HashSet<>();
        newTbls.addAll( ListUtils.removeAll(new LinkedList(cntx.getTableTypeSet()), new LinkedList(processedTableSnapshot)) );
        //Search a type in a set of new types
        return ViewModelCreator.findTypeDescrTableByTd(cntx.getTd(), newTbls);
    }
}
