package ru.aklimov.wsdlcomparator.domain;

import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexandr Klimov
 */
public class DescriptorsContainer {

    private Set<TypeDescriptor> typeDescriptors;
    private Set<GroupDescriptor> groupDescriptors;

    public DescriptorsContainer() {
    }

    public DescriptorsContainer(Set<TypeDescriptor> typeDescriptors, Set<GroupDescriptor> groupDescriptors) {
        this.typeDescriptors = typeDescriptors;
        this.groupDescriptors = groupDescriptors;
    }



    public Set<TypeDescriptor> getTypeDescriptors() {
        if(typeDescriptors == null){
            typeDescriptors = new HashSet<>();
        }
        return typeDescriptors;
    }

    public void setTypeDescriptors(Set<TypeDescriptor> typeDescriptors) {
        this.typeDescriptors = typeDescriptors;
    }

    public Set<GroupDescriptor> getGroupDescriptors() {
        if(groupDescriptors == null){
            groupDescriptors = new HashSet<>();
        }
        return groupDescriptors;
    }

    public void setGroupDescriptors(Set<GroupDescriptor> groupDescriptor) {
        this.groupDescriptors = groupDescriptor;
    }
}
