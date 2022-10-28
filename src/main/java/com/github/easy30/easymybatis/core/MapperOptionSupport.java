package com.github.easy30.easymybatis.core;

import com.github.easy30.easymybatis.UpdateOption;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MapperOptionSupport {
    public static Set<String> getIgnoreColumnSet(UpdateOption option) {

        if (option != null && ArrayUtils.isNotEmpty(option.getIgnoreColumns())) {

            return new HashSet<>(Arrays.asList(option.getIgnoreColumns()));
        }

        return null;
    }

    public static Map<String, String> getExtraColVals(UpdateOption option) {

        if (option != null && ArrayUtils.isNotEmpty(option.getColumnAndValues())) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < option.getColumnAndValues().length; i += 2) {
                map.put(option.getColumnAndValues()[i], "" + option.getColumnAndValues()[i + 1]);// ""+null to "null"
            }
            return map;

        }

        return null;
    }

    public static String getAndRemove(Map<String, String> extraColVals, String key1, String key2) {
        String value = null;
        if (extraColVals != null) {
            value = extraColVals.get(key1);
            if (value != null) {

                extraColVals.remove(key1);
            } else {
                value = extraColVals.get(key2);
                if (value != null) {
                    extraColVals.remove(key2);
                }
            }
        }
        return value;
    }

    public static String getTable(MapperOption option) {


        if (option != null && StringUtils.isNotEmpty(option.getTable())) {
            return option.getTable();
        }

        return null;
    }


    public static String getTable(EntityAnnotation entityAnnotation, MapperOption option) {
        String table = getTable(option);
        if (table == null) table = entityAnnotation.getTable();
        return table;
    }

}
