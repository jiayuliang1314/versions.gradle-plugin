package cn.itcast.job.task.checkversion;

import cn.itcast.job.pojo.VersionsGradleLineBean;
import cn.itcast.job.utils.FileUtil;
import org.jsoup.Jsoup;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.CustomRedirectStrategy;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Selectable;

import java.io.IOException;
import java.security.Security;
import java.util.*;


public class CheckVersionTask implements PageProcessor {
    public String File_OF_VERSION = "";
    public String REPORT_PATH = "";
    private Map<String, VersionsGradleLineBean> mapOfKeysAndVersions = new HashMap<>();//代表有等号的左右相关的map，左边为key，右边为value
    private Map<String, VersionsGradleLineBean> mapOfLibrary = new HashMap<>();//用于检查是否有重复的依赖，作用范围在process里
    private List<Request> requestList = new ArrayList<>();
    private List<VersionsGradleLineBean> gradleLineBeans = new ArrayList<>();
    private List<String> repos = new ArrayList<>();
    private Site site = Site.me()
            .setCharset("utf8")//设置编码
            .setSleepTime(5 * 1000)//睡眠时间
            .setTimeOut(60 * 1000)//设置超时时间
            .setRetrySleepTime(3000)//设置重试的间隔时间
            .setRetryTimes(3)//设置重试的次数
            .setCycleRetryTimes(6)//设置重试的次数
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.84 Safari/535.11 SE 2.X MetaSr 1.0");

