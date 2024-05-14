package com.next.module.transfer.server.service;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.next.module.transfer.server.TransferServer;
import com.next.module.transfer.server.tool.FileTool;
import com.next.module.transfer.server.tool.LogTool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ClassName:接收线程类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class ReceiveThread extends TransferSocket implements Runnable {

    //连接套接字
    private Socket mSocket;

    //网络输入流
    private DataInputStream mDis;

    //网络输出流
    private DataOutputStream mDos;

    //文件保存路径
    private String mPath;

    public ReceiveThread(Socket mSocket, String path, String host) throws IOException {
        super();
        this.mHostName = host;
        this.mSocket = mSocket;
        this.mPath = path;
        this.mThread = new Thread(this);
        init();
    }

    /**
     * 初始化
     *
     * @throws IOException
     */
    private void init() throws IOException {
        //处理流
        this.mDis = new DataInputStream(new BufferedInputStream(this.mSocket.getInputStream()));
        this.mDos = new DataOutputStream(new BufferedOutputStream(this.mSocket.getOutputStream()));

        this.isEnd = false;
        this.isError = false;

        //初始化缓冲区
        this.data = new byte[8192];
        this.len = 0;

        //初始化文件长度
        this.mTransferLenght = 0;
        this.mFileLenght = 0;

        //获取发送端地址
        this.mIp = this.mSocket.getInetAddress().getHostAddress();

        //发送主机名
        this.mDos.writeBytes(this.mHostName);
        this.mDos.flush();

        //获取文件长度
        this.mFileLenght = this.mDis.readLong();

        //获取文件名
        this.len = mDis.read(this.data);
        this.mFileName = new String(this.data, 0, this.len, "GBK");
    }

    @Override
    public void run() {
        File file = null;
        DataOutputStream fos = null;

        try {
            this.mDos.write(1);
            this.mDos.flush();

            //创建文件并打开输出流
            file = this.createFile();
            fos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

            //启动计算接收速度的线程
            new CountRate().start();

            //传输文件内容
            while ((this.len = this.mDis.read(this.data)) != -1) {
                fos.write(this.data, 0, this.len);
                this.mTransferLenght += this.len;
            }
            fos.flush();

            //检查权限
            if (!this.checkPermission()) {
                this.isError = true;
                this.isEnd = true;

                LogTool.e(TransferServer.TAG, "没有存储权限");
            }
        } catch (IOException e) {
            this.isError = true;
            this.isEnd = true;

            LogTool.e(TransferServer.TAG, "接收异常:" + e.getMessage());
        } finally {
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
                if (fos != null) {
                    fos.close();
                }
                this.isEnd = true;
            } catch (IOException e) {
            }
        }

    }

    /**
     * 检查权限
     *
     * @return true/false
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PackageManager pm = TransferServer.getInstance().getContext().getPackageManager();

            if ((PackageManager.PERMISSION_GRANTED !=
                    pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, TransferServer.getInstance().getContext().getPackageName()) && !Environment.isExternalStorageManager())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 创建文件
     *
     * @return 文件对象
     */
    private File createFile() {
        //检查路径是否存在
        File file = new File(this.mPath);
        if (!file.exists()) {
            file.mkdir();
        }

        //创建文件
        String sb = this.mPath + File.separator + this.mFileName;
        file = new File(sb);

        if (file.exists()) {
            //删除文件
            FileTool.delete(file.getPath());
        }

        try {
            file.createNewFile();
        } catch (Exception e) {
        }

        return file;
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            this.mSocket.close();
        } catch (IOException e) {
        }
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    public String getPath() {
        return this.mPath;
    }

    /**
     * 获取接收速度
     *
     * @return 接收速度
     */
    public long getReceiveRate() {
        return getRate();
    }

    /**
     * 计算接收速度
     */
    private class CountRate extends Thread {

        @Override
        public void run() {
            super.run();

            long preTime;
            long currTime;
            long timeDiff;
            long preLenght;
            long currLenght;

            preLenght = ReceiveThread.this.mTransferLenght;
            preTime = System.currentTimeMillis();
            while (ReceiveThread.this.mTransferLenght < ReceiveThread.this.mFileLenght && !ReceiveThread.this.isEnd) {
                currTime = System.currentTimeMillis();
                currLenght = ReceiveThread.this.mTransferLenght;
                timeDiff = currTime - preTime;
                if (timeDiff != 0) {
                    ReceiveThread.this.mCurrRate = (long) ((double) (ReceiveThread.this.mTransferLenght - preLenght) * 1000.0 / (double) timeDiff);
                } else {
                    ReceiveThread.this.mCurrRate = 0;
                }
                preLenght = currLenght;
                preTime = currTime;
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
            }

            ReceiveThread.this.mCurrRate = 0;
        }
    }
}