package ru.aklimov.wsdlcomparator.differentiator;

import org.apache.commons.collections.ListUtils;
import ru.aklimov.wsdlcomparator.DifferenceComputer;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WsdlMessagePartDiffInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexandr Klimov
 */
public class MessageDiffService {

    /**
     * This method compares two lists of WSDL message parts that present wsdl:messages.<br>
     *
     * @param oldMessage
     * @param newMessage
     * @return
     */
    public static Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> compareMessages(
            List<WSMethodDescr.MessagePartDescr> oldMessage,
            List<WSMethodDescr.MessagePartDescr> newMessage,
            Set<TypeDiffInfo> typeDiffSet){

        Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> resMap = new HashMap<>();

        //Search and process added parts
        List<WSMethodDescr.MessagePartDescr> addedParts = ListUtils.removeAll(newMessage, oldMessage);
        for(WSMethodDescr.MessagePartDescr partDescr : addedParts){
            WsdlMessagePartDiffInfo wmpdf = new WsdlMessagePartDiffInfo();
            wmpdf.setNew(true);
            resMap.put(partDescr, wmpdf);
        }

        //Search ad processed removed parts
        List<WSMethodDescr.MessagePartDescr> removedParts = ListUtils.removeAll(oldMessage, newMessage);
        for(WSMethodDescr.MessagePartDescr partDescr : removedParts){
            TypeDiffInfo typeDiffInfo = DifferenceComputer.searchDiffNfoByType(partDescr.getTypeDescr(), typeDiffSet);
            WsdlMessagePartDiffInfo wmpdf = new WsdlMessagePartDiffInfo();
            wmpdf.setDeleted(true);
            wmpdf.setMsgPart(partDescr);
            resMap.put(partDescr, wmpdf);
        }

        //Analyse rest of parts
        List<WSMethodDescr.MessagePartDescr> forDeepAnalyse = ListUtils.removeAll(oldMessage, newMessage);
        for(WSMethodDescr.MessagePartDescr partDescr : forDeepAnalyse){
            WSMethodDescr.MessagePartDescr newVersionOfPart = newMessage.get(newMessage.indexOf(partDescr));
            WSMethodDescr.MessagePartDescr oldVersionOfPart = oldMessage.get(oldMessage.indexOf(partDescr));

            boolean isXsdSchemaBindingChanged = newVersionOfPart.isByElementBinding() != oldVersionOfPart.isByElementBinding();
            boolean isTargetXsdTypeChanged = ! newVersionOfPart.getTypeDescr().equals(oldVersionOfPart.getTypeDescr());
            TypeDiffInfo typeDiff = DifferenceComputer.searchDiffNfoByType(newVersionOfPart.getTypeDescr(), typeDiffSet);

            if(isXsdSchemaBindingChanged || isTargetXsdTypeChanged){
                WsdlMessagePartDiffInfo wmpdf = new WsdlMessagePartDiffInfo();
                wmpdf.setMsgPart(newVersionOfPart);

                if(typeDiff != null){
                    wmpdf.setAffectedByType(true);
                }

                if(isXsdSchemaBindingChanged) {
                    wmpdf.setXsdSchemaBindingChanged(true);
                }
                if(isTargetXsdTypeChanged){
                    wmpdf.setOldTypeDescr(oldVersionOfPart.getTypeDescr());
                }

                resMap.put(partDescr, wmpdf);
            }

        }

        newMessage.addAll(removedParts);
        return resMap;
    }

    public static boolean isPartsReordered(List<WSMethodDescr.MessagePartDescr> oldMessage,
                                           List<WSMethodDescr.MessagePartDescr> newMessage){
        List<WSMethodDescr.MessagePartDescr> forDeepAnalyse = ListUtils.removeAll(oldMessage, newMessage);
        for(WSMethodDescr.MessagePartDescr partDescr : forDeepAnalyse){
            int newIndex = newMessage.indexOf(partDescr);
            int oldIndex = oldMessage.indexOf(partDescr);

            if(newIndex != oldIndex){
                return true;
            }
        }
        return false;
    }
}
