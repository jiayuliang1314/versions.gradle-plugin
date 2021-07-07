package cn.itcast.job.utils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtils {
    public static int time = 0;

    public static void writeData(String file_path, String ip, String code_version) {
        File file = new File(file_path);
        ip = ip + "\r\n";
        try {
            OutputStreamWriter osw = new OutputStreamWriter
                    (new FileOutputStream(file, true), code_version);
            osw.write(ip);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeData(String file_path, String ip) {
        File file = new File(file_path);
        ip = ip + "\r\n";
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
            osw.write(ip);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void renameJpgFilesAddFhdInOneFolder(String path) {
        FileUtils.cyclicTraversalPath(new File(path), new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) {
                FileUtils.renameJpgFilesAddFhd(src_dir);
            }
        });
        Log.i("renameMp4FilesAddFhdInOneFolder done " + path);
    }

    public static void renameJpgFilesAddFhd(File file) {
        if (file.getName().contains(".jpg") && !file.getName().endsWith("_fhd.jpg")) {
            file.renameTo(new File(file.getParentFile().getAbsolutePath() + "\\"
                    + file.getName().replace(".jpg", "") + "_fhd.jpg"));
        }
        if (file.getName().contains(".JPG") && !file.getName().endsWith("_fhd.jpg")) {
            file.renameTo(new File(file.getParentFile().getAbsolutePath() + "\\"
                    + file.getName().replace(".JPG", "") + "_fhd.jpg"));
        }
    }

    public static void renameMp4FilesAddFhdInOneFolder(String path) {
        FileUtils.cyclicTraversalPath(new File(path), new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) {
                FileUtils.renameMp4FilesAddFhd(src_dir);
            }
        });
        Log.i("renameMp4FilesAddFhdInOneFolder done " + path);
    }

    public static void renameMp4FilesAddFhd(File file) {
        if (file.getName().contains(".mp4") && !file.getName().endsWith("_fhd.mp4")) {
            file.renameTo(new File(file.getParentFile().getAbsolutePath() + "\\"
                    + file.getName().replace(".mp4", "") + "_fhd.mp4"));
        }
    }

    public static void copyJpgFilesAddMiniInOneFolder(String path) {
        FileUtils.cyclicTraversalPath(new File(path), new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) {
                FileUtils.copyJpgFilesAddMini(src_dir);
            }
        });
        Log.i("copyJpgFilesAddMiniInOneFolder done " + path);
    }

    public static void copyJpgFilesAddMini(File file) {
        if (file.getName().contains(".jpg") && !file.getName().startsWith("mini_")) {
            copyFile(file.getAbsolutePath(), file.getParentFile().getAbsolutePath() + "\\"
                    + "mini_" + file.getName());
        }
    }

    public static void copyJpgToGifFilesInOneFolder(String path) {
        FileUtils.cyclicTraversalPath(new File(path), new FileUtils.ControlFileCallback() {
            @Override
            public void control(File src_dir) {
                FileUtils.copyJpgToGifFile(src_dir);
            }
        });
        Log.i("copyJpgToGifFilesInOneFolder done " + path);
    }

    public static void copyJpgToGifFile(File file) {
        if (file.getName().contains(".jpg") && !file.getName().startsWith("mini_")) {
            copyFile(file.getAbsolutePath(), file.getParentFile().getAbsolutePath() + "\\"
                    + file.getName().replace(".jpg", ".gif"));
        }
    }



    /*
     * *
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     *
     * @param newPath String 复制后路径 如：f:/fqf.txt
     *
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            Log.i("copyFile oldPath " + oldPath);
            Log.i("copyFile newPath " + newPath);
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newFolder = (new File(newPath)).getParentFile();
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    // System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
                return true;
            } else {
                Log.i("copyFile oldPath is not exists");
            }
        } catch (Exception e) {
            // System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 循环遍历一个folder，对其每个文件进行操作
     *
     * @param src_dir  folder
     * @param callback 具体要干哈，例如改名或者移动位置
     */
    public static void cyclicTraversalPath(File src_dir, ControlFileCallback callback) {
        if (src_dir.isDirectory()) {
//            Log.i("Folder " + src_dir.getName());
            File[] src_files = src_dir.listFiles();
            for (int i = 0; i < src_files.length; i++) {
                cyclicTraversalPath(src_files[i], callback);
            }
        } else {
            try {
                callback.control(src_dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void cyclicFolderTraversalPath(File src_dir, ControlFileCallback callback) throws Exception {
        if (src_dir.isDirectory()) {
//            Log.i("Folder " + src_dir.getName());
            File[] src_files = src_dir.listFiles();
            for (int i = 0; i < src_files.length; i++) {
//                cyclicTraversalPath(src_files[i], callback);
                if (src_files[i].isDirectory()) {
                    callback.control(src_files[i]);
                }
            }
        } else {
//            try {
//                callback.control(src_dir);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    public static void cyclicFolderTraversalPath(String src_dir, ControlFileCallback callback) throws Exception {
        cyclicFolderTraversalPath(new File(src_dir), callback);
    }

    public static void cyclicTraversalPath(String src_dir, ControlFileCallback callback) {
        cyclicTraversalPath(new File(src_dir), callback);
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()));
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteAllFilesOfDir(File path) {
        if (!path.exists())
            return;
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFilesOfDir(files[i]);
        }
        path.delete();
    }

    public static int getFileSize(String source) {
        try {
            FileInputStream fis = new FileInputStream(source);

            FileChannel fc = fis.getChannel();

            BigDecimal fileSize = new BigDecimal(fc.size());

            BigDecimal size = fileSize.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP);

            return size.intValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getFileSizeInKb(String source) {
        try {
            FileInputStream fis = new FileInputStream(source);

            FileChannel fc = fis.getChannel();

            BigDecimal fileSize = new BigDecimal(fc.size());

            BigDecimal size = fileSize.divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP);

            return size.intValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void modify(String file, String beforeContains, String afterLine) {
        String newProjectString = file;//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xml";
        String newProjectStringtmp = file + "tmp";//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xmltmp";
        File tmp = new File(newProjectStringtmp);
        File now = new File(newProjectString);
        if (tmp.exists()) {
            tmp.delete();
        }
        FileUtils.readFileEveryLine(newProjectString, new FileUtils.ControlFileEveryLineCallback() {

            @Override
            public void control(String line) throws IOException {
                Log.i(line);
                if (line.contains(beforeContains)) {
                    FileUtils.writeData(newProjectStringtmp, afterLine);
                } else {
                    FileUtils.writeData(newProjectStringtmp, line);
                }
            }
        });
        now.delete();
        tmp.renameTo(new File(newProjectString));
    }

    public static void modifyLineBeginWith(String file, String beforeBeginWith, String afterLine) {
        String newProjectString = file;//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xml";
        String newProjectStringtmp = file + "tmp";//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xmltmp";
        File tmp = new File(newProjectStringtmp);
        File now = new File(newProjectString);
        if (tmp.exists()) {
            tmp.delete();
        }
        FileUtils.readFileEveryLine(newProjectString, new FileUtils.ControlFileEveryLineCallback() {

            @Override
            public void control(String line) throws IOException {
                Log.i(line);
                if (line.startsWith(beforeBeginWith)) {
                    FileUtils.writeData(newProjectStringtmp, afterLine);
                } else {
                    FileUtils.writeData(newProjectStringtmp, line);
                }
            }
        });
        now.delete();
        tmp.renameTo(new File(newProjectString));
    }

    public static void modifyLineBeginWithAndTime(String file, ModifyLineBeginWithAndTimeListener lineBeginWithAndTimeListener) {
        time = 0;
        String newProjectString = file;//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xml";
        String newProjectStringtmp = file + "tmp";//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xmltmp";
        File tmp = new File(newProjectStringtmp);
        File now = new File(newProjectString);
        if (tmp.exists()) {
            tmp.delete();
        }
        FileUtils.readFileEveryLine(newProjectString, new FileUtils.ControlFileEveryLineCallback() {

            @Override
            public void control(String line) throws IOException {
//                Log.i(line);
                if (lineBeginWithAndTimeListener.startWithOrOther(line)) {
                    FileUtils.writeData(newProjectStringtmp, lineBeginWithAndTimeListener.afterLine(line, time));
                    time++;
                } else {
                    FileUtils.writeData(newProjectStringtmp, line);
                }
            }
        });
        now.delete();
        tmp.renameTo(new File(newProjectString));
    }

    public static void modifyReplace(String file, String beforeContains, String afterLine) {
        String newProjectString = file;//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xml";
        String newProjectStringtmp = file + "tmp";//"/Users/admin/StudioProjects/" + packageNameRemoveDot + "/app/src/main/res/values/strings.xmltmp";
        File tmp = new File(newProjectStringtmp);
        File now = new File(newProjectString);
        if (tmp.exists()) {
            tmp.delete();
        }
        FileUtils.readFileEveryLine(newProjectString, new FileUtils.ControlFileEveryLineCallback() {

            @Override
            public void control(String line) throws IOException {
                Log.i(line);
                if (line.contains(beforeContains)) {
                    FileUtils.writeData(newProjectStringtmp, line.replace(beforeContains, afterLine));
                } else {
                    FileUtils.writeData(newProjectStringtmp, line);
                }
            }
        });
        now.delete();
        tmp.renameTo(new File(newProjectString));
    }

    public static void readFileEveryLine(String file, ControlFileEveryLineCallback controlFileCallback) {
        File manifest_file = new File(file);
        String code_version = ReadTextCode.getJavaEncode(manifest_file.getAbsolutePath());
        BufferedReader reader_src = null;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(manifest_file.getAbsolutePath())), code_version);
            reader_src = new BufferedReader(isr);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line_value;
        try {
            while ((line_value = reader_src.readLine()) != null) {
                if (controlFileCallback != null) {
                    controlFileCallback.control(line_value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader_src.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFileEveryNumLine(String file, int num, ControlFileEveryNumLineCallback controlFileCallback) {
        File manifest_file = new File(file);
        String code_version = ReadTextCode.getJavaEncode(manifest_file.getAbsolutePath());
        BufferedReader reader_src = null;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(manifest_file.getAbsolutePath())), code_version);
            reader_src = new BufferedReader(isr);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line_value;
        int i = 0;
        List<String> stringList = new ArrayList<>();
        try {
            while ((line_value = reader_src.readLine()) != null) {
                stringList.add(line_value);
                i++;
                if (i % 6 == 0) {
                    if (controlFileCallback != null) {
                        controlFileCallback.control(stringList);
                        stringList.clear();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader_src.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isExits(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void changeAllStringAtOneFileBeginWithEndWith(String filePath, String begin, String end, String after) throws IOException {
        Log.i(filePath);
        Log.i(begin);
        Log.i(end);
        Log.i(after);
        File file_tmp = new File(filePath + "tmp");
        if (!file_tmp.exists()) {
            file_tmp.createNewFile();
        } else {
            file_tmp.delete();
            file_tmp.createNewFile();
        }
        BufferedReader reader_label;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(filePath)), StandardCharsets.UTF_8);
            reader_label = new BufferedReader(isr);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return;
        }
        String line_value;
        while ((line_value = reader_label.readLine()) != null) {
            if (line_value.contains(begin) && line_value.contains(end)) {
                writeData(file_tmp.getAbsolutePath(), begin + after + end);
            } else {
                writeData(file_tmp.getAbsolutePath(), line_value);
            }
        }
        reader_label.close();
        String name = filePath;
        if (new File(name).delete()) {
            System.out.println("delete success");
        } else {
            System.out.println("delete fail");
        }
        if (file_tmp.renameTo(new File(name))) {
            System.out.println("renameTo success");
        } else {
            System.out.println("renameTo fail");
        }
    }

    public static void createFolder(String s) {
        File fileDir = new File(s);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
    }

    public static boolean isTodayModifed(String file) {
        return isTodayModifed(new File(file));
    }

    public static boolean isTodayModifed(File file) {
        return (new SimpleDateFormat("yyyy-MM-dd").format(new Date())).
                equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date(file.lastModified())));
    }

    public static void renameFile(File src_dir, String newName) {
        src_dir.renameTo(new File(src_dir.getParentFile().getAbsolutePath() + "/" + newName));
    }

    public interface ModifyLineBeginWithAndTimeListener {
        boolean startWithOrOther(String line);

        String afterLine(String line, int time) throws UnsupportedEncodingException;
    }

    public interface ControlFileEveryLineCallback {
        void control(String line) throws IOException;
    }

    public interface ControlFileEveryNumLineCallback {
        void control(List<String> line);
    }

    public interface ControlFileCallback {
        void control(File src_dir) throws Exception;
    }
}
