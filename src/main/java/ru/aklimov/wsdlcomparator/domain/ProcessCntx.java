package ru.aklimov.wsdlcomparator.domain;

import com.google.common.collect.ImmutableMap;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaType;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.OwnerInfo;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class is used as information container during a Xml Schema items processing (items descriptors creating).<br/>
 * Main goal this class is pass an argument to processing functions in more compact manner.<br/>
 * <br/>
 * <strong>Context is created at start either a type or a group processing only!</strong><br/>
 * <br/>
 * Over processing some items we may face necessary start processing some either child or relative item
 * <i>(type of an element declaration or group declaration that a referenced)</i>.<br/>
 * In this case <strong>partially new</strong> context is created for both a new group tag and a new type tag declaration.<br/>
 * The <strong>partially</strong> word means a number of common collections remain untouched and are copied silent over
 * construction a new context.<br/>
 * These collections are following ones contain source data for processing:
 * <ul>
 *     <li>all root types</li>
 *     <li>all root groups</li>
 *     <li>qname->group declaration map</li>
 *     <li>qname->type declaration map</li>
 * </ul>
 * Also, collections for collecting processed items over processing are pass to a newly constructed context.<br/>
 * <strong>buildContextForNewType</strong> and <strong>buildContextForNewGroup</strong> functions do switch context in that way.<br/>
 * <br/>
 *
 * @author Alexandr Klimov
  */
public class ProcessCntx {
    static private final String CONSTRUCTOR_ILLEGAL_ARGUMENT_ERR_MSG = "Following constructor arguments must not be NULL: ";

    /**
     * It's a processed type. A xsd schema traversal will be start from that type during processing of its.<br/>
     * MUST NOT BE NULL
     * */
    private XmlSchemaType type;
    /**
     * Type descriptor of this context XML Schema type.<br/>
     * This field is in accordance with the <strong>type</strong> field.
     */
    private TypeDescriptor currentProcessedTypeDesc;

    /**
     * It's analogue for <strong>type</strong> field in bounds <strong>group</strong> tag processing.
     */
    private XmlSchemaGroup group;
    /**
     * It's analogue for <strong>currentProcessedTypeDesc</strong> tag in bounds <strong>group</strong> tag processing.
     */
    private GroupDescriptor currentProcessedGroupDesc;

    /**
     * Source data for processing.<br/>
     * MUST NOT BE NULL. If schema definitions does not contain any top-level(root) element definition then
     * this map must be empty one.
     */
    private ImmutableMap<QName, XmlSchemaElement> xsdElemQNameToElemMap;
    /**
     * Source data for processing.<br/>
     * MUST NOT BE NULL. If schema definitions does not contain any top-level(root) type definition then
     * this map must be empty one.
     */
    private ImmutableMap<QName, XmlSchemaType> xsdQNameToTypeMap;
    /**
     * Source data for processing.<br/>
     * MUST NOT BE NULL. If schema definitions does not contain any group definition then this map must be empty one.
     */
    private ImmutableMap<QName, XmlSchemaGroup> xsdQNameToGroupMap;
    /**
     * May be NULL.<br/>
     * F.e. it's NULL for both top-level not anonymous type definition and top-level group one.
     */
    private OwnerInfo ownerPair;

    /**
     * This map is a container of XmlSchemaType processing results.<br/>
     * It's filled throughout XML schema processing.
     */
    private Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap;
    /**
     * This map is a container of XmlSchemaGroup processing results.<br/>
     * It's filled throughout XML schema processing.
     */
    private Map<XmlSchemaGroup, GroupDescriptor> groupToDescriptorMap;



    /////////// CONSTRUCTORS ////////////////////////

    public ProcessCntx(XmlSchemaType typeForProcessing,
                            XmlSchemaPreprocessRes schemaPreprocessRes,
                                Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap,
                                    Map<XmlSchemaGroup, GroupDescriptor> groupToDescriptorMap){
        this(typeForProcessing,
                null,
                    schemaPreprocessRes.getXsdQNameToElemMap(),
                        schemaPreprocessRes.getTypeQNameToType(),
                            schemaPreprocessRes.getXsdQNameToGroupMap(),
                                schemaPreprocessRes.getTypeToInitOwnerPair().get(typeForProcessing),
                                    typeToDescriptorMap,
                                        groupToDescriptorMap);

    }

    /**
     * This constructor means start processing of an group tag, but a group declaration can't be anonymous and is on top level
     * always, because of any owner information is unnecessary. Hence the ownerPair argument is null.
     *
     * @param groupForProcessing
     * @param schemaPreprocessRes
     * @param typeToDescriptorMap
     * @param groupToDescriptorMap
     */
    public ProcessCntx(XmlSchemaGroup groupForProcessing,
                       XmlSchemaPreprocessRes schemaPreprocessRes,
                       Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap,
                       Map<XmlSchemaGroup, GroupDescriptor> groupToDescriptorMap){
        this(null,
                groupForProcessing,
                    schemaPreprocessRes.getXsdQNameToElemMap(),
                        schemaPreprocessRes.getTypeQNameToType(),
                            schemaPreprocessRes.getXsdQNameToGroupMap(),
                                null,//<--owner pair
                                    typeToDescriptorMap,
                                        groupToDescriptorMap);

    }

