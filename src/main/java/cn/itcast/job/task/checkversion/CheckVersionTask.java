package cn.itcast.job.task.checkversion;

import cn.itcast.job.cache.ConfigConstant;
import cn.itcast.job.pojo.VersionsGradleLineBean;
import cn.itcast.job.utils.FileUtil;
import cn.itcast.job.utils.StringUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.itcast.job.cache.ConfigConstant.File_OF_VERSION;
import static cn.itcast.job.cache.ConfigConstant.REPORT_PATH;
import static cn.itcast.job.cache.VersionsGradleInfosCache.*;
import static cn.itcast.job.utils.StringUtil.search;

public class CheckVersionTask implements PageProcessor {
    //    private List<String> repos = new ArrayList<>();//用于输出仓库名，辅助用
    private Site site = Site.me()
            .setCharset("utf8")//设置编码
            .setSleepTime(5 * 1000)//睡眠时间
            .setTimeOut(60 * 1000)//设置超时时间
            .setRetrySleepTime(3000)//设置重试的间隔时间
            .setRetryTimes(3)//设置重试的次数
            .setCycleRetryTimes(6)//设置重试的次数
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50");

    @Override
    public Site getSite() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, DH keySize < 768");
        return site;
    }

    public void process(String fileOfVersion, String dependices, String reportPath) throws Exception {
        File_OF_VERSION = fileOfVersion;
        ConfigConstant.DEPENDICES_PATH = dependices;
        REPORT_PATH = reportPath;
        gradleLineBeans.clear();
        mapOfKeysAndVersions.clear();
        mapOfGroupLibraryNameAndDetailLine.clear();
        List<Request> requestList = new ArrayList<>();
        FileUtil.readFileEveryLine(File_OF_VERSION, new FileUtil.ControlFileEveryLineCallback() {
            @Override
            public void control(String line) throws IOException {
                VersionsGradleLineBean versionsGradleLineBean = new VersionsGradleLineBean(line);
                gradleLineBeans.add(versionsGradleLineBean);
            }
        });
        Map<String, VersionsGradleLineBean> mapOfLibrary = new HashMap<>();//用于检查是否有重复的依赖，作用范围在process里
        for (VersionsGradleLineBean versionsGradleLineBean : gradleLineBeans) {
            String line = versionsGradleLineBean.getLine();
            if (line != null && line.length() > 0) {

                int numOfMaoHao = search(line, ":");

                if (numOfMaoHao >= 2) {
                    System.out.println("numOfMaoHao " + numOfMaoHao);
                    //1.判断是否重复了
                    String key = versionsGradleLineBean.getGroupName() + "/" + versionsGradleLineBean.getLibraryName();

                    if (!mapOfLibrary.containsKey(key)) {
                        mapOfLibrary.put(key, versionsGradleLineBean);
                        versionsGradleLineBean.setDuplicate(false);

                        Request request = new Request(
                                "https://mvnrepository.com/artifact/"
                                        + versionsGradleLineBean.getGroupName()
                                        + "/"
                                        + versionsGradleLineBean.getLibraryName());
                        request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                        request.putExtra("groupName", versionsGradleLineBean.getGroupName());
                        request.putExtra("libraryName", versionsGradleLineBean.getLibraryName());
                        requestList.add(request);
                    } else {
                        versionsGradleLineBean.setDuplicate(true);
                    }
                }
            }

            System.out.println(versionsGradleLineBean);
            if (versionsGradleLineBean.getKey() != null && versionsGradleLineBean.getValue() != null) {
                if (mapOfKeysAndVersions.containsKey(versionsGradleLineBean.getKey())
                        && StringUtil.search(versionsGradleLineBean.getValue(), "\"") == 2
                        && StringUtil.search(versionsGradleLineBean.getValue(), ":") == 0
                ) {
                    mapOfKeysAndVersions.get(versionsGradleLineBean.getKey()).setVersionLineDuplicate(true);
                }
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
            String groupName = (String) page.getRequest().getExtra("groupName");
            String libraryName = (String) page.getRequest().getExtra("libraryName");
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
                    Request request = new Request("https://jcenter.bintray.com/" + groupName.replace(".", "/") + "/" + libraryName.replace(".", "/"));
                    request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                    request.putExtra("groupName", groupName);
                    request.putExtra("libraryName", libraryName);
                    page.addTargetRequest(request);
                }
            }
            if (page.getRawText().contains("The requested path was not found.")) {
                String url = page.getUrl().toString();
                System.out.println(url + " The requested path was not found.");
                //5.加上jitpack
                {
//                    Request request = new Request("https://jitpack.io/com/github/jiayuliang1314/StrongToolsRecyclerView/");
                    Request request = new Request("https://jitpack.io/" + groupName.replace(".", "/") + "/" + libraryName.replace(".", "/"));
                    request.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                    request.putExtra("groupName", groupName);
                    request.putExtra("libraryName", libraryName);
                    page.addTargetRequest(request);
                }
            }
            if (page.getRawText().contains("404 Not Found")) {
                String url = page.getUrl().toString();
                System.out.println(url + " 404 Not Found");
            }
        } else {
            String url = page.getUrl().toString();
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
        String groupName = (String) page.getRequest().getExtra("groupName");
        String libraryName = (String) page.getRequest().getExtra("libraryName");

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
//                repos.add(href.substring(href.lastIndexOf("=") + 1));
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
            request.putExtra("groupName", groupName);
            request.putExtra("libraryName", libraryName);
            page.addTargetRequest(request);
        }
    }
    //endregion

    //region done
    public void done() {
        ReportTask.done();
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
