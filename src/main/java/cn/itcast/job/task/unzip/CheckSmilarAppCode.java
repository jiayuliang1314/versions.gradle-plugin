package cn.itcast.job.task.unzip;

import cn.itcast.job.utils.CollectionsUtils;
import cn.itcast.job.utils.FileUtils;
import cn.itcast.job.utils.Log;
import com.lou.simhasher.SimHasher;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

//import org.apache.commons.io.FileUtils;

public class CheckSmilarAppCode {
    public static String pathNewApp = "";
    public static String pathOldApp = "";
    public static String pathNew = "";
    public static String pathOld = "";
    public static Map<String, SimHasher> pathHashMapOld = new HashMap<>();
    public static Map<String, SimHasher> pathHashMapNew = new HashMap<>();
    public static List<NewOldLengthBean> list = new ArrayList<>();
    public static int minLength;
    public static String minPathOld;
    public static String result;

    /**
     * 测试用
     *
     * @param filename 名字
     * @return
     */
    public static String readAllFile(String filename) {
        String everything = "";
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            everything = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
        }

        return everything;
    }

    public static void process(String... args) {
        if (args.length < 3) {
//            System.out.println("please use : ");
//            System.out.println("finder.jar /user/admin/project1 /user/admin/project2 /user/admin/result.text");
            return;
        }
        pathNewApp = args[0];
        pathOldApp = args[1];

        pathNew = pathNewApp + "/tmp";
        pathOld = pathOldApp + "/tmp";

        new MyUnzipTask(new File(pathNewApp), new File(pathNew)).call();
        new MyUnzipTask(new File(pathOldApp), new File(pathOld)).call();

        result = args[2];
        File resultFile = new File(result);
        if (resultFile.exists()) {
            resultFile.delete();
        }
        try {
            resultFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileUtils.cyclicTraversalPath(pathNew, new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) throws Exception {
//                if (!src_dir.getAbsolutePath().contains("build/")) {
                String str1 = readAllFile(src_dir.getAbsolutePath());
                SimHasher hash1 = new SimHasher(str1);
                pathHashMapNew.put(src_dir.getAbsolutePath(), hash1);
//                }
            }
        });
        FileUtils.cyclicTraversalPath(pathOld, new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) throws Exception {
//                if (!src_dir.getAbsolutePath().contains("build/")) {
                String str1 = readAllFile(src_dir.getAbsolutePath());
                SimHasher hash1 = new SimHasher(str1);
                pathHashMapOld.put(src_dir.getAbsolutePath(), hash1);
//                }
            }
        });
        CollectionsUtils.mapTraversal(pathHashMapNew, new CollectionsUtils.MapTraversalCallback<String, SimHasher>() {
            @Override
            public void visit(Map.Entry<String, SimHasher> entryNew) {
                minLength = Integer.MAX_VALUE;
                String pathNew = entryNew.getKey();
                minPathOld = "";
                CollectionsUtils.mapTraversal(pathHashMapOld, new CollectionsUtils.MapTraversalCallback<String, SimHasher>() {
                    @Override
                    public void visit(Map.Entry<String, SimHasher> entryOld) {
                        String pathOld = entryOld.getKey();
                        SimHasher valueNew = entryNew.getValue();
                        SimHasher valueOld = entryOld.getValue();
                        int length = valueNew.getHammingDistance(valueOld.getSignature());
                        if (length < minLength) {
                            minLength = length;
                            minPathOld = pathOld;
                        }
                    }
                });
                NewOldLengthBean newOldLengthBean = new NewOldLengthBean(pathNew, minPathOld, minLength);
                list.add(newOldLengthBean);
            }
        });
        Collections.sort(list, new Comparator<NewOldLengthBean>() {
            @Override
            public int compare(NewOldLengthBean o1, NewOldLengthBean o2) {
                return o1.length - o2.length;
            }
        });
        Log.i("================");
        Log.i("================");
        Log.i("================");
        int i = 0;
//        StringBuilder stringBuilder = new StringBuilder();
        for (NewOldLengthBean newOldLengthBean : list) {
            Log.i(i + " " + newOldLengthBean.toString());
//            stringBuilder.append(newOldLengthBean.toString()).append("/n");
            FileUtils.writeData(result, i + " " + newOldLengthBean.toString());
            i++;
        }

    }

    public static class NewOldLengthBean {
        public String pathNew;
        public String pathOld;
        public int length;

        public NewOldLengthBean(String pathNew, String pathOld, int length) {
            this.pathNew = pathNew;
            this.pathOld = pathOld;
            this.length = length;
        }

        @Override
        public String toString() {
            return "" +
                    " New='" + pathNew + '\'' + "\n" +
                    ", Old='" + pathOld + '\'' + "\n" +
                    ", length=" + length + "\n" +
                    "\n";
        }
    }
}
