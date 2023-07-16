package cn.itcast.job;

import cn.itcast.job.cache.ConfigConstant;
import cn.itcast.job.pojo.dependices.DependicesNode;
import cn.itcast.job.task.checkversion.CheckVersionTask;
import cn.itcast.job.task.checkversion.DependicesFileReadAndGetTree;

public class Application {

    public static void main(String[] args) {
        CheckVersionTask checkVersionTask = new CheckVersionTask();
        if(false){
            ConfigConstant.DEPENDICES_PATH = "/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/checkversionutils/dependices.txt";
            DependicesNode root = DependicesFileReadAndGetTree.dependicesFileReadAndGetTree();
            System.out.println("end");
            return;
        }
        if (false) {
            try {
                checkVersionTask.process("/Volumes/T7 Shield/2023_Android/wavely/versions.gradle",
                        "/Volumes/T7 Shield/2023_Android/wavely/dependices.txt",
                        "/Volumes/T7 Shield/2023_Android/wavely/report.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (args == null || args.length == 0) {
            System.out.println("Please use like \n versions-gradle-checker-3.0.jar /admin/project/demo/version.gradle /admin/project/demo/dependices.txt /admin/project/demo/report.html");
            return;
        }

        try {
            if (args.length == 3) {
                checkVersionTask.process(args[0], args[1], args[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
