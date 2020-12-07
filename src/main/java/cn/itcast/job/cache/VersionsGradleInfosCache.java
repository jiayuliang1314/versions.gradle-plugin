package cn.itcast.job.cache;

import cn.itcast.job.pojo.VersionsGradleLineBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionsGradleInfosCache {
    public static List<VersionsGradleLineBean> gradleLineBeans = new ArrayList<>();
    public static Map<String, VersionsGradleLineBean> mapOfKeysAndVersions = new HashMap<>();//代表有等号的左右相关的map，左边为key，右边为value
    public static Map<String, VersionsGradleLineBean> mapOfGroupLibraryNameAndDetailLine = new HashMap<>();//代表有依赖名和具体信息的行的map
}