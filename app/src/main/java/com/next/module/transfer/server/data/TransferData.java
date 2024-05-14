package com.next.module.transfer.server.data;

import android.os.Build;
import android.os.Environment;

/**
 * ClassName:传输数据类
 *
 * @author Afton
 * @time 2023/11/20
 * @auditor
 */
public class TransferData {

    //主机名称
    private String hostName;

    //文件接收地址
    private String fileReceiveAddress;

    //传输服务端口号
    private int port;

    //是否开启日志打印
    private boolean isOpenLog;

    //回调频率
    private long callBackRate;

    public TransferData() {
        this.hostName = Build.MODEL;
        this.fileReceiveAddress = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        this.port = 5110;
        this.isOpenLog = false;
        this.callBackRate = 1000;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getFileReceiveAddress() {
        return fileReceiveAddress;
    }

    public void setFileReceiveAddress(String fileReceiveAddress) {
        this.fileReceiveAddress = fileReceiveAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isOpenLog() {
        return isOpenLog;
    }

    public void setOpenLog(boolean openLog) {
        isOpenLog = openLog;
    }

    public long getCallBackRate() {
        return callBackRate;
    }

    public void setCallBackRate(long callBackRate) {
        this.callBackRate = callBackRate;
    }
}