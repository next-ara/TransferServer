package com.next.module.transfer.server.service;

import com.next.module.file2.File2;
import com.next.module.transfer.server.TransferServer;
import com.next.module.transfer.server.tool.LogTool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ClassName:发送线程类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class SendThread extends TransferSocket implements Runnable {

    //连接套接字
    private Socket mSocket;

    //处理流
    //从网络接收
    private DataInputStream mDis;
    //发送到网络
    private DataOutputStream mDos;
    //读取文件
    private DataInputStream mFis;

    //待发送的文件对象
    private File2 mFile;

    public SendThread(String iP, int port, File2 file) throws NoSuchFieldException {
        super();
        this.mIp = iP;
        this.mPort = port;
        this.mFile = file;
        if (!file.exists()) {
            throw new NoSuchFieldException("文件不存在");
        }
        init();
    }

    private void init() {
        this.mCurrRate = 0;
        this.mTransferLenght = 0;
        this.mFileLenght = this.mFile.length();
        this.mFileName = this.mFile.getName();
        this.isError = false;
        this.isEnd = false;
        this.mThread = new Thread(this);

        //初始化缓冲区
        this.data = new byte[8192];
        this.len = 0;
    }

    private void initRun() throws IOException {
        //建立连接，超时10秒连接失败
        this.mSocket = new Socket();
        this.mSocket.connect(new InetSocketAddress(this.mIp, this.mPort), 5000);

        //获取处理流
        this.mDis = new DataInputStream(new BufferedInputStream(this.mSocket.getInputStream()));
        this.mDos = new DataOutputStream(new BufferedOutputStream(this.mSocket.getOutputStream()));
        this.mFis = new DataInputStream(new BufferedInputStream(this.mFile.openInputStream()));
    }

    @Override
    public void run() {
        try {
            //初始化
            this.initRun();

            //获取主机名
            this.len = this.mDis.read(this.data);
            this.mHostName = new String(this.data, 0, this.len, "GBK");

            this.mDos.writeLong(this.mFileLenght);
            this.mDos.flush();
            this.mDos.write(this.mFileName.getBytes("GBK"));
            this.mDos.flush();
            this.mDis.read();

            //计算发送速度
            new CountRate().start();

            //发送文件内容
            this.len = 0;
            while ((this.len = this.mFis.read(this.data)) != -1) {
                this.mDos.write(this.data, 0, this.len);
                //记录已发送的长度
                this.mTransferLenght += this.len;
            }
            this.mDos.flush();
        } catch (IOException e) {
            this.isError = true;
            this.isEnd = true;
            this.mCurrRate = 0;

            LogTool.e(TransferServer.TAG, "发送异常：" + e.getMessage());
        } finally {
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                }
                if (this.mFis != null) {
                    this.mFis.close();
                }
                this.isEnd = true;
            } catch (IOException e) {
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            this.mSocket.close();
            this.isError = true;
        } catch (IOException e) {
        }
    }

    /**
     * 获取发送速度
     *
     * @return 发送速度
     */
    public long getSendRate() {
        return getRate();
    }

    /**
     * 计算发送速度
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

            preLenght = SendThread.this.mTransferLenght;
            preTime = System.currentTimeMillis();
            while (SendThread.this.mTransferLenght < SendThread.this.mFileLenght && !SendThread.this.isEnd) {
                currTime = System.currentTimeMillis();
                currLenght = SendThread.this.mTransferLenght;
                timeDiff = currTime - preTime;
                if (timeDiff != 0) {
                    SendThread.this.mCurrRate = (long) ((double) (SendThread.this.mTransferLenght - preLenght) * 1000.0 / (double) timeDiff);
                } else {
                    SendThread.this.mCurrRate = 0;
                }
                preLenght = currLenght;
                preTime = currTime;
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
            }
            SendThread.this.mCurrRate = 0;
        }
    }
}