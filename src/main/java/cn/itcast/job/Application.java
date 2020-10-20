package cn.itcast.job;

import cn.itcast.job.task.checkversion.CheckVersionTask;

public class Application {

    public static void main(String[] args) {
        CheckVersionTask checkVersionTask = new CheckVersionTask();
        if(false){
            try {
                checkVersionTask.process("/Users/admin/StudioProjects/wavely2/versions.gradle", "/Users/admin/StudioProjects/wavely2/report.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (args == null || args.length == 0) {
            System.out.println("Please use like \n versions-gradle-checker-1.0.jar /admin/project/demo/version.gradle /admin/project/demo/report.html \n or \n versions-gradle-checker-1.0.jar /admin/project/demo/version.gradle");
            return;
        }


        try {
            if (args.length == 2) {
                checkVersionTask.process(args[0], args[1]);
            } else if (args.length == 1) {
                checkVersionTask.process(args[0],
                        args[0].substring(0, args[0].lastIndexOf("/") + 1) + "report.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
