package cn.itcast.job.task.checkversion;

import cn.itcast.job.pojo.VersionsGradleLineBean;
import cn.itcast.job.pojo.dependices.DependicesNode;
import cn.itcast.job.utils.CollectionsUtils;
import cn.itcast.job.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static cn.itcast.job.cache.ConfigConstant.REPORT_PATH;
import static cn.itcast.job.cache.VersionsGradleInfosCache.*;

public class ReportTask {
    public static String detail;

    public static void done() {
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            if (versionsGradleLineBean.getVersion() != null && versionsGradleLineBean.getVersion().startsWith("$")) {
                VersionsGradleLineBean versionSpicalLine = mapOfKeysAndVersions.get(versionsGradleLineBean.getVersion().replace("$", ""));
                if (versionSpicalLine != null) {
                    String versionListed = versionSpicalLine.getValue();
                    versionsGradleLineBean.setVersionListed(versionListed
                            .replace("\"", "")
                            .replace("'", ""));

                    boolean manyLibUseSameVersionListedButTheyAreNotSame =
                            versionSpicalLine.setLastVersionAndCheck(
                                    versionsGradleLineBean.getLastVersion(),
                                    versionsGradleLineBean.getLinkVersionBeanList(),
                                    versionsGradleLineBean);
                    versionsGradleLineBean.setManyLibUseSameVersionListedButTheyAreNotSame(manyLibUseSameVersionListedButTheyAreNotSame);
                }
            }
        }

        detail = "<h1>功能：</h1>" +
                "1.  依赖最新版本号列表 给出链接，点击跳转到网页<br>" +
                "2.  依赖最终使用的版本和version.gradle声明的不同，给出提示<br>" +
                "3.  versions.gradle 按行给出最新版本提示<br>" +
                "4.  versions.gradle 文件格式化<br><br><br>" +
                "其他<br>" +
                "a). 版本号去重复 <font color=\"red\"> -> duplicate version,can delete!!! </font><br>" +
                "    依赖去重复 <font color=\"red\"> -> duplicate library,can delete!!! </font><br>" +
                "b). 多个依赖使用同一个版本，但是他们有独立的版本号 ，给出提示  <font color=\"green\">Many Lib Use Same $Version, But They Are Not Same</font><br>" +
                "    例如：androidx.annotation = \"androidx.annotation:annotation:$versions.androidx\"" +
                "         " + "androidx.core = \"androidx.core:core:$versions.androidx\" 都使用了$versions.androidx，但他们的最新版本号不同<br>" +
                "<br>" +
                "<br>";


