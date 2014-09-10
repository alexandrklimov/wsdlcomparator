package ru.aklimov.wsdlcomparator.facades.impl;

import ru.aklimov.wsdlcomparator.modelbuilders.ViewModelCreator;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.ModelBuildResult;
import ru.aklimov.wsdlcomparator.facades.ITypeModelCreatorFacade;

import java.util.Set;

/**
 * @author Alexandr Klimov
 */
public class TypeModelCreatorFacade implements ITypeModelCreatorFacade {
    public static final boolean DEFAULT_MERGE_WITH_BASE_TYPE_FLAG = true;
    public static final boolean DEFAULT_INCLUDE_REF_GROUP_FLAG = true;
    public static final int DEFAULT_DEEP_COUNT = 1;

    private ViewModelCreator vmc;

    public TypeModelCreatorFacade() {
        vmc = new ViewModelCreator();
    }

    public TypeModelCreatorFacade(ViewModelCreator vmc) {
        this.vmc = vmc;
    }



    /**
     * {@see TypeModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param typeDiffSet
     * @param groupDiffSet
     * @return
     */
    @Override
    public ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet, Set<GroupDiffInfo> groupDiffSet) {
        return vmc.createModelByDiffInfoSet(typeDiffSet, groupDiffSet, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_DEEP_COUNT);
    }

    /**
     * {@see TypeModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param typeDiffSet
     * @param groupDiffSet
     * @param mergeWithBaseType
     * @param includeRefGroup
     * @return
     */
    @Override
    public ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet, Set<GroupDiffInfo> groupDiffSet, boolean mergeWithBaseType, boolean includeRefGroup) {
        return vmc.createModelByDiffInfoSet(typeDiffSet, groupDiffSet, mergeWithBaseType, includeRefGroup, DEFAULT_DEEP_COUNT);
    }

    /**
     * {@see TypeModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     *
     * @param typeDiffSet
     * @param groupDiffSet
     * @param deepCount
     * @return
     */
    @Override
    public ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet, Set<GroupDiffInfo> groupDiffSet, int deepCount) {
        return vmc.createModelByDiffInfoSet(typeDiffSet, groupDiffSet, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_INCLUDE_REF_GROUP_FLAG, deepCount);
    }

    @Override
    public ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet, Set<GroupDiffInfo> groupDiffSet, boolean mergeWithBaseType, boolean includeRefGroup, int deepCount) {
        return vmc.createModelByDiffInfoSet(typeDiffSet, groupDiffSet, mergeWithBaseType, includeRefGroup, deepCount);
    }


    /**
     * {@see TypeModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param typeDescriptors
     * @param groupDescriptors
     * @return
     */
    @Override
    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors, Set<GroupDescriptor> groupDescriptors) {
        return vmc.createModelBySet(typeDescriptors, groupDescriptors, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_DEEP_COUNT);
    }

    /**
     * {@see TypeModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param typeDescriptors
     * @param groupDescriptors
     * @param mergeWithBaseType
     * @param includeRefGroup
     * @return
     */
    @Override
    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors, Set<GroupDescriptor> groupDescriptors, boolean mergeWithBaseType, boolean includeRefGroup) {
        return vmc.createModelBySet(typeDescriptors, groupDescriptors, mergeWithBaseType, includeRefGroup, DEFAULT_DEEP_COUNT);
    }

    /**
     * {@see TypeModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see TypeModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     *
     * @param typeDescriptors
     * @param groupDescriptors
     * @param deepCount
     * @return
     */
    @Override
    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors, Set<GroupDescriptor> groupDescriptors, int deepCount) {
        return vmc.createModelBySet(typeDescriptors, groupDescriptors, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_INCLUDE_REF_GROUP_FLAG, deepCount);
    }

    @Override
    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors, Set<GroupDescriptor> groupDescriptors, boolean mergeWithBaseType, boolean includeRefGroup, int deepCount) {
        return vmc.createModelBySet(typeDescriptors, groupDescriptors, mergeWithBaseType, includeRefGroup, deepCount);
    }
}
