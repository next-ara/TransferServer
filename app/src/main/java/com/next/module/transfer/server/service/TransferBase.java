package com.next.module.transfer.server.service;

/**
 * ClassName:传输基类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class TransferBase {

    //IP地址
    protected String mIp;

    //端口号
    protected int mPort;

    //主机名
    protected String mHostName;

    //线程对象
    protected Thread mThread;

    //启动线程
    public void start() {
        mThread.start();
    }

    /**
     * 判断线程是否存活
     *
     * @return true/false
     */
    public boolean isAlive() {
        return mThread.isAlive();
    }

    /**
     * 返回IP地址
     *
     * @return IP地址
     */
    public String getIP() {
        return mIp;
    }

    /**
     * 返回端口号
     *
     * @return 端口号
     */
    public int getPort() {
        return mPort;
    }

    /**
     * 设置主机名
     *
     * @param hostName 主机名
     */
    public void setHostName(String hostName) {
        this.mHostName = hostName;
    }

    /**
     * 返回主机名
     *
     * @return 主机名
     */
    public String getHostName() {
        return mHostName;
    }
}