        detail += "<br><h1>1.  依赖最新版本号列表 给出链接，点击跳转到网页</h1><br>";
        detail += "<table border=\"1\">\n" +
                "<tr>\n" +
                "<td>" + "Library Name" + "</td>\n" +
                "<td>" + "Version" + "</td>\n" +
                "<td>" + "Version Listed" + "</td>\n" +
                "<td>" + "Version Latest" + "</td>\n" +
                "<td>" + "Link" + "</td>\n" +
                "<td>" + "ManyLibUseSameVersionButTheyAreNotSame" + "</td>\n" +
                "</tr>\n";
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            if (versionsGradleLineBean != null && !versionsGradleLineBean.isDuplicate() && versionsGradleLineBean.getGroupAndLibraryName() != null && versionsGradleLineBean.getGroupAndLibraryName().length() > 0) {
                detail += "<tr>\n" +
                        "<td>" + versionsGradleLineBean.getGroupAndLibraryName() + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getVersion() == null ? "" : versionsGradleLineBean.getVersion()) + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getVersionListed() == null ? "" : versionsGradleLineBean.getVersionListed()) + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getLastVersionReport()) + "</td>\n" +
                        "<td>" + versionsGradleLineBean.getLinkVersionBeanListHtml() + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.isManyLibUseSameVersionListedButTheyAreNotSame() ? " <font color=\"green\">Many Lib Use Same $Version, But They Are Not Same</font>" : "") + "</td>\n" +
                        "</tr>\n";
                mapOfGroupLibraryNameAndDetailLine.put(versionsGradleLineBean.getGroupAndLibraryName(), versionsGradleLineBean);
            }
        }
        detail += "</table>\n";

        detail += "<br><h1>2.  依赖最终使用的版本和version.gradle声明的不同，给出提示</h1><br>";
        DependicesNode root = DependicesFileReadAndGetTree.dependicesFileReadAndGetTree();
        CollectionsUtils.mapTraversal(mapOfGroupLibraryNameAndDetailLine, new CollectionsUtils.MapTraversalCallback<String, VersionsGradleLineBean>() {
            @Override
            public void visit(Map.Entry<String, VersionsGradleLineBean> entry) {
                try {
                    String key = entry.getKey();
                    System.out.println("key " + key);
                    String value = entry.getValue().getVersionListed();
                    System.out.println("entry value " + value);
                    if (value != null && mapOfGroupLibraryNameAndVersionFromDependices.get(key) != null && !mapOfGroupLibraryNameAndVersionFromDependices.get(key).equals(value)) {
//                        detail += key + " " + entry.getValue().getVersionListed() + " in versions.gradle,but " + mapOfGroupLibraryNameAndVersionFromDependices.get(key) + " resolved";
                        detail += "<br>";
                        detail += "<table border=\"1\">\n" +
                                "<tr>\n" +
                                "<td>" + key + " " + entry.getValue().getVersionListed() + " in versions.gradle,but " + mapOfGroupLibraryNameAndVersionFromDependices.get(key) + " resolved" + "</td>\n" +
                                "</tr>\n";
                        detail += "</table>\n";
                        detail += "<br>";
                        List<DependicesNode> nodes = mapOfGroupLibraryNameAndVersionFromDependicesNodes.get(key);
                        Stack<DependicesNode> stack = new Stack<>();
                        for (DependicesNode node : nodes) {
                            stack = new Stack<>();
                            printNodeAndParent(node, stack);
                        }
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        detail += "<br><h1>3.  versions.gradle 按行给出最新版本提示</h1><br>";
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            System.out.println(versionsGradleLineBean);
            detail += versionsGradleLineBean.getHtml();
            detail += "<br>";
        }

        detail += "<br><h1>4.  versions.gradle 文件格式化</h1><br>";

        List<VersionsGradleLineBean> gradleLineBeansCopy = new ArrayList<>();
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            try {
                gradleLineBeansCopy.add((VersionsGradleLineBean) versionsGradleLineBean.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        List<VersionsGradleLineBean> newVersionLines = new ArrayList<>();
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeansCopy) {
            if (versionsGradleLineBean.getFormatVersion() != null) {
                VersionsGradleLineBean newVersion = new VersionsGradleLineBean(versionsGradleLineBean.getFormatVersion());
                newVersionLines.add(newVersion);
            }
        }
        gradleLineBeansCopy.addAll(2, newVersionLines);

        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeansCopy) {
            detail += versionsGradleLineBean.getFormatKeyVersion();
            detail += "<br>";
        }

        String content = "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "</head>\n" +
                "<body>\n" +
                detail +
                "<body>\n" +
                "</html>\n";
        FileUtil.writeFile(REPORT_PATH, content, false);

        //利用HashSet去重
//        Set<String> set = new HashSet<String>(repos);
//        repos = new ArrayList<String>(set);
//        for (String item : repos) {
//            System.out.println(item);
//        }
    }

    private static void printNodeAndParent(DependicesNode node, Stack<DependicesNode> stack) {
        if (node != null && !node.isRoot) {
            stack.push(node);
            if (node.parent != null) {
                printNodeAndParent(node.parent, stack);
            }
        } else {
            while (!stack.isEmpty()) {
                DependicesNode head = stack.pop();
                detail += head.line + "<br>";
            }
            detail += "<br>";
        }
    }

}
