package cn.itcast.job.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * versions.umeng = "6.1.2"
 * deps.umeng = "com.umeng.analytics:analytics:$versions.umeng"
 */
public class VersionsGradleLineBean implements Serializable, Cloneable {
    private String line;
    private String key;//等号前边的内容   deps.umeng
    private String value;//等号后边的内容 "com.umeng.analytics:analytics:$versions.umeng"
    private String libraryName;//com.umeng.analytics:analytics
    private String version;//等号后边的内容,取得的版本号 $versions.umeng
    private String versionListed;//版本号单独列出来的 6.1.2
    private String lastVersion;//最新版本号 6.9.0
    private List<LinkVersionBean> linkVersionBeanList = new ArrayList<>();//版本号,link list,可能多个仓库最新的版本号不一样，都保存着
    private List<VersionsGradleLineBean> versionsGradleLineBeanList = new ArrayList<>();//版本行对应的所有具体依赖的行信息
    private boolean manyLibUseSameVersionListedButTheyAreNotSame;//多个依赖使用同一个例如versions.umeng，但是他们最新的不一样
    private boolean duplicate;//重复了
    private boolean isVersionLine;//是否是版本行

    public VersionsGradleLineBean(String line) {
        this.line = line;
        if (line != null && line.length() > 0) {
            if (line.contains("=")) {
                String[] keyValueArray = line.split("=");
                key = keyValueArray[0].trim();
                value = keyValueArray[1].trim();
                int numOfMaoHao = search(line, ":");

                if (numOfMaoHao >= 2) {
                    version = value.substring(value.lastIndexOf(":") + 1, value.length() - 1);
                }
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public List<LinkVersionBean> getLinkVersionBeanList() {
        return linkVersionBeanList;
    }

    public void setLinkVersionBeanList(List<LinkVersionBean> linkVersionBeanList) {
        this.linkVersionBeanList = linkVersionBeanList;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isManyLibUseSameVersionListedButTheyAreNotSame() {
        return manyLibUseSameVersionListedButTheyAreNotSame;
    }

    public void setManyLibUseSameVersionListedButTheyAreNotSame(boolean manyLibUseSameVersionListedButTheyAreNotSame) {
        this.manyLibUseSameVersionListedButTheyAreNotSame = manyLibUseSameVersionListedButTheyAreNotSame;
    }

    public String getVersionListed() {
        return versionListed;
    }

    public void setVersionListed(String versionListed) {
        this.versionListed = versionListed;
    }

    public String getLastVersionReport() {
        if (false && getLibraryName().contains("StrongToolsRecyclerView")) {
            int i = 0;
        }
        if (manyLibUseSameVersionListedButTheyAreNotSame) {
            return lastVersion;
        } else if (getLastVersion() == null ||
                getLastVersion().equals(getVersion()) ||
                getLastVersion().equals(getVersionListed())) {
            return "";
        }
        return lastVersion;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String least, String url) {
        if ("unknown".equals(least)) {
            return;
        }
        if (lastVersion == null) {
            lastVersion = least;
        } else {
            for (LinkVersionBean linkVersionBean : linkVersionBeanList) {
                if (Objects.equals(least, linkVersionBean.version)) {
                    return;
                }
            }
            lastVersion += " " + least;
        }
        LinkVersionBean linkVersionBean = new LinkVersionBean();
        linkVersionBean.link = url;
        linkVersionBean.version = least;
        linkVersionBeanList.add(linkVersionBean);
    }

    public boolean setLastVersionAndCheck(String least, List<LinkVersionBean> urls, VersionsGradleLineBean versionsGradleLineBean) {
        isVersionLine = true;
        if (lastVersion != null) {
            if (Objects.equals(least, lastVersion)) {
                //donothing
            } else {
                lastVersion += " | " + least;
                setManyLibUseSameVersionListedButTheyAreNotSame(true);
                for (int i = 0; i < versionsGradleLineBeanList.size(); i++) {
                    versionsGradleLineBeanList.get(i).setManyLibUseSameVersionListedButTheyAreNotSame(true);
                }
            }
        } else {
            lastVersion = least;
        }
        versionsGradleLineBeanList.add(versionsGradleLineBean);
        linkVersionBeanList.addAll(urls);
        return manyLibUseSameVersionListedButTheyAreNotSame;
    }

    @Override
    public String toString() {
        if (duplicate) {
            return line + " -> duplicate library,can delete!!!";
        }
        if (manyLibUseSameVersionListedButTheyAreNotSame) {
            return line +
//                    (version == null ? "" : ("  version=" + version + ' ')) +
                    (versionListed == null ? "" : ("  before -> " + versionListed + ' ')) +
                    (lastVersion == null ? "" : ("  least -> " + lastVersion + ' ')) +
                    " Many Lib Use Same $Version, But They Are Not Same";
        }
        if (version != null && version.startsWith("$")) {
            return line;
        }
        if (version != null || versionListed != null || lastVersion != null) {
            if (isVersionLine) {
                String valueVersion = value.replace("\"", "").replace("'", "");
                if (Objects.equals(valueVersion, lastVersion)) {
                    return line;
                } else {
                    return line + ("  --> " + lastVersion.replace(valueVersion, "") + ' ');
                }
            } else {
                return line +
                        ((lastVersion == null || Objects.equals(lastVersion, version)) ? "" : ("  --> " + lastVersion + ' '));
            }
        }
        return line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private int search(String str, String strRes) {//查找字符串里与指定字符串相同的个数
        int n = 0;//计数器
        while (str.indexOf(strRes) != -1) {
            int i = str.indexOf(strRes);
            n++;
            str = str.substring(i + 1);
        }
        return n;
    }

    public String getHtml() {
        if (duplicate) {
            return line + " <font color=\"red\"> -> duplicate library,can delete!!! </font>";
        }
        if (manyLibUseSameVersionListedButTheyAreNotSame) {
            return line +
//                    (version == null ? "" : ("  version=" + version + ' ')) +
                    (versionListed == null ? "" : ("  before -> " + versionListed + ' ')) +
                    (lastVersion == null ? "" : ("  least -> " + getHtmlLeastVersion(null) + ' ')) +
                    " <font color=\"green\">Many Lib Use Same $Version, But They Are Not Same</font>";
        }
        if (version != null && version.startsWith("$")) {
            return line;
        }
        if (version != null || versionListed != null || lastVersion != null) {
            if (isVersionLine) {
                String valueVersion = value.replace("\"", "").replace("'", "");
                if (Objects.equals(valueVersion, lastVersion)) {
                    return line;
                } else {
                    return line + ("  --> " + getHtmlLeastVersion(valueVersion) + ' ');
                }
            } else {
                return line +
                        ((lastVersion == null || Objects.equals(lastVersion, version)) ? "" : ("  --> " + getHtmlLeastVersion(null) + ' '));
            }
        }
        return line;
    }

    public String getHtmlLeastVersion(String before) {
        String link = "";
        for (LinkVersionBean linkVersionBean : linkVersionBeanList) {
            if (before != null && Objects.equals(linkVersionBean.version, before)) {
                continue;
            }
            link += " " + "<a href=\"" + linkVersionBean.link + "\">" + linkVersionBean.version + "</a>" + " ";
        }
        return link;
    }

    public String getLinkVersionBeanListHtml() {
        String link = "";
        if (manyLibUseSameVersionListedButTheyAreNotSame) {
            for (LinkVersionBean linkVersionBean : linkVersionBeanList) {
                link += " <a href=\"" + linkVersionBean.link + "\">" + linkVersionBean.link + " " + linkVersionBean.version + "</a>" + " <br>";
            }
        } else {
            for (LinkVersionBean linkVersionBean : linkVersionBeanList) {
                if (getLastVersion() == null ||
                        getLastVersion().equals(getVersion()) ||
                        getLastVersion().equals(getVersionListed())) {
                    continue;
                }
                link += " <a href=\"" + linkVersionBean.link + "\">" + linkVersionBean.link + " " + linkVersionBean.version + "</a>" + " <br>";
            }
        }
        return link;
    }

    public String getFormatKeyVersion() {
        if (version != null && version.length() > 0 && !version.startsWith("$")) {
            String newVersionName = "versions." + key.replace(".", "_");
            return key + " = " + value.replace(version, "$" + newVersionName);
        } else if (version != null && version.length() > 0 && version.startsWith("$") && value.startsWith("'")) {
            return key + " = " + value.replace("'", "\"");
        }
        return line;
    }

    public String getFormatVersion() {
        if (version != null && version.length() > 0 && !version.startsWith("$")) {
            String newVersionName = "versions." + key.replace(".", "_") + " = \"" + version + "\"";
            return newVersionName;
        }
        return null;
    }

    public static class LinkVersionBean implements Serializable {
        public String link;
        public String version;
    }
}
