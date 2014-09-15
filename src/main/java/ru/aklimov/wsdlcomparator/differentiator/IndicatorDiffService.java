package ru.aklimov.wsdlcomparator.differentiator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.domain.descriptors.*;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.*;

/**
 * @author Alexandr Klimov
 */
public class IndicatorDiffService {
    static private Logger LOG = LoggerFactory.getLogger(IndicatorDiffService.class);


    /**
     * A complex type may have not got any element at all, then it may have not got root indicator (seq. all, choice) at all.
     *
     * @author Alexandr Klimov
     * @param newIndDescr
     * @param oldIndDescr
     * @param changedElements is enriched during recursive processing
     */
    @SuppressWarnings("unchecked")
    static public void indicatorCompare(IndicatorDescriptor newIndDescr,
                                         IndicatorDescriptor oldIndDescr,
                                         Map<ParticleContent, ChangeInfoDetails> changedElements){

        LOG.debug("Search changed descriptors in an indicator");

        if(newIndDescr==null && newIndDescr==oldIndDescr){
            return;

        }else if(newIndDescr!=null && oldIndDescr==null){
            changedElements.put(newIndDescr, new ChangeInfoDetails(NEW));
            return;

        }else if(newIndDescr==null && oldIndDescr!=null){
            ChangeInfoDetails cid = new ChangeInfoDetails();
            cid.setChangeType(DELETE);
            cid.setDelIndicator(oldIndDescr);
            changedElements.put(oldIndDescr, new ChangeInfoDetails());
            return;

        }

        /////////// If both IndicatorDescriptor instances are not null ///////////////////////

        //////////////// SEARCH CHANGED ELEMENTS ///////////////////////////////////////////////////////
        //Using ListUtils.removeAll because of CollectionUtils.removeAll contains a bug that
        //only fixed in 4.0 branch of commons-collections

        List<ParticleContent> newIndItems = newIndDescr.getItems();
        List<ParticleContent> oldIndItems = oldIndDescr.getItems();
        Collection<ParticleContent> itemsForDeepCompare = CollectionUtils.intersection(newIndItems, oldIndItems);
        boolean isDeepCompareChanges = false;

        //Because of an equals method of ElementDescriptor does not take type of an element for comparison,
        //the intersection contains ElementDescriptor either newTd type or OTHER type, if equals on these type returns TRUE.
        //There is we can obtain an ElementDescriptor from newTd type and an ElementDescriptor from OTHER type
        // which describe the same element, but may have got changed or different types.
        //
        //At this time we are detecting REPLACED TYPE and CHANGE_IN_ATTRIBUTES only. More deep changes in a type will be detected during
        //affected types searching.
        LOG.debug("Search changed content items.");
        for(ParticleContent pc : itemsForDeepCompare){
            if(pc instanceof ElementDescriptor){
                ElementDescriptor newEd = (ElementDescriptor) newIndItems.get(newIndItems.indexOf(pc));
                ElementDescriptor oldEd = (ElementDescriptor) oldIndItems.get(oldIndItems.indexOf(pc));
                boolean tdEq = newEd.getTypeDescr().equals(oldEd.getTypeDescr());
                boolean elemAttrsEq = newEd.attributeCompare(oldEd);

                ChangeInfoDetails chngNfo = null;
                if(!tdEq && elemAttrsEq){
                    chngNfo = new ChangeInfoDetails(REPLACED_TYPE, oldEd.getTypeDescr());

                }else if(tdEq && !elemAttrsEq){
                    chngNfo = new ChangeInfoDetails(CHANGE_IN_ATTRIBUTES, oldEd.getTypeDescr());

                } else if(!tdEq && !elemAttrsEq){
                    chngNfo = new ChangeInfoDetails(CHANGE_IN_ATTRIBUTES_AND_REPLACED_TYPE, oldEd.getTypeDescr());

                }
                if(chngNfo!=null){
                    changedElements.put(newEd, chngNfo);
                    isDeepCompareChanges = true;
                }

            }else if(pc instanceof GroupReference){
                GroupReference newGroupRef = (GroupReference) newIndItems.get(newIndItems.indexOf(pc));
                GroupReference oldGroupRef = (GroupReference) oldIndItems.get(oldIndItems.indexOf(pc));

                ChangeInfoDetails changeInfoDetails = groupCompare(newGroupRef, oldGroupRef);
                if(changeInfoDetails != null){
                    changedElements.put(newGroupRef, changeInfoDetails);
                }

            } else {
                //We will fall into this branch if an item is IndicatorDescriptor.
                //At this step compared IndicatorDescriptor instances may have differences in element type or attributes only
                //so they are equals check passed. see IndicatorDescriptor#equals
                IndicatorDescriptor newInnerIndDescr = (IndicatorDescriptor) newIndItems.get(newIndItems.indexOf(pc));
                IndicatorDescriptor oldInnerIndDescr = (IndicatorDescriptor) oldIndItems.get(oldIndItems.indexOf(pc));
                //Indicator comparing with details has sense if indicators have the same type only.
                Map<ParticleContent, ChangeInfoDetails> tmpChangedElements = new HashMap<>();
                indicatorCompare(newInnerIndDescr, oldInnerIndDescr, tmpChangedElements);
                if( ! tmpChangedElements.isEmpty()){
                    changedElements.putAll(tmpChangedElements);
                    isDeepCompareChanges = true;
                }

            }
        }

        //Search new items
        LOG.debug("Search new items");
        Collection<ParticleContent> createdItems = ListUtils.removeAll(newIndItems, itemsForDeepCompare);
        for(ParticleContent pc : createdItems){
            changedElements.put(pc, new ChangeInfoDetails(NEW) );
        }

        //Search deleted items
        LOG.debug("Search deleted items");
        Collection<ParticleContent> delItems = ListUtils.removeAll(oldIndItems, itemsForDeepCompare);
        for(ParticleContent pc : delItems){
            ChangeInfoDetails changeInfoDetails = new ChangeInfoDetails(DELETE);
            if( pc instanceof ElementDescriptor){
                changeInfoDetails.setDelElem((ElementDescriptor) pc);

            } else if(pc instanceof IndicatorDescriptor){
                changeInfoDetails.setDelIndicator((IndicatorDescriptor) pc);

            } else if( pc instanceof GroupReference){
                changeInfoDetails.setOldGrpRef((GroupReference) pc);

            }

            changedElements.put( pc, changeInfoDetails);
        }

        //////////////////////////////
        //  Detect type of a change for an indicator
        //////////////////////////////
        List<AttributeDescriptor> changedAttributes = newIndDescr.attributesEqual(oldIndDescr);
        boolean isChangesInAttrs = ( !changedAttributes.isEmpty()  );
        boolean isChangesInContent = isDeepCompareChanges || !createdItems.isEmpty() || !delItems.isEmpty();

        if(isChangesInContent){
            ChangeInfoDetails chNfo;
            if(isChangesInAttrs){
                chNfo = new ChangeInfoDetails(CHANGE_IN_CONTENT_AND_ATTR);
                chNfo.setChangedAttributes(changedAttributes);
            } else {
                chNfo = new ChangeInfoDetails(CHANGE_IN_CONTENT);
            }
            changedElements.put(newIndDescr, chNfo);

        } else if(!isChangesInContent && isChangesInAttrs) {
            ChangeInfoDetails chNfo = new ChangeInfoDetails(CHANGE_IN_ATTRIBUTES);
            chNfo.setChangedAttributes(changedAttributes);
            changedElements.put(newIndDescr, chNfo);

        }

    }


