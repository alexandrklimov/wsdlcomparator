package ru.aklimov.wsdlcomparator.differentiator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.DifferenceComputer;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.IDiffInfoWithAffected;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;

import java.util.*;

import static ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.*;

/**
 * @author Alexandr Klimov
 */
public class AffectResearcher {
    static private Logger log = LoggerFactory.getLogger(AffectResearcher.class);

    /**
     * This function finds all types and groups are affected by changed type.<br/>
     * One returns a set of new diffs are intended for using over processing in the next iteration.
     *
     * @param df - changed type probably affects some other types
     * @param itemsDiffSet - current set changed types
     * @return Set&lt;TypeDiffInfo&gt;, new diffs or updated diffs
     */
    static public Set<IDiffInfoWithAffected> findAffectedItemsByType(TypeDiffInfo df, Set<IDiffInfoWithAffected> itemsDiffSet){
        log.debug("findAffectedItemsByType: df=" + df);
        Set<IDiffInfoWithAffected> newItemsDiffs = new HashSet<>();
        TypeDescriptor changedTd = df.getTypeDescr();
        List<OwnerInfo> owners = changedTd.getOwnerInfoLst();

        for(OwnerInfo pair : owners){
            //Process only if processed type is not a type of root element or not a root type
            if(pair.getTypeDescriptor() != null){ // <- owner is a type
                TypeDescriptor ownerTd = pair.getTypeDescriptor();
                //Find a TypeDiffInfo for this owner type
                TypeDiffInfo ownDf = DifferenceComputer.searchDiffNfoByType(ownerTd, itemsDiffSet);
                /// Searching items of the changed type in owner type ///
                Map<ParticleContent, ChangeInfoDetails> itemsChangeInfo = processAffectedOwnersItemsByType(ownerTd.getRootIndicator(), changedTd, ownDf);

                /// Searching attributes of the changed type in owner type ///
                //todo: Attributes may be affected with some attribute of an attribute changes
                Map<String, ChangeInfoDetails> affectedOwnerTypeAttrs = new HashMap<>();
                for(AttributeDescriptor ownerAd : ownerTd.getAttributes()){
                    if( ownDf==null || !ownDf.getAffectedAttributes().keySet().contains(ownerAd.getName()) ){
                        if(ownerAd.getTypeDescr().equals(changedTd)){
                            affectedOwnerTypeAttrs.put(ownerAd.getName(), new ChangeInfoDetails(ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.CHANGE_IN_TYPE) );
                        }
                    }
                }

                //If the changed type is base for the owner
                //or if some descriptors or attributes of a changed type has been found in owner type
                if( pair.isChild() ||  !itemsChangeInfo.isEmpty() || !affectedOwnerTypeAttrs.isEmpty() ){
                    if(ownDf==null){
                        ownDf = new TypeDiffInfo(ownerTd);
                        //Add for future processing
                        newItemsDiffs.add(ownDf);
                    }

                    if(pair.isChild()){
                        ownDf.setAffectedByBaseChanges(true);
                    }

                    //Add affected element into a set.
                    ownDf.getAffectedItems().putAll(itemsChangeInfo);
                    //Add affected attributes into a set.
                    ownDf.getAffectedAttributes().putAll(affectedOwnerTypeAttrs);
                }

            } else if(pair.getGroupDescriptor() != null) { // <- owner is a group
                GroupDescriptor ownGd = pair.getGroupDescriptor();
                //Find a GroupDiffInfo for this owner group
                GroupDiffInfo ownDf = DifferenceComputer.searchDiffNfoByGroup(ownGd, itemsDiffSet);
                /// Searching items of the changed type in the owner group ///
                //start with group root indicator
                Map<ParticleContent, ChangeInfoDetails> itemsChangeInfo = new HashMap<>();
                if(ownGd.getRootIndDescr() != null){
                    itemsChangeInfo = processAffectedOwnersItemsByType(ownGd.getRootIndDescr(), changedTd, ownDf);
                }

                if( !itemsChangeInfo.isEmpty() ){
                    if(ownDf == null){
                        ownDf = new GroupDiffInfo(ownGd);
                        //Add for future processing
                        newItemsDiffs.add(ownDf);
                    }

                    ownDf.getAffectedItems().putAll(itemsChangeInfo);
                }
            }

        }

        return newItemsDiffs;
    }


