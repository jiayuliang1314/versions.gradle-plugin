package cn.itcast.job.utils;

import java.util.*;

public class CollectionsUtils {
    public static <T> List<T> getListFromArray(T[] array) {
        List<T> resultList = new ArrayList<>(array.length);

        Collections.addAll(resultList, array);
        return resultList;
    }

    /**
     * every step add one item
     *
     * @param i    begin from 1  1，2，3，4。。。
     * @param step
     * @param list
     * @param item
     * @param <T>
     */
    public static <T> void addItemInListEveryStep(int i, int step, List<T> list, T item) {
        list.add(i * step - 1, item);
    }

    //https://blog.csdn.net/xujiangdong1992/article/details/79738717
    //比较两个list
    //取出存在menuOneList中，但不存在resourceList中的数据，差异数据放入differentList
    public static <T> List<T> listCompare(List<T> menuOneList, List<T> resourceList) {
        Map<T, Integer> map = new HashMap<T, Integer>(resourceList.size());
        List<T> differentList = new ArrayList<T>();
        for (T resource : resourceList) {
            map.put(resource, 1);
        }
        for (T resource1 : menuOneList) {
            if (map.get(resource1) == null) {
                differentList.add(resource1);
            }
        }
        return differentList;
    }

    public static <K, V> void mapTraversal(Map<K, V> map, MapTraversalCallback<K, V> mapTraversalCallback) {
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            if (mapTraversalCallback != null) {
                mapTraversalCallback.visit(entry);
            }
        }
    }

    public static List<String> removeDuplicate(List<String> list) {
        HashSet<String> h = new HashSet<String>(list);
        list.clear();
        list.addAll(h);
        return list;
    }

    public interface MapTraversalCallback<K, V> {
        void visit(Map.Entry<K, V> entry);
    }
}
