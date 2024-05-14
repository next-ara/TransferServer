package com.next.module.transfer.server.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClassName:服务端线程类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class ServerThread extends TransferBase implements Runnable {

    //收到发送请求时用户的操作接口
    public interface ReceiveOper {

        /**
         * 接收到发送请求时用户的操作接口
         *
         * @param receive 接收线程
         */
        void operate(ReceiveThread receive);
    }

    private static ServerThread mServerThread;

    //服务端套接字
    private ServerSocket mServerSocket;

    //文件保存路径
    private String mFileSavaPath;

    //接收到发送请求时用户的操作接口
    private ReceiveOper mReceiveOper;

    private ServerThread(String ip, int mPort, String path) {
        this.mIp = ip;
        this.mPort = mPort;
        this.mFileSavaPath = path;
        this.mThread = new Thread(this);
        this.mHostName = "";
        this.init();
    }

    private ServerThread(String ip, int mPort, String path, String hostName) {
        this.mHostName = hostName;
        this.mIp = ip;
        this.mPort = mPort;
        this.mFileSavaPath = path;
        this.mThread = new Thread(this);
        this.init();
    }

    private ServerThread(int mPort, String path) {
        this.mIp = null;
        this.mPort = mPort;
        this.mFileSavaPath = path;
        this.mThread = new Thread(this);
        this.mHostName = "";
        this.init();
    }

    private ServerThread(int mPort, String path, String hostName) {
        this.mHostName = hostName;
        this.mIp = null;
        this.mPort = mPort;
        this.mFileSavaPath = path;
        this.mThread = new Thread(this);
        this.mHostName = hostName;
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        //设置主机名
        if (this.mHostName == null || this.mHostName.equals("")) {
            //获取设备名称
            this.mHostName = android.os.Build.MODEL;
        }

        //默认的收到发送请求的实现接口，将会无条件接收发送过来的文件
        this.mReceiveOper = TransferBase::start;
    }

    /**
     * 创建服务端线程对象
     *
     * @param ip   指定ip
     * @param port 端口
     * @param path 文件保存路径
     * @return 服务端线程对象
     */
    public static synchronized ServerThread createServerThread(String ip, int port, String path) {
        if (mServerThread != null) {
            mServerThread.close();
            mServerThread = null;
        }
        mServerThread = new ServerThread(ip, port, path);
        return mServerThread;
    }

    /**
     * 创建服务端线程对象
     *
     * @param ip   指定ip
     * @param port 端口
     * @param path 文件保存路径
     * @return 服务端线程对象
     */
    public static synchronized ServerThread createServerThread(String ip, int port, String path, String hostName) {
        if (mServerThread != null) {
            mServerThread.close();
            mServerThread = null;
        }
        mServerThread = new ServerThread(ip, port, path, hostName);
        return mServerThread;
    }

    /**
     * 创建服务端线程对象
     *
     * @param port 端口
     * @param path 文件保存路径
     * @return 服务端线程对象
     */
    public static synchronized ServerThread createServerThread(int port, String path) {
        if (mServerThread != null) {
            mServerThread.close();
            mServerThread = null;
        }
        mServerThread = new ServerThread(port, path);
        return mServerThread;
    }

    /**
     * 创建服务端线程对象
     *
     * @param port 端口
     * @param path 文件保存路径
     * @return 服务端线程对象
     */
    public static synchronized ServerThread createServerThread(int port, String path, String hostName) {
        if (mServerThread != null) {
            mServerThread.close();
            mServerThread = null;
        }
        mServerThread = new ServerThread(port, path, hostName);
        return mServerThread;
    }

    /**
     * 注册接收发送请求的操作接口
     *
     * @param mReceiveOper 接收发送请求的操作接口对象
     */
    public void registerReceiveOper(ReceiveOper mReceiveOper) {
        this.mReceiveOper = mReceiveOper;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    if (this.mServerSocket == null || this.mServerSocket.isClosed()) {
                        //打开服务端套接字
                        this.mServerSocket = this.openServerSocket();
                        continue;
                    }
                    //等待连接
                    Socket sc = this.mServerSocket.accept();
                    //处理接收到的连接
                    this.mReceiveOper.operate(new ReceiveThread(sc, this.mFileSavaPath, getHostName()));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            this.mServerThread = null;
        } finally {
            try {
                if (this.mServerSocket != null)
                    this.mServerSocket.close();
            } catch (IOException e) {
            }
            this.mServerThread = null;
        }
    }

    /**
     * 打开服务端套接字
     *
     * @return 服务端套接字
     */
    private ServerSocket openServerSocket() {
        ServerSocket ssc = null;
        while (true) {
            try {
                if (this.mIp == null || !this.mIp.matches("\\d{1,3}((.\\d{1,3}){3})")) {
                    ssc = new ServerSocket(this.mPort);
                } else {
                    ssc = new ServerSocket(mPort, 0,
                            InetAddress.getByName(this.mIp));
                }

                break;
            } catch (Exception e) {
                this.mPort++;
            }
        }
        return ssc;
    }

    /**
     * 获取文件保存路径
     *
     * @return 文件保存路径
     */
    public String getFileSavaPath() {
        return this.mFileSavaPath;
    }

    /**
     * 设置文件保存路径
     *
     * @param FileSavaPath 文件保存路径
     */
    public void setFileSavaPath(String FileSavaPath) {
        this.mFileSavaPath = FileSavaPath;
    }

    /**
     * 中断线程
     */
    public void interrupt() {
        this.mThread.interrupt();
    }

    /**
     * 判断线程是否中断
     *
     * @return true/false
     */
    public boolean isInterrupted() {
        return this.mThread.isInterrupted();
    }

    /**
     * 关闭服务端套接字
     *
     * @return true/false
     */
    public boolean close() {
        try {
            if (this.mServerSocket != null) {
                this.mServerSocket.close();
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
}