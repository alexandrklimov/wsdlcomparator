package ru.aklimov.wsdlcomparator.facades.impl;

import ru.aklimov.wsdlcomparator.modelbuilders.ViewModelCreator;
import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.impl.WSMethodDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.GroupDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.method.WSMethodDescrTable;
import ru.aklimov.wsdlcomparator.facades.IMethodModelCreatorFacade;

import java.util.Set;

/**
 * Created by aklimov on 09.03.14.
 */
public class MethodModelCreatorFacade implements IMethodModelCreatorFacade{
    public static final boolean DEFAULT_MERGE_WITH_BASE_TYPE_FLAG = true;
    public static final boolean DEFAULT_INCLUDE_REF_GROUP_FLAG = true;
    public static final int DEFAULT_DEEP_COUNT = 1;

    private ViewModelCreator vmc;

    public MethodModelCreatorFacade() {
        vmc = new ViewModelCreator();
    }

    public MethodModelCreatorFacade(ViewModelCreator vmc) {
        this.vmc = vmc;
    }


    /**
     * @param wsMethodsDiffs
     * @param typeTbls
     * @param groupTbls
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @param deepCount
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<WSMethodDiffInfo> wsMethodsDiffs, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, boolean includeRefGroup, boolean mergeWithBaseType, int deepCount) {
        return vmc.createWSMethodModelByDiffInfo(wsMethodsDiffs, typeTbls, groupTbls, includeRefGroup, mergeWithBaseType, deepCount);
    }

    /**
     * {@see MethodModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param wsMethodsDiffs
     * @param typeTbls
     * @param groupTbls
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<WSMethodDiffInfo> wsMethodsDiffs, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, boolean includeRefGroup, boolean mergeWithBaseType) {
        return vmc.createWSMethodModelByDiffInfo(wsMethodsDiffs, typeTbls, groupTbls, includeRefGroup, mergeWithBaseType, DEFAULT_DEEP_COUNT);
    }

    /**
     *{@see MethodModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     *
     * @param wsMethodsDiffs
     * @param typeTbls
     * @param groupTbls
     * @param deepCount
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<WSMethodDiffInfo> wsMethodsDiffs, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, int deepCount) {
        return vmc.createWSMethodModelByDiffInfo(wsMethodsDiffs, typeTbls, groupTbls, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, deepCount);
    }

    /**
     * {@see MethodModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param wsMethodsDiffs
     * @param typeTbls
     * @param groupTbls
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<WSMethodDiffInfo> wsMethodsDiffs, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls) {
        return vmc.createWSMethodModelByDiffInfo(wsMethodsDiffs, typeTbls, groupTbls, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_DEEP_COUNT);
    }


    /**
     * @param wsMethodDescr
     * @param typeTbls
     * @param groupTbls
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @param deepCount
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, boolean includeRefGroup, boolean mergeWithBaseType, int deepCount) {
        return vmc.createWSMethodModelByWSMethodDescr(wsMethodDescr, typeTbls, groupTbls, includeRefGroup, mergeWithBaseType, deepCount);
    }

    /**
     * {@see MethodModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param wsMethodDescr
     * @param typeTbls
     * @param groupTbls
     * @param includeRefGroup
     * @param mergeWithBaseType
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, boolean includeRefGroup, boolean mergeWithBaseType) {
        return vmc.createWSMethodModelByWSMethodDescr(wsMethodDescr, typeTbls, groupTbls, includeRefGroup, mergeWithBaseType, DEFAULT_DEEP_COUNT);
    }

    /**
     * {@see MethodModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     *
     * @param wsMethodDescr
     * @param typeTbls
     * @param groupTbls
     * @param deepCount
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls, int deepCount) {
        return vmc.createWSMethodModelByWSMethodDescr(wsMethodDescr, typeTbls, groupTbls, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, deepCount);
    }

    /**
     * {@see MethodModelCreatorFacade#DEFAULT_MERGE_WITH_BASE_TYPE_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_INCLUDE_REF_GROUP_FLAG}
     * {@see MethodModelCreatorFacade#DEFAULT_DEEP_COUNT}
     *
     * @param wsMethodDescr
     * @param typeTbls
     * @param groupTbls
     * @return
     */
    @Override
    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr, Set<TypeDescrTable> typeTbls, Set<GroupDescrTable> groupTbls) {
        return vmc.createWSMethodModelByWSMethodDescr(wsMethodDescr, typeTbls, groupTbls, DEFAULT_INCLUDE_REF_GROUP_FLAG, DEFAULT_MERGE_WITH_BASE_TYPE_FLAG, DEFAULT_DEEP_COUNT);
    }
}