    /**
     * Common constructor
     *
     * @param type required
     * @param group
     * @param xsdElemQNameToElemMap required
     * @param xsdQNameToTypeMap required
     * @param xsdQNameToGroupMap required
     * @param ownerPair
     * @param typeToDescriptorMap
     * @param groupToDescriptorMap
     */
    public ProcessCntx(XmlSchemaType type,
                            XmlSchemaGroup group,
                                Map<QName, XmlSchemaElement> xsdElemQNameToElemMap,
                                    Map<QName, XmlSchemaType> xsdQNameToTypeMap,
                                        Map<QName, XmlSchemaGroup> xsdQNameToGroupMap,
                                            OwnerInfo ownerPair,
                                                Map<XmlSchemaType, TypeDescriptor> typeToDescriptorMap,
                                                    Map<XmlSchemaGroup, GroupDescriptor> groupToDescriptorMap) {
        StringBuilder errorParams = new StringBuilder();
        boolean errorParamFlag = false;
        if(type == null && group == null){
            errorParams.append(" processed type and processed group are NULL at the same time.");
            errorParamFlag = true;
        }
        if(xsdElemQNameToElemMap == null){
            errorParams.append(" xsdElemQNameToElemMap |");
            errorParamFlag = true;
        }
        if(xsdQNameToTypeMap == null){
            errorParams.append(" xsdQNameToTypeMap |");
            errorParamFlag = true;
        }
        if(xsdQNameToGroupMap == null){
            errorParams.append(" xsdQNameToGroupMap |");
            errorParamFlag = true;
        }

        if(errorParamFlag){
            throw new IllegalArgumentException(CONSTRUCTOR_ILLEGAL_ARGUMENT_ERR_MSG + errorParams.toString() );
        }

        this.type = type;
        this.group = group;
        this.xsdElemQNameToElemMap = ImmutableMap.copyOf(xsdElemQNameToElemMap);
        this.xsdQNameToTypeMap = ImmutableMap.copyOf(xsdQNameToTypeMap);
        this.xsdQNameToGroupMap = ImmutableMap.copyOf(xsdQNameToGroupMap);
        this.ownerPair = ownerPair;
        this.typeToDescriptorMap = typeToDescriptorMap;
        this.groupToDescriptorMap = groupToDescriptorMap;
    }


    /////////// METHODS /////////////////////////////

    /**
     *
     * @param newXsdType
     * @param ownerInfo
     * @return
     */
    public ProcessCntx buildContextForNewType(XmlSchemaType newXsdType, OwnerInfo ownerInfo){
       ProcessCntx tmpCntx = new ProcessCntx(newXsdType,
                                                null,
                                                    xsdElemQNameToElemMap,
                                                        xsdQNameToTypeMap,
                                                            xsdQNameToGroupMap,
                                                                ownerInfo,
                                                                    typeToDescriptorMap,
                                                                        groupToDescriptorMap);
        return tmpCntx;

    }

    /**
     * Group declaration can't be anonymous and is on top level always<br/>
     * Owner argument means a type is refer to the processed group.
     *
     * @param newXsdGroup
     * @return
     */
    public ProcessCntx buildContextForNewGroup(XmlSchemaGroup newXsdGroup, OwnerInfo refOwner){
        ProcessCntx tmpCntx = new ProcessCntx(null,
                                                newXsdGroup,
                                                    xsdElemQNameToElemMap,
                                                        xsdQNameToTypeMap,
                                                            xsdQNameToGroupMap,
                                                                refOwner,
                                                                    typeToDescriptorMap,
                                                                        groupToDescriptorMap);
        return tmpCntx;

    }


    /////////// GETTERS/SETTERS /////////////////////

    public XmlSchemaType getType() {
        return type;
    }

    public void setType(XmlSchemaType type) {
        this.type = type;
    }

    public XmlSchemaGroup getGroup() {
        return group;
    }

    public GroupDescriptor getCurrentProcessedGroupDesc() {
        return currentProcessedGroupDesc;
    }

    public void setCurrentProcessedGroupDesc(GroupDescriptor currentProcessedGroupDesc) {
        this.currentProcessedGroupDesc = currentProcessedGroupDesc;
    }

    public TypeDescriptor getCurrentProcessedTypeDesc() {
        return currentProcessedTypeDesc;
    }

    public void setCurrentProcessedTypeDesc(TypeDescriptor currentProcessedTypeDesc) {
        this.currentProcessedTypeDesc = currentProcessedTypeDesc;
    }

    public Map<QName, XmlSchemaElement> getXsdElemQNameToElemMap() {
        //return Collections.unmodifiableMap(xsdElemQNameToElemMap);
        return xsdElemQNameToElemMap;
    }

    public Map<QName, XmlSchemaType> getXsdQNameToTypeMap() {
        return xsdQNameToTypeMap;
    }

    public Map<QName, XmlSchemaGroup> getXsdQNameToGroupMap() {
        return xsdQNameToGroupMap;
    }

    public OwnerInfo getOwnerPair() {
        return ownerPair;
    }

    public Map<XmlSchemaType, TypeDescriptor> getTypeToDescriptorMap() {
        if(typeToDescriptorMap==null){
            typeToDescriptorMap = new HashMap<>();
        }
        return typeToDescriptorMap;
    }

    public Map<XmlSchemaGroup, GroupDescriptor> getGroupToDescriptorMap() {
        if(groupToDescriptorMap ==null){
            groupToDescriptorMap = new HashMap<>();
        }
        return groupToDescriptorMap;
    }

}
