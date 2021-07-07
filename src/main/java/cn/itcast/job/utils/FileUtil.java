package cn.itcast.job.utils;

//import lombok.extern.slf4j.Slf4j;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

import java.io.*;
import java.nio.charset.StandardCharsets;

//ok
//@Slf4j
public class FileUtil {
    /**
     * 删除文件夹和文件夹里面的文件
     *
     * @param path
     */
    public static void deleteDir(final String path) {
        File dir = new File(path);
        deleteDirWithFile(dir, true);
    }

    public static void deleteFilesInDir(final String path) {
        File dir = new File(path);
        deleteDirWithFile(dir, false);
    }

    public static void deleteDirWithFile(File dir, boolean deleteDir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWithFile(file, true); // 递规的方式删除文件夹
            }
        }

        if (deleteDir) {
            dir.delete();// 删除目录本身
        }
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String content, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = content + "\r\n";
        try {
            File file = new File(strFilePath);
            if (file.exists()) {
                file.delete();
            }

            //log.debug("Create the file:" + strFilePath);
            file.getParentFile().mkdirs();
            file.createNewFile();

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            //log.error("Error on write File:" + e);
        }
    }

    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            //log.error("exception", e);
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            //log.error("error:", e);
        }
    }

    // 创建保存录音的目录
    public static void createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    // 删除文件
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    // 删除文件
    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * write file, the string will be written to the begin of the file
     *
     * @param filePath 文件路径
     * @param content  内容
     * @return 执行结果
     */
    public static boolean writeFile(String filePath, String content) {
        return writeFile(filePath, content, false);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param filePath 文件路径
     * @param stream   InputStream
     * @return 执行结果
     * @see {@link #writeFile(String, InputStream, boolean)}
     */
    public static boolean writeFile(String filePath, InputStream stream) {
        return writeFile(filePath, stream, false);
    }

    /**
     * write file
     *
     * @param filePath the file to be opened for writing.
     * @param stream   the input stream
     * @param append   if <code>true</code>, then bytes will be written to the end of
     *                 the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(String filePath, InputStream stream,
                                    boolean append) {
        return writeFile(filePath != null ? new File(filePath) : null, stream,
                append);
    }

    /**
     * write file
     *
     * @param filePath 文件路径
     * @param content  内容
     * @param append   is append, if true, write to the end of file, else clear
     *                 content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content,
                                    boolean append) {
        if (Checker.isEmpty(content)) {
            return false;
        }

        File file1 = new File(filePath);
        Writer fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file1), StandardCharsets.UTF_8));//new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (IOException e) {
            //log.error("exception", e);
            return false;
            //throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    //log.error("exception", e);
                    //throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     *
     * @param file   the file to be opened for writing.
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of
     *               the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(File file, InputStream stream,
                                    boolean append) {
        OutputStream o = null;
        try {
            makeDirs(file.getAbsolutePath());
            o = new FileOutputStream(file, append);
            byte[] data = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (o != null) {
                try {
                    o.close();
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (Checker.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    public static String getFolderName(String filePath) {
        if (Checker.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    public static synchronized boolean copyFile(String oldPath, String newPath, String fileName) {
        return copyFile(oldPath + "/" + fileName, newPath + "/" + fileName);
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static synchronized boolean copyFile(String oldPath, String newPath) {
        boolean success = false;
        try {
            File newFile = new File(newPath);

            if (!newFile.exists()) {
                newFile.getParentFile().mkdirs();
                newFile.createNewFile();
            } else {
                newFile.delete();// 删除已存在的文件
            }

            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                Source s = null;
                BufferedSink bufferedSink = null;
                try {
                    s = Okio.source(oldFile);
                    bufferedSink = Okio.buffer(Okio.sink(new File(newPath)));
                    bufferedSink.writeAll(s);
                    bufferedSink.flush();
                    success = true;
                } catch (IOException e) {
                    throw e;
                } finally {
                    closeCloseable(s);
                    closeCloseable(bufferedSink);
                }
            }
        } catch (Exception e) {
            //log.error("exception", e);
        }

        return success;
    }

    /**
     * 复制文件或文件夹
     * 如果目标文件夹不存在则自动创建
     * 如果文件或文件夹已经存在则自动编号-copy n
     *
     * @param src    源文件或文件夹绝对路径
     * @param dstDir 目标文件夹绝对路径
     * @return 是否成功复制文件或文件夹
     */
    public static boolean copyDir(File src, File dstDir) {
        if (!src.exists()) {
            return false;
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }

        if (src.isFile()) { // 文件
            copyFile(src.getAbsolutePath(), dstDir.getAbsolutePath() + "/" + src.getName());
        } else { // 文件夹
            File newSrcDir = new File(dstDir + "/" + src.getName());
            if (newSrcDir.exists()) {
                deleteDir(newSrcDir.getAbsolutePath());
            }
            newSrcDir.mkdirs();
            for (File srcSub : src.listFiles()) {
                copyDir(srcSub, newSrcDir);// 递归复制源文件夹下子文件和文件夹
            }
        }
        return true;
    }

    public static String read(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        String result = "";
        BufferedSource bufferedSource = null;
        try {
            Source source = Okio.source(file);
            bufferedSource = Okio.buffer(source);
            result = bufferedSource.readUtf8();
        } catch (IOException e) {
            //log.error("exception", e);
        } finally {
            closeCloseable(bufferedSource);
        }

        return result;
    }

    public static String read(String path) {
        return read(new File(path));
    }

    public static byte[] readBytes(String path) {
        return readBytes(new File(path));
    }

    public static byte[] readBytes(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        byte[] result = null;
        BufferedSource bufferedSource = null;
        try {
            Source source = Okio.source(file);
            bufferedSource = Okio.buffer(source);
            result = bufferedSource.readByteArray();
        } catch (IOException e) {
            //log.error("exception", e);
        } finally {
            closeCloseable(bufferedSource);
        }

        return result;
    }

    public static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean isFileExist(String path) {
        return new File(path).exists();
    }

    public static boolean replaceStr(String path, String oldStr, String newStr) {
        String content = FileUtil.read(path);
        content = content.replace(oldStr, newStr);
        boolean wrote = FileUtil.writeFile(path, content);
        return wrote;
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

    public static void cyclicTraversalPath(String src_dir, ControlFileCallback callback) {
        cyclicTraversalPath(new File(src_dir), callback);
    }

    public interface ControlFileEveryLineCallback {
        void control(String line) throws IOException;
    }

    public interface ControlFileCallback {
        void control(File src_dir) throws Exception;
    }
}
