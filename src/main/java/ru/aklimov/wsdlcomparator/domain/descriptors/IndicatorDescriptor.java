package ru.aklimov.wsdlcomparator.domain.descriptors;

import org.apache.commons.collections.ListUtils;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.constants.Constants;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class presents a content of one from following indicators declaration:<br/>
 * <ul>
 *     <li>sequence</li>
 *     <li>choice</li>
 *     <li>all</li>
 * </ul>
 *
 * Content may consists of both ElementDescriptor's and IndicatorDescriptor's.
 *
 * @author Alexandr Klimov
 */
public class IndicatorDescriptor implements ParticleContent{
    static private int EMPTY_HASH = "nullnull".hashCode();
    static private int UNKNOWN_TYPE_HASH = "null-some-descriptors".hashCode();

    private String id;
    private long minOccurs;
    private long maxOccurs;
    private boolean isMaxOccUnbound;

    private Class<? extends XmlSchemaGroupParticle> type;
    private List<ParticleContent> items = new LinkedList<>();
    /**
     *  This field is valid for root indicator only.<br/>
     *  An indicator can belong to ether ComplexType or Group but not both at the same time
     * */
    private TypeDescriptor ownerTd;
    /**
     *  This field is valid for root indicator only.<br/>
     *  An indicator can belong to ether ComplexType or Group but not both at the same time
     * */
    private GroupDescriptor ownerGroup;
    /** This field is null for root indicator */
    private List<IndicatorDescriptor> parentChain = new LinkedList<>();

    /**
     * This field indicates if this indicator have been resolved by a reference to a group
     */
    //TODO: most likely, I don't want to use this field ever
    private boolean byRef;
    /**
     * If <strong>byRef==TRUE</strong> then this field contains a QName of the group.
     */
    //TODO: most likely, I don't want to use this field ever
    private QName refName;

    ////////////////// CONSTRUCTORS ////////////////////////

    public IndicatorDescriptor() {
    }

    public IndicatorDescriptor(Class<? extends XmlSchemaGroupParticle> type, List<ParticleContent> items) {
        this.type = type;
        this.items = items;
    }


    ////////////////// METHODS ////////////////////////

    /**
     *
     * @param obj
     * @return boolean
     */
    @Override
    public boolean equals(Object obj){
        return twoPhaseEquals(obj, 1);
    }

