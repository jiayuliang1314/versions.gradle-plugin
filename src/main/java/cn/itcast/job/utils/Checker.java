package cn.itcast.job.utils;

import java.util.Collection;
import java.util.Map;

//ok
public class Checker {
    public static boolean isEmpty(Object[] array) {
        return isNull(array) || array.length == 0;
    }

    public static boolean isEmpty(Collection collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return isNull(map) || map.isEmpty();
    }

    public static boolean isEmpty(CharSequence charSequence) {
        return isNull(charSequence) || charSequence.length() == 0;
    }

    public static boolean isNull(Object object) {
        return object == null;
    }
}
