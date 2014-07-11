package ru.aklimov.wsdlcomparator.facades;

import ru.aklimov.wsdlcomparator.domain.descriptors.WSMethodDescr;
import ru.aklimov.wsdlcomparator.domain.diff.impl.DiffWSMethodInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.GroupDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.WSMethodDescrTable;

import java.util.Set;

/**
 * Created by aklimov on 09.03.14.
 */
public interface IMethodModelCreatorFacade {

    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<DiffWSMethodInfo> wsMethodsDiffs,
                                                                 Set<TypeDescrTable> typeTbls,
                                                                 Set<GroupDescrTable> groupTbls,
                                                                 boolean includeRefGroup,
                                                                 boolean mergeWithBaseType,
                                                                 int deepCount);

    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<DiffWSMethodInfo> wsMethodsDiffs,
                                                                 Set<TypeDescrTable> typeTbls,
                                                                 Set<GroupDescrTable> groupTbls,
                                                                 boolean includeRefGroup,
                                                                 boolean mergeWithBaseType);

    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<DiffWSMethodInfo> wsMethodsDiffs,
                                                                 Set<TypeDescrTable> typeTbls,
                                                                 Set<GroupDescrTable> groupTbls,
                                                                 int deepCount);

    public Set<WSMethodDescrTable> createWSMethodModelByDiffInfo(Set<DiffWSMethodInfo> wsMethodsDiffs,
                                                                 Set<TypeDescrTable> typeTbls,
                                                                 Set<GroupDescrTable> groupTbls);


    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr,
                                                                      Set<TypeDescrTable> typeTbls,
                                                                      Set<GroupDescrTable> groupTbls,
                                                                      boolean includeRefGroup,
                                                                      boolean mergeWithBaseType,
                                                                      int deepCount);

    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr,
                                                                      Set<TypeDescrTable> typeTbls,
                                                                      Set<GroupDescrTable> groupTbls,
                                                                      boolean includeRefGroup,
                                                                      boolean mergeWithBaseType);

    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr,
                                                                      Set<TypeDescrTable> typeTbls,
                                                                      Set<GroupDescrTable> groupTbls,
                                                                      int deepCount);

    public Set<WSMethodDescrTable> createWSMethodModelByWSMethodDescr(Set<WSMethodDescr> wsMethodDescr,
                                                                      Set<TypeDescrTable> typeTbls,
                                                                      Set<GroupDescrTable> groupTbls);

}
