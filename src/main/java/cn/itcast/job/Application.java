package cn.itcast.job;

import cn.itcast.job.task.checkversion.CheckVersionTask;

public class Application {

    public static void main(String[] args) {
        CheckVersionTask checkVersionTask = new CheckVersionTask();
        if (false) {
            try {
                checkVersionTask.process("/Users/admin/StudioProjects/wavely2/versions.gradle", "/Users/admin/StudioProjects/wavely2/dependices.txt", "/Users/admin/StudioProjects/wavely2/report.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (args == null || args.length == 0) {
            System.out.println("Please use like \n versions-gradle-checker-1.0.jar /admin/project/demo/version.gradle /admin/project/demo/dependices.txt /admin/project/demo/report.html");
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
