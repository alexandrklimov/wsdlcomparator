package ru.aklimov.wsdlcomparator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.differentiator.AffectResearcher;
import ru.aklimov.wsdlcomparator.differentiator.GroupDiffService;
import ru.aklimov.wsdlcomparator.differentiator.MessageDiffService;
import ru.aklimov.wsdlcomparator.differentiator.TypeDiffService;
import ru.aklimov.wsdlcomparator.domain.CompareResult;
import ru.aklimov.wsdlcomparator.domain.DescriptorsContainer;
import ru.aklimov.wsdlcomparator.domain.DiffContainer;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfoWithAffected;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WsdlMessagePartDiffInfo;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 16.04.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class DifferenceComputer {
    static private Logger log = LoggerFactory.getLogger(DifferenceComputer.class);

    /**
     * @param newWSMethods
     * @param oldWSMethods
     * @param typeDiff
     * @return Set<DiffWSMethodInfo>
     */
    @SuppressWarnings("unchecked")
    static public Set<WSMethodDiffInfo> getWSMethodDiff(Set<WSMethodDescr> newWSMethods, Set<WSMethodDescr> oldWSMethods, Set<TypeDiffInfo> typeDiff){
        Set<WSMethodDiffInfo> resSet = new HashSet<>();
        //There is ListUtils.intersection method is used instead of CollectionUtils.intersection because of
        //CollectionUtils.intersection contains a bug will be fiex in 4.x version of commons-collections library
        List<WSMethodDescr> tmpNewWSMethodsLst = new LinkedList<>();
        List<WSMethodDescr> tmpOldWSMethodsLst = new LinkedList<>();
        tmpNewWSMethodsLst.addAll(newWSMethods);
        tmpOldWSMethodsLst.addAll(oldWSMethods);

        log.debug("\n\tSTART WS METHOD DESCRIPTORS DIFF\n");

        /////////
        // Search added methods
        /////////
        Collection<WSMethodDescr> addedWSMethods = ListUtils.removeAll(tmpNewWSMethodsLst, tmpOldWSMethodsLst);
        log.debug("Start processing added methods.");
        for(WSMethodDescr method : addedWSMethods){
            log.debug("Method descriptors for processing: " + method.getMethodName() );
            WSMethodDiffInfo diffMethod = new WSMethodDiffInfo();
            diffMethod.setWsMethodDescr(method);
            diffMethod.setChangeType(WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.NEW);
            resSet.add(diffMethod);
        }

        /////////
        //Search removed methods
        /////////
        Collection<WSMethodDescr> removedWSMethods = ListUtils.removeAll(tmpOldWSMethodsLst, tmpNewWSMethodsLst);
        log.debug("Start processing removed methods.");
        for(WSMethodDescr method : removedWSMethods){
            log.debug("Method descriptors for processing: " + method.getMethodName() );
            WSMethodDiffInfo diffMethod = new WSMethodDiffInfo();
            diffMethod.setWsMethodDescr(method);
            diffMethod.setChangeType(WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.DELETE);
            resSet.add(diffMethod);
        }

        Collection<WSMethodDescr> intersectMethods = ListUtils.intersection( tmpNewWSMethodsLst, tmpOldWSMethodsLst);

        /////////
        //Find TypeDiffInfo's for Input/Output messages.
        /////////
        log.debug("Start processing survived methods.");
        for(WSMethodDescr method : intersectMethods){
            log.debug("Method descriptors for processing: " + method.getMethodName() );
            WSMethodDescr oldMethodDescr = tmpOldWSMethodsLst.get(tmpOldWSMethodsLst.indexOf(method));
            WSMethodDescr newMethodDescr = tmpNewWSMethodsLst.get( tmpNewWSMethodsLst.indexOf(method) );

            Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> inMsgDiffInfo =
                    MessageDiffService.compareMessages(oldMethodDescr.getInputMessage(), newMethodDescr.getInputMessage(), typeDiff);
            boolean inPartsReordered = MessageDiffService.isPartsReordered(oldMethodDescr.getInputMessage(), newMethodDescr.getInputMessage());

            /////////
            //Response may be deleted or added
            /////////
            boolean isResponseAdded = ! newMethodDescr.getOutputMessage().isEmpty() &&  oldMethodDescr.getOutputMessage().isEmpty();
            boolean isResponseDeleted = newMethodDescr.getOutputMessage().isEmpty() && ! oldMethodDescr.getOutputMessage().isEmpty();
            Map<WSMethodDescr.MessagePartDescr, WsdlMessagePartDiffInfo> outMsgDiffInfo = new HashMap<>();

            boolean outPartsReordered = false;
            if( ! isResponseDeleted && ! isResponseAdded){
                outMsgDiffInfo = MessageDiffService.compareMessages(oldMethodDescr.getInputMessage(), newMethodDescr.getInputMessage(), typeDiff);
                outPartsReordered = MessageDiffService.isPartsReordered(oldMethodDescr.getInputMessage(), newMethodDescr.getInputMessage());
            }

            /////////
            //Construct result DiffInfo
            /////////
            if( ! inMsgDiffInfo.isEmpty() ||inPartsReordered ||
                    isResponseDeleted || isResponseAdded ||
                    ! outMsgDiffInfo.isEmpty() || outPartsReordered){

                WSMethodDiffInfo diffMethod = new WSMethodDiffInfo();
                diffMethod.setWsMethodDescr(method);
                diffMethod.setChangeType(WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.CHANGE_MESSAGE_PART);

                if(! inMsgDiffInfo.isEmpty()){
                    diffMethod.getChangedInMsgParts().putAll(inMsgDiffInfo);
                }

                if(inPartsReordered){
                    diffMethod.setInMsgPartReordered(true);
                }

                if(isResponseAdded || isResponseDeleted){
                    if(isResponseAdded){
                        diffMethod.setChangeType( WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.RESPONSE_ADD );
                    } else {
                        diffMethod.setChangeType( WSMethodDiffInfo.WSMETHOD_CHANGE_TYPE.RESPONSE_DEL );
                        diffMethod.getDeletedOutputMessage().addAll( oldMethodDescr.getOutputMessage() );
                    }

                } else {
                    if(! outMsgDiffInfo.isEmpty()){
                        diffMethod.getChangedOutMsgParts().putAll(outMsgDiffInfo);
                    }
                    if(outPartsReordered){
                        diffMethod.setOutMsgPartReordered(true);
                    }
                }

                resSet.add(diffMethod);
                if( log.isDebugEnabled() ){
                    log.debug("\n\t" + diffMethod.toString());
                }
            }

        }

        log.debug("\n\tEND WS METHODS DIFF\n");

        return resSet;
    }

    /**
     * Method returns a result of a difference operation on both types and WS-methods
     *
     * @param newResult
     * @param oldResult
     * @return CompareResult
     */
    public static CompareResult getFullWsdlDiff(final WSDLProcessor.WSDLProcessingResult newResult, final WSDLProcessor.WSDLProcessingResult oldResult){
        DiffContainer itemsDiffs = getXSDItemsDiffs(newResult.getDescriptorContainer(), oldResult.getDescriptorContainer());
        Set<WSMethodDiffInfo> wsMethodDiff = getWSMethodDiff(newResult.getWsMethodDescr(), oldResult.getWsMethodDescr(), itemsDiffs.getTypeDiffInfos());

        CompareResult cr = new CompareResult(itemsDiffs.getTypeDiffInfos(), itemsDiffs.getGroupDiffInfos(), wsMethodDiff);
        return cr;
    }


    public static DiffContainer getXSDItemsDiffs(final DescriptorsContainer newDc, final DescriptorsContainer oldDc){

        Set<GroupDiffInfo> groupsDiffSet = getXSDGroupsDiffs(newDc.getGroupDescriptors(), oldDc.getGroupDescriptors());
        Set<TypeDiffInfo> typesDiffSet = getXSDTypesDiffs(newDc.getTypeDescriptors(), oldDc.getTypeDescriptors());

        Set<IDiffInfoWithAffected> itemsDiffSet = new HashSet<>();
        itemsDiffSet.addAll(groupsDiffSet);
        itemsDiffSet.addAll(typesDiffSet);

        //RELY ON CONVERGING ALGORITHM

        // 1.)Search affected items by changed types
        // 2.)Search affected items by changed group
        // 3.)Check if we have any newly changed types.
        //     a.) if we do - go to 1.)
        //     b.) break this while
        log.debug("Search items are affected by types and groups. A number of root items: " + itemsDiffSet.size());
        Set<IDiffInfoWithAffected> itemsDiffsUnderProcess = new HashSet<>();
        itemsDiffsUnderProcess.addAll(itemsDiffSet);
        while(true){
            //It's a buffer for all types that have been affected by changed types at the first time
            Set<IDiffInfoWithAffected> diffBuffer = new HashSet<>();

            log.debug("Check items by changed types");
            Set<TypeDiffInfo> typeDiffInfos = filterForTDI(itemsDiffsUnderProcess);
            for(TypeDiffInfo tdi: typeDiffInfos){
                Set<IDiffInfoWithAffected> newOrUpdatedDiffs = AffectResearcher.findAffectedItemsByType(tdi, itemsDiffSet);
                itemsDiffSet.addAll(newOrUpdatedDiffs);
                diffBuffer.addAll(newOrUpdatedDiffs);

            }

            log.debug("Check items by changed groups");
            Set<GroupDiffInfo> groupDiffInfos = filterForGDI(itemsDiffsUnderProcess);
            for(GroupDiffInfo gdi: groupDiffInfos){
                Set<IDiffInfoWithAffected> newOrUpdatedDiffs = AffectResearcher.findAffectedItemsByGroup(gdi, itemsDiffSet);
                itemsDiffSet.addAll(newOrUpdatedDiffs);
                diffBuffer.addAll(newOrUpdatedDiffs);

            }

            if(diffBuffer.isEmpty()){
                //There is not any changes;
                log.debug("Affected types by a type: there is not any changes");
                break;

            } else {
                //Merge found changed items with processed ones
                itemsDiffsUnderProcess = new HashSet<>(diffBuffer);
                log.debug("Found affected items: "+diffBuffer.size()+" -> go to next iteration.");
            }

        }

        Set<TypeDiffInfo> resultTypeDiffInfos = filterForTDI(itemsDiffSet);
        Set<GroupDiffInfo> resultGroupDiffInfos = filterForGDI(itemsDiffSet);
        DiffContainer diffContainer = new DiffContainer(resultGroupDiffInfos, resultTypeDiffInfos);
        return diffContainer;
    }


    /**
     *
     * @param newGdSet
     * @param oldGdSet
     * @return
     */
    private static Set<GroupDiffInfo> getXSDGroupsDiffs(final Set<GroupDescriptor> newGdSet, final Set<GroupDescriptor> oldGdSet){
        log.debug("START GET TYPE DIFF");
        Set<GroupDiffInfo> groupDiffInfoSet = new HashSet<>();

        //Search new groups
        log.debug("Search new groups...");
        Sets.SetView<GroupDescriptor> created = Sets.difference(newGdSet, oldGdSet);
        log.debug("Search removed groups...");
        Sets.SetView<GroupDescriptor> removed = Sets.difference(oldGdSet, newGdSet);
        log.debug("Search retired groups...");
        Sets.SetView<GroupDescriptor> forComparison = Sets.intersection(newGdSet, oldGdSet);

        if( log.isDebugEnabled() ){
            StringBuilder sb = new StringBuilder();
            sb.append("\n\tNew groups: " + created.size());
            sb.append("\n\tdeleted groups: " + removed.size());
            sb.append("\n\tFor comparison: " + forComparison.size());
            log.debug(sb.toString());
        }

        for(GroupDescriptor createdGd : created){
            groupDiffInfoSet.add( new GroupDiffInfo(true, createdGd) );
        }

        for(GroupDescriptor removedGd : removed){
            GroupDiffInfo removedGrpDf = new GroupDiffInfo(removedGd);
            removedGrpDf.setRemoved(true);
            groupDiffInfoSet.add(removedGrpDf);
        }

        List<GroupDescriptor> newGdLst = new LinkedList<>();
        newGdLst.addAll(newGdSet);
        List<GroupDescriptor> oldGdLst = new LinkedList<>();
        oldGdLst.addAll(oldGdSet);
        for(GroupDescriptor gd : forComparison){
            GroupDescriptor newGd = newGdLst.get(newGdLst.indexOf(gd));
            GroupDescriptor oldGd = oldGdLst.get( oldGdLst.indexOf(gd) );
            //Check root group indicator changes

            Map<ParticleContent, ChangeInfoDetails> changedElements = new HashMap<>();
            if(newGd.getRootIndicatorType() == oldGd.getRootIndicatorType() ){
                GroupDiffService.groupCompare(newGd, oldGd, changedElements);
                if( ! changedElements.isEmpty()){
                    GroupDiffInfo gdi = new GroupDiffInfo(newGd, changedElements);
                    groupDiffInfoSet.add(gdi);
                }

            } else {
                GroupDiffInfo gdi = new GroupDiffInfo(newGd);
                gdi.setChangedRootIndicatorType(true);
                gdi.setOldRootIndicator(oldGd.getRootIndDescr());
                groupDiffInfoSet.add(gdi);
            }

        }

        return groupDiffInfoSet;

    }


    /**
     * newTdSet and oldTdSet are sets contain xsd type descriptors of a new wsdl-file and old one.
     *
     * @param newTdSet
     * @param oldTdSet
     * @return Set<TypeDiffInfo>
     */
    @SuppressWarnings("unchecked")
    private static Set<TypeDiffInfo> getXSDTypesDiffs(Set<TypeDescriptor> newTdSet, Set<TypeDescriptor> oldTdSet){
        log.debug("START GET TYPE DIFF");
        Set<TypeDiffInfo> typeDiffInfoSet = new HashSet<>();

        /*//Search new types
        log.debug("Search new types");
        //todo: rewrite this algorithm the same way as search removed group algorithm
        for(TypeDescriptor newTd : newTdSet){
            boolean isNotFound = true;
            for(TypeDescriptor oldTd : oldTdSet){
                if(newTd.equals(oldTd)){
                    isNotFound=false;
                    break;
                }
            }
            if(isNotFound){
                if( log.isDebugEnabled() ){
                    if( newTd.isAnonymous() ){
                        log.debug("Added type: anonymous");
                    } else {
                        log.debug("Added type: "+ newTd.getName());
                    }
                }
                typeDiffInfoSet.add(new TypeDiffInfo(true, newTd));
            }
        }

        //Search removed types
        //todo: rewrite this algorithm the same way as search removed group algorithm
        log.debug("Search removed types");
        List<TypeDescriptor> tmpListNewTd = new LinkedList<>();
        tmpListNewTd.addAll( newTdSet );
        List<TypeDescriptor> tmpListOldTd = new LinkedList<>();
        tmpListOldTd.addAll( oldTdSet );
        List<TypeDescriptor> removedTypes = ListUtils.removeAll(tmpListOldTd, tmpListNewTd);

        for(TypeDescriptor remTd : removedTypes){
            if( log.isDebugEnabled() ){
                if( remTd.isAnonymous() ){
                    log.debug("Removed type: anonymous");
                } else {
                    log.debug("Removed type: "+ remTd.getName());
                }
            }
            TypeDiffInfo df = new TypeDiffInfo(remTd);
            df.setRemoved(true);
            typeDiffInfoSet.add(df);

        }
*/

        //Search new groups
        log.debug("Search new groups...");
        Sets.SetView<TypeDescriptor> created = Sets.difference(newTdSet, oldTdSet);
        log.debug("Search removed groups...");
        Sets.SetView<TypeDescriptor> removed = Sets.difference(oldTdSet, newTdSet);
        log.debug("Search retired groups...");
        Sets.SetView<TypeDescriptor> forComparison = Sets.intersection(newTdSet, oldTdSet);

        if( log.isDebugEnabled() ){
            StringBuilder sb = new StringBuilder();
            sb.append("\n\tNew groups: " + created.size());
            sb.append("\n\tdeleted groups: " + removed.size());
            sb.append("\n\tFor comparison: " + forComparison.size());
            log.debug(sb.toString());
        }

        for(TypeDescriptor createdTd : created){
            typeDiffInfoSet.add( new TypeDiffInfo(true, createdTd) );
        }

        for(TypeDescriptor removedTd : removed){
            TypeDiffInfo removedTypeDf = new TypeDiffInfo(removedTd);
            removedTypeDf.setRemoved(true);
            typeDiffInfoSet.add(removedTypeDf);
        }

        //Remove all found base XSD types. These types my be assumed as new but they are not.
        //They are always in any scheme by default, but they are used not always
        log.debug("Remove all found base XSD types");
        Iterator<TypeDiffInfo> diffInfoSetIter = typeDiffInfoSet.iterator();
        while(diffInfoSetIter.hasNext()){
            TypeDiffInfo df = diffInfoSetIter.next();
            if( isBaseType(df.getTypeDescr()) ){
                diffInfoSetIter.remove();
            }
        }

        //find changed types
        log.debug("Search changed types");
        for(TypeDescriptor newTd : newTdSet){
            for(TypeDescriptor oldTd : oldTdSet){
                TypeDiffInfo df = null;
                if(newTd.equals(oldTd)){
                    //Comparing of type
                    if(newTd.isComplexType() && oldTd.isComplexType()){
                        df = TypeDiffService.compareComplexTypeWithDetails(newTd, oldTd);
                    }else {
                        df = TypeDiffService.compareSimpleTypeWithDetails(newTd, oldTd);
                    }
                }
                if(df!=null){
                    typeDiffInfoSet.add(df);
                }
            }
        }

        /// Find all affected types ///
        log.debug("Search all affected types");
        //It's creating a HashSet instance is updated after each iteration.
        Set<TypeDiffInfo> processingDiffs = new HashSet<>(typeDiffInfoSet);

        /// End of processing ///
        log.debug("\n\tEND OF PROCESSING\n");
        return typeDiffInfoSet;
    }


    /**
     *
     * @param td
     * @param dfSet
     * @return
     */
    static public TypeDiffInfo searchDiffNfoByType(TypeDescriptor td, Set<? extends IDiffInfoWithAffected> dfSet){
        TypeDiffInfo res = null;
        if(td!=null){
            for(IDiffInfoWithAffected someDf : dfSet){
                if(someDf instanceof TypeDiffInfo){
                    TypeDescriptor typeDescr = ((TypeDiffInfo) someDf).getTypeDescr();
                    if( td.equals(typeDescr) ){
                        res = ((TypeDiffInfo) someDf);
                        break;
                    }
                }
            }
        }
        return res;
    }

    static public GroupDiffInfo searchDiffNfoByGroup(GroupDescriptor gd, Set<? extends IDiffInfoWithAffected> dfSet){
        GroupDiffInfo res = null;
        if(gd != null){
            for(IDiffInfoWithAffected someDf : dfSet){
                if(someDf instanceof GroupDiffInfo){
                    GroupDescriptor groupDescr = ((GroupDiffInfo) someDf).getGrpDescr();
                    if( gd.equals(groupDescr) ){
                        res = ((GroupDiffInfo) someDf);
                        break;
                    }
                }
            }
        }
        return res;
    }

    static public boolean isBaseType(TypeDescriptor td){
        if(td.getQName() == null){
            return false;
        }
        return "http://www.w3.org/2001/XMLSchema".equals( td.getQName().getNamespaceURI() );
    }


    static public boolean dfIsNullOrDoesNotContains(IDiffInfoWithAffected dfi, ParticleContent item){
        return (dfi == null) || ( ! dfi.getAffectedItems().containsKey(item));
    }

    static private Set<TypeDiffInfo> filterForTDI(Set<IDiffInfoWithAffected> itemDiffSet){
        Collection<IDiffInfoWithAffected> filtered = Collections2.filter(itemDiffSet, new Predicate<IDiffInfoWithAffected>() {
            @Override
            public boolean apply(IDiffInfoWithAffected iDiffInfoWithAffected) {
                return (iDiffInfoWithAffected instanceof TypeDiffInfo);
            }
        });

        Set resSet = new HashSet<>();
        resSet.addAll(filtered);
        return resSet;
    }

    static private Set<GroupDiffInfo> filterForGDI(Set<IDiffInfoWithAffected> itemDiffSet){
        Collection<IDiffInfoWithAffected> filtered = Collections2.filter(itemDiffSet, new Predicate<IDiffInfoWithAffected>() {
            @Override
            public boolean apply(IDiffInfoWithAffected iDiffInfoWithAffected) {
                return (iDiffInfoWithAffected instanceof GroupDiffInfo);
            }
        });

        Set resSet = new HashSet<>();
        resSet.addAll(filtered);
        return resSet;
    }

}
