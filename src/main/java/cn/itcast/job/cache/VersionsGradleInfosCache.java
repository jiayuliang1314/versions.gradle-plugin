package cn.itcast.job.cache;

import cn.itcast.job.pojo.VersionsGradleLineBean;
import cn.itcast.job.pojo.dependices.DependicesNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionsGradleInfosCache {
    public static List<VersionsGradleLineBean> gradleLineBeans = new ArrayList<>();
    public static Map<String, VersionsGradleLineBean> mapOfKeysAndVersions = new HashMap<>();//代表有等号的左右相关的map，左边为key，右边为value
    public static Map<String, VersionsGradleLineBean> mapOfGroupLibraryNameAndDetailLine = new HashMap<>();//代表有依赖名和具体信息的行的map
    public static Map<String, String> mapOfGroupLibraryNameAndVersionFromDependices = new HashMap<>();//代表有依赖名和版本号行的map
    public static Map<String, List<DependicesNode>> mapOfGroupLibraryNameAndVersionFromDependicesNodes = new HashMap<>();//代表有依赖名和版本号行的map

    public static void putInMapOfGroupLibraryNameAndVersionFromDependices(String key, String version, DependicesNode dependicesNode) {
        if (version != null) {
            mapOfGroupLibraryNameAndVersionFromDependices.put(key, version);
        }
        List<DependicesNode> list = mapOfGroupLibraryNameAndVersionFromDependicesNodes.get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(dependicesNode);
        mapOfGroupLibraryNameAndVersionFromDependicesNodes.put(key, list);
    }
}