    @Override
    public Site getSite() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, DH keySize < 768");
        return site;
    }

    public void process(String fileOfVersion, String reportPath) throws Exception {
        File_OF_VERSION = fileOfVersion;
        REPORT_PATH = reportPath;
        FileUtil.readFileEveryLine(File_OF_VERSION, new FileUtil.ControlFileEveryLineCallback() {
            @Override
            public void control(String line) throws IOException {
                VersionsGradleLineBean versionsGradleLineBean = new VersionsGradleLineBean(line);
                gradleLineBeans.add(versionsGradleLineBean);
            }
        });

        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            String line = versionsGradleLineBean.getLine();
            if (line != null && line.length() > 0) {

                int numOfMaoHao = search(line, ":");

                if (numOfMaoHao >= 2) {
                    System.out.println("numOfMaoHao " + numOfMaoHao);
                    String endStr = "";
                    if (line.contains("'")) {
                        endStr = line.substring(line.indexOf("'") + 1);
                    } else if (line.contains("\"")) {
                        endStr = line.substring(line.indexOf("\"") + 1);
                    }
                    String[] array = endStr.split(":");


                    //1.判断是否重复了
                    String key = array[0] + "/" + array[1];
                    versionsGradleLineBean.setLibraryName(array[0] + ":" + array[1]);
                    if (!mapOfLibrary.containsKey(key)) {
                        mapOfLibrary.put(key, versionsGradleLineBean);
                        versionsGradleLineBean.setDuplicate(false);
                        //2.google android 相关的包，去访问google 的maven,测试不好使
//                        if (array[0].startsWith("android.") ||
//                                array[0].startsWith("androidx.") ||
//                                array[0].startsWith("com.android") ||
//                                array[0].startsWith("com.crashlytics.sdk.android") ||
//                                array[0].startsWith("com.google.") ||
//                                array[0].startsWith("io.fabric.sdk.android") ||
//                                array[0].startsWith("org.chromium.net") ||
//                                array[0].startsWith("org.jetbrains.kotlin") ||
//                                array[0].startsWith("tools.base.build-system.debug") ||
//                                array[0].startsWith("zipflinger")
//                        ) {
//                            Request request = new Request("https://maven.google.com/web/index.html#androidx.recyclerview:recyclerview"/*"https://maven.google.com/web/index.html#" + array[0] + ":" + array[1]*/);
//                            request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
//                            requestList.add(request);
//                        }
                        //3.加上mvnrepository
                        {
                            Request request = new Request("https://mvnrepository.com/artifact/" + array[0] + "/" + array[1]);
                            request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                            request.putExtra("array0", array[0]);
                            request.putExtra("array1", array[1]);
                            requestList.add(request);
                        }
                    } else {
                        versionsGradleLineBean.setDuplicate(true);
                    }
                }
            }

            System.out.println(versionsGradleLineBean);
            if (versionsGradleLineBean.getKey() != null && versionsGradleLineBean.getValue() != null) {
                mapOfKeysAndVersions.put(versionsGradleLineBean.getKey(), versionsGradleLineBean);
            }
        }

        Request[] requests = new Request[requestList.size()];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = requestList.get(i);
        }
        System.out.println("requests.length " + requests.length);
        List<SpiderListener> list = new ArrayList<>();
        list.add(new MySpiderListener());
        Spider.create(CheckVersionTask.this)
                .addRequest(requests)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000)))
                .thread(100)
                .setDownloader(new MyHttpClientDownloader())
                .setSpiderListeners(list)
                .run();

    }

    @Override
    public void process(Page page) {
        if (page.getStatusCode() != 200 || page.getRawText().contains("Not Found: /artifact/")
                || page.getRawText().contains("The requested path was not found.")
                || page.getRawText().contains("404 Not Found")
        ) {
            VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
            String array0 = (String) page.getRequest().getExtra("array0");
            String array1 = (String) page.getRequest().getExtra("array1");
            if (page.getStatusCode() != 404) {
                String url = page.getUrl().toString();
                System.out.println(url + "process happen Page status not 200 " + page.getStatusCode());
            }
            if (page.getRawText().contains("Not Found: /artifact/")) {
                String url = page.getUrl().toString();
                System.out.println(url + " Not Found: /artifact/");
                //4.加上jcenter
                {
//                            Request request = new Request("https://jcenter.bintray.com/com/google/android/flexbox/");
                    Request request = new Request("https://jcenter.bintray.com/" + array0.replace(".", "/") + "/" + array1.replace(".", "/"));
                    request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                    request.putExtra("array0", array0);
                    request.putExtra("array1", array1);
                    page.addTargetRequest(request);
                }
            }
            if (page.getRawText().contains("The requested path was not found.")) {
                String url = page.getUrl().toString();
                System.out.println(url + " The requested path was not found.");
                //5.加上jitpack
                {
//                    Request request = new Request("https://jitpack.io/com/github/jiayuliang1314/StrongToolsRecyclerView/");
                    Request request = new Request("https://jitpack.io/" + array0.replace(".", "/") + "/" + array1.replace(".", "/"));
                    request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                    request.putExtra("array0", array0);
                    request.putExtra("array1", array1);
                    page.addTargetRequest(request);
                }
            }
            if (page.getRawText().contains("404 Not Found")) {
                String url = page.getUrl().toString();
                System.out.println(url + " 404 Not Found");
            }
        } else {
            String url = page.getUrl().toString();
            if (url.startsWith("https://maven.google.com/web/index.html#")) {
                processMavenGoogle(page, url);
            }
            if (url.startsWith("https://mvnrepository.com/artifact/")) {
                processMvnrepository(page, url);
            }
            if (url.startsWith("https://jcenter.bintray.com/")) {
                processJcenter(page, url);
            }
            if (url.startsWith("https://jitpack.io/")) {
                processJitpack(page, url);
            }
        }
    }

    //region process
    private void processJitpack(Page page, String url) {
        //                System.out.println("link before " + url);
        String companyRedirectWebsit = CustomRedirectStrategy.RedirectMap.get(url);
//                System.out.println("link after " + companyRedirectWebsit);
        String version = "";
//                <pre><a onclick="navi(event)" href=":2.0.1/" rel="nofollow">2.0.1/</a></pre>
        try {
            String[] items = Jsoup.parse(page.getHtml().css("body").get()).text().split("/");
            version = items[items.length - 1].replace("/", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(url + " " + version);
        VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
        versionsGradleLineBean.setLastVersion(version, url);
    }

    private void processJcenter(Page page, String url) {
        //                System.out.println("link before " + url);
        String companyRedirectWebsit = CustomRedirectStrategy.RedirectMap.get(url);
//                System.out.println("link after " + companyRedirectWebsit);
        String version = "";
//                <pre><a onclick="navi(event)" href=":2.0.1/" rel="nofollow">2.0.1/</a></pre>
        try {
            version = Jsoup.parse(page.getHtml().css("pre").nodes().get(page.getHtml().css("pre").nodes().size() - 2).get()).text().replace("/", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(url + " " + version);
        VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
        versionsGradleLineBean.setLastVersion(version, url);
    }

    /**
     * ok
     *
     * @param page
     * @param url
     */
    private void processMvnrepository(Page page, String url) {
        VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
        String array0 = (String) page.getRequest().getExtra("array0");
        String array1 = (String) page.getRequest().getExtra("array1");

        //                System.out.println("link before " + url);
        String companyRedirectWebsit = CustomRedirectStrategy.RedirectMap.get(url);
//                System.out.println("link after " + companyRedirectWebsit);
        String version = "";
//                <div class="main-footer-text page-centered"><p><a href="https://www.outreach.io/">Outreach Home Page</a></p><a href="https://lever.co/" class="image-link"><span>Jobs powered by </span><img alt="Lever logo" src="/img/lever-logo-full.svg"></a></div>
        try {
            String href = page.getHtml().css("a.vbtn.release").css("a", "href").get();
            version = href.substring(href.lastIndexOf("/") + 1).trim();
        } catch (Exception e) {
            System.out.println(url + " happen Exception ");
            e.printStackTrace();
        }
        System.out.println(url + " " + version);
        versionsGradleLineBean.setLastVersion(version, url);

        //1.打印出所有的tab,并添加到请求中
        List<Selectable> selectables = page.getHtml().css("ul.tabs>li").nodes();
        for (Selectable selectable : selectables) {
            String href = selectable.css("a", "href").get();
            System.out.println("processMvnrepository href " + href);
            if (href.contains("=")) {
                repos.add(href.substring(href.lastIndexOf("=") + 1));
            }
            if (href.contains("Spring") || href.contains("spring") || href.contains("redhat") || href.contains("icm")) {
                //springio-libs-release
                //springio-plugins-release
                //spring-libs-milestone
                //redhat-ga
                //redhat-earlyaccess
                //icm
                System.out.println("processMvnrepository Spring or Redhat meet " + href.substring(href.lastIndexOf("=") + 1));
                continue;
            }

            Request request = new Request("https://mvnrepository.com" + href);
            request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
            request.putExtra("array0", array0);
            request.putExtra("array1", array1);
            page.addTargetRequest(request);
        }
    }

    /**
     * 不好使
     *
     * @param page
     * @param url
     */
    private void processMavenGoogle(Page page, String url) {
        //                System.out.println("link before " + url);
        String companyRedirectWebsit = CustomRedirectStrategy.RedirectMap.get(url);
//                System.out.println("link after " + companyRedirectWebsit);
        String version = "";
//                <div class="main-footer-text page-centered"><p><a href="https://www.outreach.io/">Outreach Home Page</a></p><a href="https://lever.co/" class="image-link"><span>Jobs powered by </span><img alt="Lever logo" src="/img/lever-logo-full.svg"></a></div>
        try {
            version = Jsoup.parse(page.getHtml().css("span.ng-binding").get()).text().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(url + " " + version);
        VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
        versionsGradleLineBean.setLastVersion(version, url);
    }
    //endregion

    //region done
    public void done() {
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

        String detail = "<h1>功能：</h1>" +
                "1.  versions.gradle 文件格式化<br>" +
                "2.  依赖最新版本号列表 给出链接，点击跳转到网页<br>" +
                "3.  versions.gradle 按行给出最新版本提示<br>" +
                "4.  依赖去重复 <font color=\"red\"> -> duplicate library,can delete!!! </font><br>" +
                "5.  多个依赖使用同一个版本，但是他们有独立的版本号 ，给出提示  <font color=\"green\">Many Lib Use Same $Version, But They Are Not Same</font><br>" +
                "    例如：androidx.annotation = \"androidx.annotation:annotation:$versions.androidx\"" +
                "         " + "androidx.core = \"androidx.core:core:$versions.androidx\" 都使用了$versions.androidx，但他们的最新版本号不同<br>" +
                "<br>" +
                "<br>";

        detail += "<br><h1>1.  versions.gradle 文件格式化</h1><br>";

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

        detail += "<br><h1>2.  依赖最新版本号列表 给出链接，点击跳转到网页</h1><br>";
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
            if (versionsGradleLineBean != null && !versionsGradleLineBean.isDuplicate() && versionsGradleLineBean.getLibraryName() != null && versionsGradleLineBean.getLibraryName().length() > 0) {
                detail += "<tr>\n" +
                        "<td>" + versionsGradleLineBean.getLibraryName() + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getVersion() == null ? "" : versionsGradleLineBean.getVersion()) + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getVersionListed() == null ? "" : versionsGradleLineBean.getVersionListed()) + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.getLastVersionReport()) + "</td>\n" +
                        "<td>" + versionsGradleLineBean.getLinkVersionBeanListHtml() + "</td>\n" +
                        "<td>" + (versionsGradleLineBean.isManyLibUseSameVersionListedButTheyAreNotSame() ? " <font color=\"green\">Many Lib Use Same $Version, But They Are Not Same</font>" : "") + "</td>\n" +
                        "</tr>\n";
            }
        }
        detail += "</table>\n";

        detail += "<br><h1>3.  versions.gradle 按行给出最新版本提示</h1><br>";
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            System.out.println(versionsGradleLineBean);
            detail += versionsGradleLineBean.getHtml();
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
        Set<String> set = new HashSet<String>(repos);
        repos = new ArrayList<String>(set);
        for (String item : repos) {
            System.out.println(item);
        }
    }
    //endregion

    //region 查找字符串里与指定字符串相同的个数
    public int search(String str, String strRes) {//查找字符串里与指定字符串相同的个数
        int n = 0;//计数器
        while (str.indexOf(strRes) != -1) {
            int i = str.indexOf(strRes);
            n++;
            str = str.substring(i + 1);
        }
        return n;
    }
    //endregion

    //region 监听器
    public class MySpiderListener implements SpiderListener {

        @Override
        public void onSuccess(Request request) {

        }

        @Override
        public void onError(Request request) {

        }

        @Override
        public void onComplete() {
            done();
        }
    }

    public class MyHttpClientDownloader extends HttpClientDownloader {
        @Override
        protected void onSuccess(Request request) {
        }

        @Override
        public void onError(Request request) {
            System.out.println("MyHttpClientDownloader onError");
        }
    }
    //endregion


}
