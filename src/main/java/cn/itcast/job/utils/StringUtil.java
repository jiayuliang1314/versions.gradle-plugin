package cn.itcast.job.utils;

public class StringUtil {

    //region 查找字符串里与指定字符串相同的个数
    public static int search(String str, String strRes) {//查找字符串里与指定字符串相同的个数
        int n = 0;//计数器
        while (str.indexOf(strRes) != -1) {
            int i = str.indexOf(strRes);
            n++;
            str = str.substring(i + 1);
        }
        return n;
    }
    //endregion
}
