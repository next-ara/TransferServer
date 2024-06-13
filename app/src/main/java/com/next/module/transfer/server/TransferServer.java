package com.next.module.transfer.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.next.module.file2.File2;
import com.next.module.transfer.server.data.TransferData;
import com.next.module.transfer.server.listener.OnCustomReceiveListener;
import com.next.module.transfer.server.listener.OnSendListener;
import com.next.module.transfer.server.listener.OnServiceStateListener;
import com.next.module.transfer.server.service.TransferService;

import java.util.ArrayList;

/**
 * ClassName:传输服务类
 *
 * @author Afton
 * @time 2023/8/20
 * @auditor
 */
public class TransferServer {

    public static final int SDK_VERSION = 1;

    public static final String TAG = "TransferServer";

    private static TransferServer instance;

    //上下文
    private Context context;

    //传输服务相关
    private ServiceConnection serviceConnection;

    //传输服务对象
    private TransferService.ServerBinder binder;

    //是否开启服务
    private boolean isOpen = false;

    //自定义接收监听接口
    private OnCustomReceiveListener onCustomReceiveListener;

    //服务状态监听接口
    private OnServiceStateListener onServiceStateListener;

    //传输数据对象
    private TransferData transferData;

    public static TransferServer getInstance() {
        if (instance == null) {
            instance = new TransferServer();
        }

        return instance;
    }

    private TransferServer() {
        this.transferData = new TransferData();
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @return 传输服务对象
     */
    public TransferServer init(Context context) {
        this.context = context;

        return this;
    }

    /**
     * 传输服务启动
     */
    public void start() {
        if (this.binder == null || !this.isOpen) {
            this.initTransferService();
        }
    }

    /**
     * 设置传输端口号
     *
     * @param port 端口号
     * @return 传输服务对象
     */
    public TransferServer setPort(int port) {
        this.transferData.setPort(port);

        return this;
    }

    /**
     * 设置文件接收地址
     *
     * @param address 文件接收地址
     * @return 传输服务对象
     */
    public TransferServer setFileReceiveAddress(String address) {
        this.transferData.setFileReceiveAddress(address);

        return this;
    }

    /**
     * 停止发送
     */
    public void stopSend() {
        this.binder.stopSend();
    }

    /**
     * 发送文件
     *
     * @param ip             接收方ip
     * @param fileList       待发送的文件列表
     * @param onSendListener 发送监听接口
     */
    public void sendFileList(String ip, ArrayList<File2> fileList, OnSendListener onSendListener) {
        //设置端口
        this.binder.sendFileList(ip, this.transferData.getPort(), fileList, onSendListener);
    }

    /**
     * 发送文件
     *
     * @param ip             接收方ip
     * @param port           接收方端口
     * @param fileList       待发送的文件列表
     * @param onSendListener 发送监听接口
     */
    public void sendFileList(String ip, int port, ArrayList<File2> fileList, OnSendListener onSendListener) {
        //设置端口
        this.binder.sendFileList(ip, port, fileList, onSendListener);
    }

    /**
     * 停止并解绑服务
     */
    public void unTransferServer() {
        if (this.isOpen) {
            this.context.unbindService(this.serviceConnection);
        }
    }

    /**
     * 加载传输服务
     */
    private void initTransferService() {
        this.isOpen = false;

        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                TransferServer.this.binder = (TransferService.ServerBinder) service;
                if (TransferServer.this.binder != null) {
                    TransferServer.this.isOpen = true;
                    //设置主机名
                    TransferServer.this.binder.setHostName(TransferServer.this.transferData.getHostName());
                }

                if (TransferServer.this.onServiceStateListener != null) {
                    //打开服务
                    TransferServer.this.onServiceStateListener.onOpen();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                TransferServer.this.isOpen = false;

                if (TransferServer.this.onServiceStateListener != null) {
                    //关闭服务
                    TransferServer.this.onServiceStateListener.onClose();
                }
            }
        };

        Intent intent = new Intent(this.context, TransferService.class);
        this.context.bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 设置自定义文件接收监听接口
     *
     * @param onCustomReceiveListener 自定义文件接收监听接口
     * @return 传输服务对象
     */
    public TransferServer setOnCustomReceiveListener(OnCustomReceiveListener onCustomReceiveListener) {
        this.onCustomReceiveListener = onCustomReceiveListener;

        return this;
    }

    /**
     * 设置服务状态监听接口
     *
     * @param onServiceStateListener 服务状态监听接口
     * @return 传输服务对象
     */
    public TransferServer setOnServiceStateListener(OnServiceStateListener onServiceStateListener) {
        this.onServiceStateListener = onServiceStateListener;

        return this;
    }

    /**
     * 设置是否打开日志
     *
     * @param openLog 是否打开日志
     * @return 传输服务对象
     */
    public TransferServer setOpenLog(boolean openLog) {
        this.transferData.setOpenLog(openLog);

        return this;
    }

    /**
     * 设置主机名
     *
     * @param name 主机名
     * @return 传输服务对象
     */
    public TransferServer setHostName(String name) {
        this.transferData.setHostName(name);

        return this;
    }

    /**
     * 设置回调频率
     *
     * @param rate 回调频率
     * @return 传输服务对象
     */
    public TransferServer setCallBackRate(long rate) {
        this.transferData.setCallBackRate(rate);

        return this;
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * 获取传输服务是否开启
     *
     * @return 传输服务是否开启
     */
    public boolean isOpen() {
        return this.isOpen;
    }

    /**
     * 获取自定义文件接收监听接口
     *
     * @return 自定义文件接收监听接口
     */
    public OnCustomReceiveListener getOnCustomReceiveListener() {
        return this.onCustomReceiveListener;
    }

    /**
     * 获取传输数据对象
     *
     * @return 传输数据对象
     */
    public TransferData getTransferData() {
        return this.transferData;
    }
}