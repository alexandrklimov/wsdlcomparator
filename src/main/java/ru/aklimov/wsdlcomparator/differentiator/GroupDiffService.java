package ru.aklimov.wsdlcomparator.differentiator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;
import ru.aklimov.wsdlcomparator.domain.diff.ChangeInfoDetails;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexandr Klimov
 */
public class GroupDiffService {
    static private Logger LOG = LoggerFactory.getLogger(GroupDiffService.class);


    static public GroupDiffInfo groupCompare(GroupDescriptor newGrpDescr,
                                    GroupDescriptor oldGrpDescr,
                                    Map<ParticleContent, ChangeInfoDetails> changedElements){

        if(newGrpDescr==null){
            throw new IllegalArgumentException("New group is NULL");
        }
        if(oldGrpDescr==null){
            throw new IllegalArgumentException("Old group is NULL");
        }

        if( LOG.isDebugEnabled() ){
            LOG.debug("Search changed descriptors in a group: " + newGrpDescr.getId());
        }


        /*for(Map.Entry<String,String> changedAttrEntry : changedBuiltInAttrs.entrySet()){
            ChangeInfoDetails chNfoDetail = new ChangeInfoDetails();
            chNfoDetail.setBuiltInAttrOldValue(changedAttrEntry.getValue());
            groupDiffNfo.getAffectedAttributes().put(changedAttrEntry.getKey(), new ChangeInfoDetails());
        }*/


        /// Indicator comparison ///
        GroupDiffInfo groupDiffNfo = new GroupDiffInfo(newGrpDescr);
        boolean hasChanges = false;

        boolean rootIndicatorTypeEq = ( newGrpDescr.getRootIndicatorType()==oldGrpDescr.getRootIndicatorType() );
        if(rootIndicatorTypeEq){
            Map<ParticleContent, ChangeInfoDetails> changedParticles = new HashMap<>();
            IndicatorDiffService.indicatorCompare(newGrpDescr.getRootIndDescr(), oldGrpDescr.getRootIndDescr(), changedElements);
            groupDiffNfo.setAffectedItems(changedParticles);
            hasChanges = true;

        } else {
            groupDiffNfo.setChangedRootIndicatorType(true);
            groupDiffNfo.setOldRootIndicator(oldGrpDescr.getRootIndDescr());

            ChangeInfoDetails indicatorChangeNfo;
            if(newGrpDescr.getRootIndDescr()!= null){
                indicatorChangeNfo = new ChangeInfoDetails(ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.NEW);
                groupDiffNfo.getAffectedItems().put(newGrpDescr.getRootIndDescr(), indicatorChangeNfo);
            }
            if(oldGrpDescr != null){
                indicatorChangeNfo = new ChangeInfoDetails(ChangeInfoDetails.ELEM_OR_ATTR_CHANGE_TYPE.DELETE);
                indicatorChangeNfo.setDelIndicator( oldGrpDescr.getRootIndDescr() );
                groupDiffNfo.getAffectedItems().put(oldGrpDescr.getRootIndDescr(), indicatorChangeNfo);
            }

        }

        return groupDiffNfo;
    }

}