    /**
     * This function finds all types and groups are affected by changed group.<br/>
     * One returns a set of new diffs are intended for using over processing in the next iteration.<br/>
     * This method is looks very like the
     * {@link ru.aklimov.wsdlcomparator.differentiator.AffectResearcher#findAffectedItemsByType(ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo, java.util.Set)}
     * one in a case of an affected group processing.
     *
     *
     * @param gdf
     * @param itemsDiffSet
     * @return Set&lt;GroupDiffInfo&gt; - newly created diff information
     */
    static public Set<IDiffInfoWithAffected> findAffectedItemsByGroup(GroupDiffInfo gdf, Set<IDiffInfoWithAffected> itemsDiffSet){
        log.debug("findAffectedItemsByGroup: gdf=" + gdf);
        Set<IDiffInfoWithAffected> newItemsDiffs = new HashSet<>();
        GroupDescriptor changedGd = gdf.getGrpDescr();
        List<OwnerInfo> owners = changedGd.getRefBy();

        for(OwnerInfo pair : owners){
            if( pair.getTypeDescriptor() != null){
                TypeDescriptor ownerTd = pair.getTypeDescriptor();
                //Find a TypeDiffInfo for this owner type
                TypeDiffInfo ownDf = DifferenceComputer.searchDiffNfoByType(ownerTd, itemsDiffSet);

                Map<ParticleContent, ChangeInfoDetails> itemsChangeInfo = new HashMap<>();

                if(ownerTd.getRootGroupRef() == null){
                    /// Searching items of the changed type in owner type ///
                    itemsChangeInfo = processAffectedOwnersItemsByGroup(ownerTd.getRootIndicator(), changedGd, ownDf);

                } else {
                    //If root indicator is defined as a reference to a group
                    if( ownerTd.getRootGroupRef().getRef().equals( changedGd.getQName() ) ){
                        if( DifferenceComputer.dfIsNullOrDoesNotContains(ownDf, ownerTd.getRootGroupRef()) ){
                            itemsChangeInfo.put( ownerTd.getRootGroupRef(), new ChangeInfoDetails(CHANGE_BY_REF_GRP) );

                        } else {
                            ownDf.getAffectedItems().get(ownerTd.getRootGroupRef()).setChangeType(CHANGE_BY_REF_GRP_AND_ATTR);

                        }
                    }
                }

                if( !itemsChangeInfo.isEmpty() ){
                    if(ownDf==null){
                        ownDf = new TypeDiffInfo(ownerTd);
                        //Add for future processing
                        newItemsDiffs.add(ownDf);
                    }

                    ownDf.getAffectedItems().putAll(itemsChangeInfo);
                }

            } else if(pair.getGroupDescriptor() != null){
                GroupDescriptor ownGd = pair.getGroupDescriptor();
                //Find a GroupDiffInfo for this owner group
                GroupDiffInfo ownDf = DifferenceComputer.searchDiffNfoByGroup(ownGd, itemsDiffSet);
                /// Searching items of the changed type in the owner group ///
                //start with group root indicator
                Map<ParticleContent, ChangeInfoDetails> itemsChangeInfo = new HashMap<>();
                if(ownGd.getRootIndDescr() != null){
                    itemsChangeInfo = processAffectedOwnersItemsByGroup(ownGd.getRootIndDescr(), changedGd, ownDf);
                }

                if( !itemsChangeInfo.isEmpty() ){
                    if(ownDf == null){
                        ownDf = new GroupDiffInfo(ownGd);
                        //Add for future processing
                        newItemsDiffs.add(ownDf);
                    }

                    ownDf.getAffectedItems().putAll(itemsChangeInfo);
                }
            }

        }

        return newItemsDiffs;
    }


