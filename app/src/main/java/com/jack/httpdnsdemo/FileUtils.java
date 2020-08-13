package com.jack.httpdnsdemo;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 关于File的操作类
 *
 * @author jack
 * @since 2020/3/13 14:58
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 读取磁盘路径下的文本
     *
     * @param filePathName 文件全路径
     */
    @Nullable
    public static String read(@NonNull String filePathName) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            File file = new File(filePathName);
            //不存在直接返回
            if (!file.exists()) {
                return null;
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            StringBuilder content = new StringBuilder();
            //自己定义一个缓冲区
            byte[] buffer = new byte[10240];
            int flag;
            while ((flag = bis.read(buffer)) != -1) {
                content.append(new String(buffer, 0, flag));
            }
            return content.toString();
        } catch (IOException e) {
            Log.d(TAG, "读取:" + filePathName + "失败！" + e.getMessage());
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "读取:" + filePathName + "失败！" + e.getMessage());
            //删除该文件
            delete(filePathName);
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 把文本写入磁盘文件
     *
     * @param filePathName 文件全路径
     * @param content      追加的内容
     */
    public static void writer(@NonNull String filePathName, @NonNull String content) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            File file = new File(filePathName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (!mkdirs) {
                    Log.d(TAG, "写入:" + filePathName + "失败！创建该路径失败！");
                    return;
                }
            }
            fos = new FileOutputStream(filePathName);
            bos = new BufferedOutputStream(fos);
            bos.write(content.getBytes());
            bos.flush();
        } catch (IOException e) {
            Log.d(TAG, "写入:" + filePathName + "失败！" + e.getMessage());
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 追加的方式在文件的末尾添加内容
     *
     * @param filePathName 文件全路径
     * @param content      追加的内容
     */
    public static void append(@NonNull String filePathName, @NonNull String content) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(filePathName, true);
            bos = new BufferedOutputStream(fos);
            bos.write(content.getBytes());
            bos.flush();
        } catch (IOException e) {
            Log.d(TAG, "写入:" + filePathName + "失败！" + e.getMessage());
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param delFile 要删除的文件夹或文件名
     */
    public static boolean delete(@NonNull String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
            Log.d(TAG, "删除文件失败:" + delFile + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePathName 要删除的文件的文件名
     */
    private static boolean deleteSingleFile(String filePathName) {
        File file = new File(filePathName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.d(TAG, "删除单个文件:" + filePathName + "成功！");
                return true;
            } else {
                Log.d(TAG, "删除单个文件:" + filePathName + "失败！");
                return false;
            }
        } else {
            Log.d(TAG, "删除单个文件失败:" + filePathName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param filePath 要删除的目录的文件路径
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.d(TAG, "删除目录失败:" + filePath + "不存在！");
            return false;
        }
        boolean flag = true;
        try {
            // 删除文件夹中的所有文件包括子目录
            File[] files = dirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 删除子文件
                    if (file.isFile()) {
                        //失败一次则就为整体失败
                        if (!deleteSingleFile(file.getAbsolutePath())) {
                            flag = false;
                        }
                    }
                    // 删除子目录
                    else if (file.isDirectory()) {
                        //失败一次则就为整体失败
                        if (!deleteDirectory(file.getAbsolutePath())) {
                            flag = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "删除失败！" + e.getMessage());
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "删除失败！" + e.getMessage());
        }
        if (!flag) {
            Log.d(TAG, "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.d(TAG, "删除目录:" + filePath + "成功！");
            return true;
        } else {
            Log.d(TAG, "删除目录:" + filePath + "失败！");
            return false;
        }
    }

    /**
     * 获取文件夹内所有文件大小的和
     *
     * @param filePath 文件夹路径
     */
    public static long getFolderSize(String filePath) {
        File file = new File(filePath);
        long size = 0;
        try {
            if (file.exists()) {
                if (file.isFile()) {
                    size = size + file.length();
                } else if (file.isDirectory()) {
                    File[] fileList = file.listFiles();
                    if (fileList != null) {
                        for (File aFileList : fileList) {
                            if (aFileList.isDirectory()) {
                                size = size + getFolderSize(aFileList.getAbsolutePath());
                            } else if (aFileList.isFile()) {
                                size = size + aFileList.length();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "计算失败！" + e.getMessage());
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "计算失败！" + e.getMessage());
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size       单位：byte
     * @param decimalNum 小数点位数
     */
    public static String getFormatSize(double size, int decimalNum) {
        double kiloByte = size / 1024;
//        if (kiloByte < 1) {
//            return size + "Byte";
//        }
        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            return getFormatString(decimalNum, kiloByte) + "KB";
        }
        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            return getFormatString(decimalNum, megaByte) + "MB";
        }
        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            return getFormatString(decimalNum, gigaByte) + "GB";
        }
        return getFormatString(decimalNum, teraBytes) + "TB";
    }

    /**
     * 单位转换
     */
    private static String getFormatString(int decimalNum, double number) {
        if (Math.round(number) - number == 0) {
            return String.valueOf((int) number);
        } else {
            return String.format("%." + decimalNum + "f", number);
        }
    }

}
