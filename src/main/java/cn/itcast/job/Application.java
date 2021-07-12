package cn.itcast.job;

import cn.itcast.job.task.checksmilarcode.CheckSmilarCode;
import cn.itcast.job.task.unzip.CheckSmilarAppCode;

public class Application {

    public static void main(String[] args) {
//        CheckVersionTask checkVersionTask = new CheckVersionTask();
//        if(false){
//            ConfigConstant.DEPENDICES_PATH = "/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/checkversionutils/dependices.txt";
//            DependicesNode root = DependicesFileReadAndGetTree.dependicesFileReadAndGetTree();
//            System.out.println("end");
//            return;
//        }
//        if (false) {
//            try {
//                checkVersionTask.process("/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/checkversionutils/versions.gradle",
//                        "/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/checkversionutils/dependices.txt",
//                        "/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/checkversionutils/report.html");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return;
//        }
        if (true) {
            CheckSmilarCode.process(
                    "/Users/admin/StudioProjects/AndroidScreenRecorderEasy/",
                    "/Users/admin/StudioProjects/AndroidScreenRecorder/",
                    "/Users/admin/Documents/02-网络爬虫/workspace/new_version_check2/doc/simhash/result.txt");
            return;
        }
        if (args == null || args.length == 0) {
            System.out.println("Please use like : ");
            System.out.println("versions-gradle-checker-1.0.jar checkcode /user/admin/project1 /user/admin/project2 /Users/admin/Documents/result.txt");
            System.out.println("or");
            System.out.println("versions-gradle-checker-1.0.jar checkapp /user/admin/project1 /user/admin/project2 /Users/admin/Documents/result.txt");
            return;
        }

        try {
            if (args.length == 4) {
                if ("checkcode".equals(args[0])) {
                    CheckSmilarCode.process(args[1], args[2], args[3]);
                } else if ("checkapp".equals(args[0])) {
                    CheckSmilarAppCode.process(args[1], args[2], args[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
