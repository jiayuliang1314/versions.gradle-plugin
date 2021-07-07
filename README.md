
## Simhash Google出品，是一种局部敏感Hash。利用汉明距离去衡量simhash的相似度。汉明距离小于3说明相似度高。

## 原理： [通过全文相似度来寻找相同或相似的代码][http://blog.startry.com/2016/12/14/find-same-code-by-simhash-and-hamming-distance/]

## 使用方法

1.本项目doc/simhash文件夹下找到versions-gradle-checker-1.0.jar

2.Mac电脑使用命令行：

//versions-gradle-checker-1.0.jar 项目1路径 项目2路径 结果位置

versions-gradle-checker-1.0.jar /user/admin/project1 /user/admin/project2 /Users/admin/Documents/result.txt

3.结果：

例如如下结果，文件内容相似的被扫出来了，可以针对行修改

0 {New='/Users/admin/StudioProjects/demo1/MainModule/src/test/java/com/example/mainmodule/ExampleUnitTest.java',
   Old='/Users/admin/StudioProjects/demo2/MainModule/src/test/java/com/example/mainmodule/ExampleUnitTest.java', 
   length=0}
   
1 {New='/Users/admin/StudioProjects/demo1/MainModule/src/main/java/demo1/re/AudioEncodeConfig.java', 
   Old='/Users/admin/StudioProjects/demo2/MainModule/src/main/java/com/o00/oO0/ooo/re/AudioEncodeConfig.java',
   length=0}
   
2 {New='/Users/admin/StudioProjects/demo1/MainModule/src/main/java/demo1/de/ahy.java',
   Old='/Users/admin/StudioProjects/demo2/MainModule/src/main/java/com/o0O/oO0/O0o/OOO/de/ahy.java', 
   length=0}
   
3 {New='/Users/admin/StudioProjects/demo1/MainModule/src/main/java/demo1/re/BaseEncoder.java', 
   Old='/Users/admin/StudioProjects/demo2/MainModule/src/main/java/com/o00/oO0/ooo/re/BaseEncoder.java', 
   length=0}
   
   length小于等于3，说明文件很相似


[参考]: http://blog.startry.com/2016/12/14/find-same-code-by-simhash-and-hamming-distance/
