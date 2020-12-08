package cn.itcast.job.pojo.dependices;

import cn.itcast.job.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static cn.itcast.job.cache.VersionsGradleInfosCache.mapOfGroupLibraryNameAndVersionFromDependices;

/**
 * +--- androidx.databinding:databinding-adapters:3.6.1
 * |    +--- androidx.databinding:databinding-common:3.6.1
 * |    \--- androidx.databinding:databinding-runtime:3.6.1 (*)
 * <p>
 * (*) - is used to indicate that particular dependency is described somewhere else in the tree
 * -> - is used to point the dependency that wins in version conflict.
 * <p>
 * |    +--- androidx.collection:collection:1.0.0 -> 1.1.0
 * |    |    \--- androidx.annotation:annotation:1.1.0
 */
public class DependicesNode {
    public String moudle;//androidx.databinding
    public String library;//databinding-adapters
    public String version;//3.6.1
    public boolean isHaveStar;//(*) 是否含有星号标记
    public boolean isHaveArrow;//-> 是否含有箭号标记
    public String versionAfterArrow;//箭号后边的版本

    public boolean isRoot;//是否是根结点

    public DependicesNode parent;//父节点
    public List<DependicesNode> childs = new ArrayList<>();//子节点
    public int numOfShugang = 0;//|的个数


    public DependicesNode(boolean root, DependicesNode p) {
        isRoot = root;
        parent = p;
        numOfShugang = -1;
    }

    public DependicesNode(String line) {
        System.out.println("line " + line);
        isRoot = false;
        String detail = line.substring(line.indexOf("--- ") + 4);
        int frist = detail.indexOf(":");
        int second = detail.indexOf(":", frist + 1);
        moudle = detail.substring(0, frist);
        library = detail.substring(frist + 1, second);
        String detailVersion = detail.substring(second + 1);
        isHaveStar = false;
        isHaveArrow = false;
        if (detailVersion.contains(" (*)")) {
            isHaveStar = true;
            if (detailVersion.contains(" -> ")) {
                isHaveArrow = true;
                detailVersion = detailVersion.replace(" (*)", "");
                version = detailVersion.substring(0, detailVersion.indexOf(" -> "));
                versionAfterArrow = detailVersion.substring(detailVersion.indexOf(" -> ") + 4);
                System.out.println("versionAfterArrow " + versionAfterArrow);
                mapOfGroupLibraryNameAndVersionFromDependices.put(moudle + ":" + library, versionAfterArrow);
            } else {
                isHaveStar = true;
                version = detailVersion.replace(" (*)", "");
                mapOfGroupLibraryNameAndVersionFromDependices.put(moudle + ":" + library, version);
            }
        } else if (detailVersion.contains(" -> ")) {
            isHaveArrow = true;
            version = detailVersion.substring(0, detailVersion.indexOf(" -> "));
            versionAfterArrow = detailVersion.substring(detailVersion.indexOf(" -> ") + 4);
            System.out.println("versionAfterArrow " + versionAfterArrow);
            mapOfGroupLibraryNameAndVersionFromDependices.put(moudle + ":" + library, versionAfterArrow);
        } else {
            isHaveStar = false;
            isHaveArrow = false;
            version = detailVersion;
            versionAfterArrow = null;
            mapOfGroupLibraryNameAndVersionFromDependices.put(moudle + ":" + library, version);
        }
        numOfShugang = StringUtil.search(line, "|");
    }

    @Override
    public String toString() {
        return "DependicesNode{" +
                "moudle='" + moudle + '\'' +
                ", library='" + library + '\'' +
                ", version='" + version + '\'' +
                ", isHaveStar=" + isHaveStar +
                ", isHaveArrow=" + isHaveArrow +
                ", versionAfterArrow='" + versionAfterArrow + '\'' +
                ", isRoot=" + isRoot +
                ", parent=" + parent +
                ", numOfShugang=" + numOfShugang +
                '}';
    }

    public void setParent(DependicesNode peek) {
        parent = peek;
        peek.childs.add(this);
    }
}
