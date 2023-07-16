package cn.itcast.job.task.checkversion;

import cn.itcast.job.cache.ConfigConstant;
import cn.itcast.job.pojo.VersionsGradleLineBean;
import cn.itcast.job.utils.FileUtil;
import cn.itcast.job.utils.StringUtil;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

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

//import cn.itcast.job.pojo.WanAndroidLibVersion;

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


                        Request request2 = new Request(
//                                "https://wanandroid.com/maven_pom/index?k=com.google.android.gms:play-services-analytics:");
                                "https://repo.maven.apache.org/maven2/"
                                        + versionsGradleLineBean.getGroupName().replace(".", "/")
                                        + "/"
                                        + versionsGradleLineBean.getLibraryName().replace(".", "/"));
                        request2.putExtra("VersionsGradleLineBean", versionsGradleLineBean);
                        request2.putExtra("groupName", versionsGradleLineBean.getGroupName());
                        request2.putExtra("libraryName", versionsGradleLineBean.getLibraryName());
                        requestList.add(request2);

                        Request request = new Request(
//                                "https://wanandroid.com/maven_pom/index?k=com.google.android.gms:play-services-analytics:");
                                "https://wanandroid.com/maven_pom/index?k="
                                        + versionsGradleLineBean.getGroupName()
                                        + ":"
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
        boolean result = page.getStatusCode() != 200 || page.getRawText().contains("Not Found: /artifact/")
                || page.getRawText().contains("The requested path was not found.")
                || page.getRawText().contains("404 Not Found");

        System.out.println(page.getUrl().toString() + "结果 " + !result);
        if (result) {

        } else {
            String url = page.getUrl().toString();
            if (url.startsWith("https://wanandroid.com/maven_pom/index?k=")) {
                processWanandroid(page, url);
            }
            if (url.startsWith("https://repo.maven.apache.org/maven2/")) {
                System.out.println("process maven apache " + url + " ");
                processMavenApache(page, url);
            }
        }
    }

    private void processWanandroid(Page page, String url) {
        String version = "";
        //<td>implementation 'com.google.android.gms:play-services-analytics-impl:9.0.0'</td>
        try {
            List<String> items = page.getHtml().css("td").all();

            if (items != null && items.size() >= 1) {
                VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");

                for (int i = 0; i < items.size(); i++) {
                    String line = items.get(i);
                    if (line == null || line.isEmpty()) {
                        continue;
                    }
                    String groupAndLibName = versionsGradleLineBean.getGroupName() + ":" + versionsGradleLineBean.getLibraryName() + ":";
                    if (line.contains(groupAndLibName)) {
                        int begin = line.indexOf(groupAndLibName) + groupAndLibName.length();
                        int end = line.indexOf("'", begin);
                        String tempVersion = line.substring(begin, end);
                        if (!tempVersion.contains("alpha") && !tempVersion.contains("beta") && !tempVersion.contains("rc")) {
                            version = tempVersion;
                        }
                    }
                }
            }
            System.out.println("processWanandroid " + url + " " + version);
            if (version != null && !version.isEmpty()) {
                VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
                versionsGradleLineBean.setLastVersion(version, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMavenApache(Page page, String url) {
        String version = "";
        //<td>implementation 'com.google.android.gms:play-services-analytics-impl:9.0.0'</td>
        try {
            List<String> items = page.getHtml().css("a", "title").all();

            if (items != null && items.size() >= 1) {
                VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");

                for (int i = items.size() - 1; i >= 0; i--) {
                    String line = items.get(i);
                    if (line == null || line.isEmpty()) {
                        continue;
                    }
                    if (!line.startsWith("maven-metadata")) {
                        version = line;
                        break;
                    }
//                    String groupAndLibName = versionsGradleLineBean.getGroupName() + ":" + versionsGradleLineBean.getLibraryName() + ":";
//                    if (line.contains(groupAndLibName)) {
//                        int begin = line.indexOf(groupAndLibName) + groupAndLibName.length();
//                        int end = line.indexOf("'", begin);
//                        String tempVersion = line.substring(begin, end);
//                        if (!tempVersion.contains("alpha") && !tempVersion.contains("beta") && !tempVersion.contains("rc")) {
//                            version = tempVersion;
//                        }
//                    }
                }
            }
            System.out.println("processMavenApache " + url + " " + version);
            if (version != null && !version.isEmpty()) {
                VersionsGradleLineBean versionsGradleLineBean = (VersionsGradleLineBean) page.getRequest().getExtra("VersionsGradleLineBean");
                versionsGradleLineBean.setLastVersion(version, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
