package com.next.module.transfer.server.tool;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;

import com.next.module.transfer.server.TransferServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ClassName:文件工具
 *
 * @author Afton
 * @time 2023/8/20
 * @auditor
 */
public class FileTool {

    //创建结果
    public static class CreatResult {
        //创建成功
        public static final int FLAG_SUCCESS = 1;
        //已存在
        public static final int FLAG_EXISTS = 2;
        //创建失败
        public static final int FLAG_FAILED = 3;
    }

    /**
     * 创建单个文件
     *
     * @param filePath 待创建的文件路径
     * @return 创建结果
     */
    public static int CreateFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return CreatResult.FLAG_EXISTS;
        }
        if (filePath.endsWith(File.separator)) {
            // 以 路径分隔符 结束，说明是文件夹
            return CreatResult.FLAG_FAILED;
        }

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                return CreatResult.FLAG_FAILED;
            }
        }

        try {
            if (file.createNewFile()) {
                //创建文件成功
                updateMedia(filePath);
                return CreatResult.FLAG_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return CreatResult.FLAG_FAILED;
        }

        return CreatResult.FLAG_FAILED;
    }

    /**
     * 创建文件夹
     *
     * @param folderPath 文件夹路径
     * @return 创建结果
     */
    public static int createDir(String folderPath) {
        File dir = new File(folderPath);
        //文件夹是否已经存在
        if (dir.exists()) {
            return CreatResult.FLAG_EXISTS;
        }
        if (!folderPath.endsWith(File.separator)) {
            //不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            folderPath = folderPath + File.separator;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            updateMedia(folderPath);
            return CreatResult.FLAG_SUCCESS;
        }
        return CreatResult.FLAG_FAILED;
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return true/false
     */
    public static boolean delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) return deleteSingleFile(path);
            else return deleteDirectory(path);
        }
    }

    /**
     * 删除单个文件
     *
     * @param path 文件路径
     * @return true/false
     */
    private static boolean deleteSingleFile(String path) {
        File file = new File(path);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                updateMedia(path);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 删除文件夹
     *
     * @param filePath 文件路径
     * @return true/false
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag) break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) {

            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            updateMedia(filePath);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath$Name 原文件路径
     * @param newPath$Name 目标路径
     * @return true/false
     */
    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);

            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }

            String newFile = newPath$Name;
            File file = new File(newFile);
            String fileName = file.getName();

            int i = 1;
            int index = file.getName().lastIndexOf(".");
            while (file.exists()) {
                i++;
                if (index < 0) {
                    file = new File(newFile + "(" + i + ")");
                } else {
                    file = new File(file.getParent() + File.separator + fileName.substring(0, index) + "(" + i + ")" + fileName.substring(index));
                }
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(file.getPath());
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            updateMedia(newPath$Name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件夹
     *
     * @param oldPath 原文件夹路径
     * @param newPath 目标路径
     * @return true/false
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            updateMedia(newPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 刷新媒体文件
     *
     * @param path 文件路径
     */
    public static void updateMedia(String path) {
        path = new File(path).getParentFile().getPath();
        Context context = TransferServer.getInstance().getContext();

        MediaScannerConnection.scanFile(context, new String[]{path}, null, (path1, uri) -> {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
        });
    }
}