    /**
     * This function searches descriptors/indicators are affected by change of some type.<br/>
     * Root indicator is processed in a special way: a TypeDiffInfo instance is not created for it because al need diif-related
     * information is contained id a TypeDiffInfo instance.<br/>
     * <br/>
     * <strong>Here is a recursion.</strong><br/>
     * <br/>
     * @param item an content item(an descriptors or an indicator) will be processed
     * @param changedTd
     * @param ownDf
     * @return Map&lt;ParticleContent, ChangeInfoDetails&gt; - newly created diff. info
     */
    static private Map<ParticleContent, ChangeInfoDetails> processAffectedOwnersItemsByType(ParticleContent item,
                                                                                            TypeDescriptor changedTd,
                                                                                            IDiffInfoWithAffected ownDf){
        Map<ParticleContent, ChangeInfoDetails> result = new HashMap<>();

        if(item instanceof IndicatorDescriptor){
            IndicatorDescriptor indDescr = (IndicatorDescriptor) item;

            //Here is a check that processed indicator is not newly created or deleted.
            //If it is newly created or deleted then any changes in his descriptors does not matter.
            //TODO: Really?! Are we fallen into this block somewhere?
            if(ownDf!=null){
                if( ownDf.getAffectedItems().containsKey(indDescr) ){
                    ChangeInfoDetails changeInfoDetails = ownDf.getAffectedItems().get(indDescr);
                    ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE changeType = changeInfoDetails.getChangeType();
                    if(NEW.equals(changeType) || DELETE.equals(changeType)){
                        return result;
                    }
                }
            }

            //Process indicator content
            List<ParticleContent> innerItems = indDescr.getItems();
            for(ParticleContent innerItem : innerItems){
                Map<ParticleContent, ChangeInfoDetails> tmpRes = processAffectedOwnersItemsByType(innerItem, changedTd, ownDf);
                result.putAll(tmpRes);

            }

            if( !result.isEmpty() ){
                if(ownDf==null){
                    result.put(indDescr, new ChangeInfoDetails(CHANGE_IN_CONTENT));
                }else{
                    ChangeInfoDetails chid = ownDf.getAffectedItems().get(indDescr);
                    if(chid==null){
                        ownDf.getAffectedItems().put(indDescr, new ChangeInfoDetails(CHANGE_IN_CONTENT));
                    } else {
                        //If some changes have been detected in an indicator early then we should check a type of these changes.
                        //If the indicator has changes in its attributes only then we should replace change type
                        //  from the CHANGE_IN_ATTRIBUTES to the CHANGE_IN_CONTENT_AND_ATTR.
                        //If the indicator has got changes in its content yet (CHANGE_IN_CONTENT) then we shouldn't replace
                        //  change type because changes in the indicator attributes aren't checked by this algorithm.
                        if(CHANGE_IN_ATTRIBUTES == chid.getChangeType() ){
                            chid.setChangeType(CHANGE_IN_CONTENT_AND_ATTR);
                        }
                    }
                }
            }

        } else if(item instanceof ElementDescriptor) {
            ElementDescriptor ed = (ElementDescriptor) item;
            if(ed.getTypeDescr().equals(changedTd)){
                if( ownDf==null || !ownDf.getAffectedItems().containsKey(ed) ){
                    result.put(ed, new ChangeInfoDetails(CHANGE_IN_TYPE));
                } else {
                    ChangeInfoDetails changeInfoDetails = ownDf.getAffectedItems().get(ed);
                    if( CHANGE_IN_ATTRIBUTES.equals(changeInfoDetails.getChangeType()) ){
                        changeInfoDetails.setChangeType(CHANGE_IN_ATTRIBUTES_AND_CHANGE_IN_TYPE);
                    }
                }
            }

        } else if(item instanceof GroupReference){
            //do nothing
        }


        return result;
    }

    /**
     * <strong>Here is a recursion</strong>
     *
     * @param item
     * @param changedGrp
     * @param ownDf
     * @return
     */
    static private Map<ParticleContent, ChangeInfoDetails> processAffectedOwnersItemsByGroup(ParticleContent item,
                                                                                               GroupDescriptor changedGrp,
                                                                                               IDiffInfoWithAffected ownDf){

        HashMap<ParticleContent, ChangeInfoDetails> resultMap = new HashMap<>();

        if(item instanceof ElementDescriptor){
            //do nothing

        } else if(item instanceof GroupReference){
            if( ((GroupReference)item).getRef().equals( changedGrp.getQName() ) ){
                if( DifferenceComputer.dfIsNullOrDoesNotContains(ownDf, item) ){
                    resultMap.put(item, new ChangeInfoDetails(CHANGE_BY_REF_GRP));

                } else {
                    ChangeInfoDetails cid = ownDf.getAffectedItems().get(item);
                    if(CHANGE_IN_ATTRIBUTES == cid.getChangeType()){
                        cid.setChangeType(CHANGE_BY_REF_GRP_AND_ATTR);
                    }
                }
            }

        } else if(item instanceof IndicatorDescriptor) {
            IndicatorDescriptor indDescr = (IndicatorDescriptor) item;
            for(ParticleContent childItem : indDescr.getItems() ){
                    Map<ParticleContent, ChangeInfoDetails> changesInfo = processAffectedOwnersItemsByGroup(childItem, changedGrp, ownDf);
                    if( !changesInfo.isEmpty() ){
                        if( DifferenceComputer.dfIsNullOrDoesNotContains(ownDf, indDescr) ){
                            resultMap.put(item, new ChangeInfoDetails(CHANGE_IN_CONTENT));

                        } else {
                            ChangeInfoDetails cid = ownDf.getAffectedItems().get(item);
                            if(cid.getChangeType() == CHANGE_IN_ATTRIBUTES){
                                cid.setChangeType(CHANGE_IN_CONTENT_AND_ATTR);

                            }
                        }
                    }

            }

        }

        return resultMap;

    }

}