    static public ChangeInfoDetails groupCompare(GroupReference newGrpRef, GroupReference oldGrpRef){
        long newMinOccurs = newGrpRef.getMinOccurs();
        long newMaxOccurs = newGrpRef.getMaxOccurs();
        long oldMinOccurs = oldGrpRef.getMinOccurs();
        long oldMaxOccurs = oldGrpRef.getMaxOccurs();

        boolean isChanges = false;
        Map<String, String> resMap = new HashMap<>();
        if(newMinOccurs != oldMinOccurs){
            resMap.put(AttributeDescriptor.MIN_OCCURS, String.valueOf(oldMinOccurs));
            isChanges = true;
        }
        if(newMaxOccurs != oldMaxOccurs){
            resMap.put(AttributeDescriptor.MAX_OCCURS, String.valueOf(oldMaxOccurs));
            isChanges = true;
        }

        boolean isRefreplaced = false;
        if( ! newGrpRef.getRef().equals(oldGrpRef.getRef()) ){
            isRefreplaced = true;
        }

        if( ! isChanges && !isRefreplaced){
            return null;

        } else {
            ChangeInfoDetails changeInfoDetails = new ChangeInfoDetails();
            if(isChanges && !isRefreplaced){
                changeInfoDetails.setChangeType(CHANGE_IN_ATTRIBUTES);
                changeInfoDetails.getChangedBuiltInAttributes().putAll(resMap);

            }else if(isRefreplaced && !isChanges){
                changeInfoDetails.setChangeType(REPLACED_GRP_REF);
                changeInfoDetails.setOldGrpRef(oldGrpRef);

            } else{
                changeInfoDetails.setChangeType(REPLACE_GRP_REF_AND_ATTR);
                changeInfoDetails.getChangedBuiltInAttributes().putAll(resMap);
                changeInfoDetails.setOldGrpRef(oldGrpRef);
            }
            return changeInfoDetails;
        }

    }

}
