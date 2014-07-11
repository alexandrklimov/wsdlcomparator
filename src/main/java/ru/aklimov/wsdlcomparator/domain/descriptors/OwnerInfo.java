package ru.aklimov.wsdlcomparator.domain.descriptors;

/**
 * This class describes consumers of some type or group.<br/>
 * <br/>
 * There are a few use cases for this class:
 * <ul>
 *     <li>if elemName, typeDescriptor and indicatorDescriptor are set then a type is used for "type" attribute of an element definition of a complex type;</li>
 *     <li>if elemName is set only then it's anonymous type of a root element;</li>
 *     <li>if typeDescriptor is set only then there is one from following cases:
 *          <ul>
 *              <li>the type is used as base for some type <strong>(f.e. in &lt;restriction&gt; or &lt;extension&gt; tag)</strong>;</li>
 *              <li>one is used as a type of an attribute of a complex type;</li>
 *              <li>if this class describes GROUP tag relationships then this one points to a complex type consumes the group.</li>
 *          </ul>
 *     </li>
 *     <li>if typeDescriptor and indicatorDescriptor are set and this class describes GROUP tag then this one points to
 *          an indicator consumes the group; <-- WHAT?!
 *     </li>
 *     <li>if set nothing then either a described type is a root type or a GROUP is not consumed anywhere.</li>
 * </ul>
 * //TODO: make this description well read.
 * @author Alexandr Klimov
 */
public class OwnerInfo {
    /**This field contains just an element name, if an element is not root and QName otherwise  */
    private String elemName;
    private IndicatorDescriptor indicatorDescriptor;

    /**used similary indicatorDescriptor*/
    private GroupDescriptor groupDescriptor;

    private TypeDescriptor typeDescriptor;
    /**This field is valid if typeDescriptor!=null only*/
    private boolean child;




    public String getElemName() {
        return elemName;
    }

    public void setElemName(String elemName) {
        this.elemName = elemName;
    }

    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    public void setTypeDescriptor(TypeDescriptor typeDescriptor) {
        this.typeDescriptor = typeDescriptor;
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public IndicatorDescriptor getIndicatorDescriptor() {
        return indicatorDescriptor;
    }

    public void setIndicatorDescriptor(IndicatorDescriptor indicatorDescriptor) {
        this.indicatorDescriptor = indicatorDescriptor;
    }

    public GroupDescriptor getGroupDescriptor() {
        return groupDescriptor;
    }

    public void setGroupDescriptor(GroupDescriptor groupDescriptor) {
        this.groupDescriptor = groupDescriptor;
    }
}