    /**
     * This function compares indicator in a common sense.<br/>
     * This comparison doesn't produce certain an equality but an equivalence.<br/>
     * It renders possible to understand that compared indicators are placed in the same parent, have the same types and contains
     * would that one the same element.<br/>
     * Indicators are equal if theirs parents are equals and compared indicators contains at least one pair of descriptors
     * with the same name.<br/>
     *<br/>
     * Comparing algorithm contaons of following steps:<br/>
     * <ol>
     *     <li>Compare objects by the "==" operation, if result isn't true - goto the next step.</li>
     *     <li>Check type ht other object by instanceof. If result is true - go to the next stage.</li>
     *     <li>Check compared instances for NULL value.</li>
     *     <li>Check all parents on equals, if result is true - go to the next stage</li>
     *     <li>
     *         Check parent indicator type. If it isn't CHOICE then go to the next stage, otherwise go to a step over the next stage/
     *         <ul>
     *              <li>
     *                  Parent indicator isn't CHOICE.<br/>
     *                  Check content items of both indicator descriptor for coincidence - if even one name pair is matched
     *                  then returns true, otherwise return false
     *              </li>
     *              <li>
     *                  Parent indicator is CHOICE.
     *                  Then obtain parent indicator and perform a special algorithm.  For description see later.
     *              </li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * For indicatora are nested in a <strong>CHOICE</strong> indicator a special algorithm will be performed for comparison.<br/>
     * <strong>CHOICE</strong> group indicator can contain a number of nested indicators which may contain an elements with a name is the same
     * as a name of an element in their neighbors.<br/>
     * F.e.:<br/>
     * <pre>
     * [CHOICE]
     *     [SEQUENCE]
     *         [element name="eL1"/]
     *         [element name="eL2"/]
     *     [/SEQUENCE]
     *     [SEQUENCE]
     *         [element name="eL1"/]
     *         [element name="eL3"/]
     *     [/SEQUENCE]
     *     [SEQUENCE]
     *         [element name="eL1"/]
     *         [element name="eL2"/]
     *         [element name="eL3"/]
     *     [/SEQUENCE]
     * [/CHOICE]
     * </pre>
     *
     * In this case an uncertainty is about what following SEQUENCE (sequence23) will be equals with:
     * <pre>
     * [SEQUENCE]
     *  [element name="eL2"/]
     *  [element name="eL3"/]
     *[/SEQUENCE]
     * </pre>
     *
     * For resolving the uncertainty we should compare the <strong>sequence23</strong> with each sequence in the choice and make decision
     * about more appropriate sequence for us. In our case it will be the last sequence in the choice (eL1, el2, eL3).<br/>
     * More appropriated indicator means:
     * <ol>
     *     <li>the same class</li>
     *     <li>a lager set of equal content particles(elems or nested indicators)</li>
     *     <li>in a case there are a number of indicators that appropriate for p.1 and p.2 then more appropriated is the first one</li>
     * </ol>
     * <br/>
     * <strong>In the case</strong> a nested in a CHOICE group indicator is compared,<br/>
     * <strong>then</strong> we obtain parent indicator of compared one (parent of OTHER)<br/>
     * <strong>and</strong> perform comparing with each content particle(elem or indicator) of the parent<br/>
     * <strong>and</strong> if we detect that there are more appropriated for us indicator then current passed for equals operation<br/>
     * <strong>then</strong> return false<br/>
     * <string>otherwise</string> return true.<br/>
     * <br/>
     * <br/>
     * Because of we need analyze a neighbors particles through our parent then we will fall in recursion
     * (retrieve parent and analyze neighbors over and over) during each analyze.
     * For avoid thus recursion we will use two-phase equals operation: at the first phase we perform ordinary equals code,
     * but if we need check our neighbors then we perform the second phase/
     * Second phase is obtaining a collection of neighbors particles and perform equals operation with each of them following
     * peculiarities:
     * <ol>
     *     <li>use this method on THIS instead of standard equalds method.</li>
     *     <li>set phase argument value to 2</li>
     * </ol>
     * If the <strong>phase</strong> parameter is equal 2, then we will never check neighbors.
     *
     * @param obj
     * @param phase used during nested in CHOICE indicators comparing
     * @return boolean
     */
    private boolean twoPhaseEquals(Object obj, int phase){
        //Step1
        if(this==obj){
            return true;
        }

        boolean result = false;

        //Step2
        if( !(obj instanceof IndicatorDescriptor) ){
            result = false;

        } else {
            //Step3
            IndicatorDescriptor other = (IndicatorDescriptor) obj;
            boolean bothTypeNull = (this.type==null && this.type==other.type);
            boolean typeNotNullEquals = false;
            if( !bothTypeNull && this.type!=null && other.type!=null){
                typeNotNullEquals = this.type.equals(other.type);
                boolean typeEq = bothTypeNull || typeNotNullEquals;
                if(!typeEq){
                    result = false;
                } else {
                    //Check parents
                    //Step 4
                    TypeDescriptor thisOwnerTd = this.ownerTd;
                    TypeDescriptor otherOwnerTd = other.ownerTd;
                    boolean bothOwnerTdNullEq = (thisOwnerTd==null && thisOwnerTd==otherOwnerTd);
                    boolean bothOwnerTdEqNotNull = (thisOwnerTd!=null && otherOwnerTd!=null && thisOwnerTd.equals(otherOwnerTd));
                    boolean ownerTdEq = bothOwnerTdNullEq || bothOwnerTdEqNotNull;

                    GroupDescriptor thisOwnerGd = this.ownerGroup;
                    GroupDescriptor otherOwnerGd = other.ownerGroup;
                    boolean bothOwnerGdNullEq = (thisOwnerGd==null && thisOwnerGd==otherOwnerGd);
                    boolean bothOwnerGdEqNotNull = (thisOwnerGd!=null && otherOwnerGd!=null && thisOwnerGd.equals(otherOwnerGd));
                    boolean ownerGdEq = bothOwnerGdNullEq || bothOwnerGdEqNotNull;
                    
                    if(!ownerTdEq || !ownerGdEq){
                        result = false;

                    } else {
                        //We are keeping about recursion in mind here!
                        if( !parentChainsCompare(this.getParentChain(), other.getParentChain()) ){
                            result = false;

                        } else {
                            boolean bothElemsNull = (this.items==null && this.items==other.items);
                            if( bothElemsNull ){
                                result = true;

                            } else if( (this.items==null && other.items!=null) || (this.items!=null && other.items==null) ){
                                result = false;

                            } else {
                                //If type field are equal
                                //Step5
                                //Sequence order of descriptors is unimportant at this time
                                if(this.type == other.type){
                                    boolean matched = false;

                                    IndicatorDescriptor thisParent = ( this.parentChain.isEmpty() )?null:this.parentChain.get( this.parentChain.size()-1 );

                                    if( this.parentChain.isEmpty() || thisParent.type != XmlSchemaChoice.class || phase ==2){
                                        //Common algorithm for SEQUENCE, ALL and everything during PHASE_2
                                        outer_break:
                                        for(ParticleContent thisPc : this.items){
                                            Class thisPcClass = thisPc.getClass();
                                            for(ParticleContent otherPc : other.getItems()){
                                                boolean theSameCls = thisPcClass.equals( otherPc.getClass() );
                                                if(theSameCls){
                                                    matched = thisPc.equals(otherPc);
                                                    if(matched){
                                                        break outer_break;
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        // Special algorithm for parent CHOICE
                                        IndicatorDescriptor otherParent = other.getParentChain().get( other.getParentChain().size()-1 );
                                        //At this step we cleared up that OTHER is most appropriated for us from all OTHERs parent particles
                                        //And now preform reverse check: would OTHER assume us as most appropriated particle.
                                        //This check is necessary, f.e., if one parent has only one child and other parent has many children.
                                        matched = foundEqualsPcs(other, otherParent.getItems());
                                        matched = matched && other.foundEqualsPcs(this, thisParent.getItems());

                                    }

                                    result = matched;

                                } else {
                                    result = false;
                                }

                            }
                        }
                    }
                }
            }


        }

        return result;
    }

    /**
     * This function is special for CHOICE indicator equals operation.<br/>
     * For algorithm description see {@link IndicatorDescriptor#equals(Object)}.
     *
     * @param otherPcForExclude
     * @param pcItemsOfOtherParent
     * @return boolean
     */
    private boolean foundEqualsPcs(IndicatorDescriptor otherPcForExclude, Collection<ParticleContent> pcItemsOfOtherParent){
        List<IndicatorDescriptor> checkCandidates = new LinkedList<>();

        // Search group particles contain would at least one matched particle.
        //Current OTHER is excluded.
        for(ParticleContent neighbor : pcItemsOfOtherParent){
            //We have got all parent level items, i.e. some elements (ElementDescriptor), references to a group (GroupDescriptor)
            //and various indicators(IndicatorDescriptor) are there.
            //Filter by IndicatorDescriptor class.
            boolean theSameCls = (IndicatorDescriptor.class == neighbor.getClass());
            if(theSameCls && (neighbor != otherPcForExclude) ){
                if( otherPcForExclude.twoPhaseEquals(otherPcForExclude, 2) ){
                    checkCandidates.add((IndicatorDescriptor)neighbor);
                }
            }
        }

        //todo: a position of other_neighbours_candidate should be taken in account for compute matchIndex
        if(checkCandidates.isEmpty()){
            return true;

        } else {
            // Find most appropriated
                //Compute max matched index
            int maxMatchedIndex = 0;
            for(IndicatorDescriptor candidate : checkCandidates){
                List<ParticleContent> candidateContent = candidate.getItems();
                int matchSize = ListUtils.intersection(this.getItems(), candidateContent).size();
                if(matchSize > maxMatchedIndex){
                    maxMatchedIndex = matchSize;
                }
            }
                // Calculate match index for current OTHER
            int matchIndexOther = ListUtils.intersection(this.getItems(), otherPcForExclude.getItems()).size();
                //Check for appropriate
            if(matchIndexOther >= maxMatchedIndex){
                return true;
            } else {
                return false;
            }

        }
    }

    /**
     * A hash code value is depends on type of the indicator only.
     *
     * @return int
     */
    @Override
    public int hashCode(){
        if ( type==null && items!=null){
            return UNKNOWN_TYPE_HASH;

        } else {
            String ownerTdStr = "null";
            String ownerGroupStr = "null";
            if(ownerTd != null){
                ownerTdStr = ownerTd.toString();
            }
            if(ownerGroup!=null){
                ownerGroupStr = ownerGroup.toString();
            }

            return ( ownerTdStr + ownerGroupStr + type.getName() ).hashCode();
        }

    }

    /**
     * This function is used for comparison of two lists of item parents.<br/>
     * This function is necessary for avoiding of recursion during comparison these list by equals method.<br/>
     * We don't want to compare each of item by its equals function.<br/>
     * Each of item is compared for type equality.<br/>
     *
     * @param thisPCh
     * @param otherPCh
     * @return boolean
     */
    static private boolean parentChainsCompare(List<IndicatorDescriptor>thisPCh, List<IndicatorDescriptor> otherPCh){
        if(thisPCh==otherPCh){
            return true;
        }
        if( (thisPCh!=null && otherPCh==null) || (thisPCh==null && otherPCh!=null) ){
            return false;
        }
        if(thisPCh.size() != otherPCh.size()){
            return false;
        }

        boolean res = true;
        int listSize = thisPCh.size();
        for(int c = 0; c<listSize; c++){
            IndicatorDescriptor thisIndDescr = thisPCh.get(c);
            IndicatorDescriptor otherIndDescr = thisPCh.get(c);
            if( !thisIndDescr.getType().equals( otherIndDescr.getType() ) ){
                res = false;
                break;
            }
        }

        return res;
    }

    /**
     * This method returns a list of changed buit-in attributes without values.
     *
     * @param otherDesc
     * @return a list of changed attributes
     * @throws IllegalArgumentException if this indicator type isn't equal to a given one.
     */
    public List<AttributeDescriptor> attributesEqual(IndicatorDescriptor otherDesc){
        List<AttributeDescriptor> res = new LinkedList<>();
        if(this!=otherDesc){
            if(this.type != otherDesc.type){
                throw new IllegalArgumentException("There is attempt to compare attributes of incompatible indicators.");
            }
            long otherMaxOcc = otherDesc.getMaxOccurs();
            long otherMinOcc = otherDesc.getMinOccurs();
            if(otherMaxOcc!=this.maxOccurs){
                AttributeDescriptor attrDesc = new AttributeDescriptor();
                attrDesc.setBuildIn(true);
                attrDesc.setName("maxOccurs");
                attrDesc.setQName( QName.valueOf("{"+ Constants.URI_2001_SCHEMA_XSD+"}maxOccurs") );
                res.add(attrDesc);
            }
            if(otherMinOcc!=this.minOccurs){
                AttributeDescriptor attrDesc = new AttributeDescriptor();
                attrDesc.setBuildIn(true);
                attrDesc.setName("minOccurs");
                attrDesc.setQName( QName.valueOf("{"+ Constants.URI_2001_SCHEMA_XSD+"}minOccurs") );
                res.add(attrDesc);
            }

        }

        return res;
    }

    private String getItemName(ParticleContent item){
        String res = null;
        if( item instanceof ElementDescriptor){
            ElementDescriptor elem = (ElementDescriptor) item;
            res = elem.getQname().toString();

        } else {
            res = String.valueOf(item.hashCode());
        }
        return res;
    }

    public void addParentLink(IndicatorDescriptor indDescr){
        getParentChain().add(indDescr);
    }


    ////////////// GETTERS/SETTERS ////////////////////////

    public Class<? extends XmlSchemaGroupParticle> getType() {
        return type;
    }

    public void setType(Class<? extends XmlSchemaGroupParticle> type) {
        this.type = type;
    }

    public List<ParticleContent> getItems() {
        if(items==null){
            items = new LinkedList<>();
        }
        return items;
    }

    public void setItems(List<ParticleContent> items) {
        this.items = items;
    }

    public TypeDescriptor getOwnerTd() {
        return ownerTd;
    }

    public void setOwnerTd(TypeDescriptor ownerTd) {
        this.ownerTd = ownerTd;
        this.ownerGroup = null;
    }

    public List<IndicatorDescriptor> getParentChain() {
        if(parentChain==null){
            parentChain = new LinkedList<>();
        }
        return parentChain;
    }

    public void setParentChain(List<IndicatorDescriptor> parentChain) {
        this.parentChain = parentChain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(long minOccurs) {
        this.minOccurs = minOccurs;
    }

    public long getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(long maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isMaxOccUnbound() {
        return isMaxOccUnbound;
    }

    public void setMaxOccUnbound(boolean maxOccUnbound) {
        isMaxOccUnbound = maxOccUnbound;
    }

    public GroupDescriptor getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(GroupDescriptor ownerGroup) {
        this.ownerGroup = ownerGroup;
        this.ownerTd = null;
    }

    public boolean isByRef() {
        return byRef;
    }

    public void setByRef(boolean byRef) {
        this.byRef = byRef;
    }

    public QName getRefName() {
        return refName;
    }

    public void setRefName(QName refName) {
        this.refName = refName;
    }
}
