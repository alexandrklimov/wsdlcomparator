package ru.aklimov.wsdlcomparator.facades;

import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.ModelBuildResult;

import java.util.Set;

/**
 * This interface presents a set of methods for view models creating.
 *
 * @author Alexand Klimov
 */
public interface ITypeModelCreatorFacade {

    ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet,
                                              Set<GroupDiffInfo> groupDiffSet);

    ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet,
                                              Set<GroupDiffInfo> groupDiffSet,
                                              boolean mergeWithBaseType,
                                              final boolean includeRefGroup);

    ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet,
                                              Set<GroupDiffInfo> groupDiffSet,
                                              int deepCount);

    ModelBuildResult createModelByDiffInfoSet(Set<TypeDiffInfo> typeDiffSet,
                                              Set<GroupDiffInfo> groupDiffSet,
                                              boolean mergeWithBaseType,
                                              final boolean includeRefGroup,
                                              int deepCount);


    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors,
                                             Set<GroupDescriptor> groupDescriptors);

    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors,
                                             Set<GroupDescriptor> groupDescriptors,
                                             final boolean mergeWithBaseType,
                                             final boolean includeRefGroup);

    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors,
                                             Set<GroupDescriptor> groupDescriptors,
                                             int deepCount);

    public ModelBuildResult createModelBySet(Set<TypeDescriptor> typeDescriptors,
                                             Set<GroupDescriptor> groupDescriptors,
                                             final boolean mergeWithBaseType,
                                             final boolean includeRefGroup,
                                             int deepCount);

}
