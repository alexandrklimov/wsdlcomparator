package ru.aklimov.wsdlcomparator.modelbuilders;

import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import ru.aklimov.wsdlcomparator.domain.descriptors.GroupDescriptor;
import ru.aklimov.wsdlcomparator.domain.descriptors.TypeDescriptor;
import ru.aklimov.wsdlcomparator.domain.diff.impl.GroupDiffInfo;
import ru.aklimov.wsdlcomparator.domain.diff.impl.TypeDiffInfo;
import ru.aklimov.wsdlcomparator.domain.tblmodel.GroupDescrTable;
import ru.aklimov.wsdlcomparator.domain.tblmodel.TypeDescrTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class contains a set of auxiliary methods are used by model builders/creators.
 * <br>
 * This class intended for using in scope a models building process only.
 *
 * @author Alexandr Klimov
 */
class Utils {

    static String getIndicatorName(Class<? extends XmlSchemaGroupParticle> groupParticleType){
        return groupParticleType.getSimpleName().replace("XmlSchema", "");
    }

    static String buildGroupDescrTableId(GroupDescriptor gd){
        return Constants.GROUP_TABLE_ID_PREFIX + gd.getId();
    }

    static String buildTypeDescrTableId(TypeDescriptor td){
        return Constants.TYPE_TABLE_ID_PREFIX + td.getId();
    }

    /**
     * This auxiliary method performs search in a Set of TypeDescrTable by a table id.
     *
     * @param tableSet
     * @return found table or null
     */
    static TypeDescrTable searchTypeTableById(final String id, final Set<TypeDescrTable> tableSet){
        if(id == null || tableSet == null){
            return null;
        }

        TypeDescrTable resTdt = null;
        for(TypeDescrTable tdt : tableSet){
            if( id.equals(tdt.getId()) ){
                resTdt = tdt;
                break;
            }
        }
        return resTdt;
    }


    static GroupDescrTable searchGroupTableById(final String id, final Set<GroupDescrTable> tableSet){
        if(id == null || tableSet == null){
            return null;
        }

        GroupDescrTable resGd = null;
        for(GroupDescrTable gdt : tableSet){
            if( id.equals(gdt.getId()) ){
                resGd = gdt;
                break;
            }
        }
        return resGd;
    }


    static Map<TypeDescriptor, TypeDiffInfo> transformTypeDiffSetToMap(Set<TypeDiffInfo> infoSet){
        Map<TypeDescriptor, TypeDiffInfo> resMap = new HashMap<>();
        for(TypeDiffInfo df : infoSet){
            resMap.put(df.getTypeDescr(), df);
        }
        return resMap;
    }


    static Map<GroupDescriptor, GroupDiffInfo> transformGroupDiffSetAsMap(Set<GroupDiffInfo> infoSet){
        Map<GroupDescriptor, GroupDiffInfo> resMap = new HashMap<>();
        for(GroupDiffInfo df : infoSet){
            resMap.put(df.getGrpDescr(), df);
        }
        return resMap;
    }

}
