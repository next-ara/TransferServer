package com.next.module.transfer.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.next.module.transfer.server.TransferServer;
import com.next.module.transfer.server.listener.OnCustomReceiveListener;
import com.next.module.transfer.server.listener.OnReceiveListener;
import com.next.module.transfer.server.listener.OnSendListener;
import com.next.module.transfer.server.tool.LogTool;
import com.next.module.transfer.server.tool.TransferListenerTool;

import java.io.File;
import java.util.ArrayList;

/**
 * ClassName:传输服务类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class TransferService extends Service {

    //接收文件接口
    public interface ReceiveFile {

        /**
         * 开始接收文件
         *
         * @param mReceiveListener 接收监听接口
         */
        void start(OnReceiveListener mReceiveListener);
    }

    //服务绑定对象
    private Binder mBinder;

    //服务线程对象
    private ServerThread mServerThread;

    //发送线程对象
    private SendThread mSendThread;

    //文件保存路径
    private File mFileSavePath;

    //是否正在传输
    private boolean isTransfer;

    //接收操作对象
    private MyReceiveOper mReceiveOper;

    @Override
    public void onCreate() {
        super.onCreate();
        this.init();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        this.mServerThread.close();
        super.onDestroy();
    }

    /**
     * 初始化
     */
    private void init() {
        this.isTransfer = false;
        this.mBinder = new ServerBinder();
        this.mFileSavePath = new File(TransferServer.getInstance().getTransferData().getFileReceiveAddress());
        this.mServerThread = ServerThread.createServerThread(TransferServer.getInstance().getTransferData().getPort(), this.mFileSavePath.getAbsolutePath(), this.getLastHostName());
        this.mReceiveOper = new MyReceiveOper();
        //注册接收操作
        this.mServerThread.registerReceiveOper(this.mReceiveOper);
        //启动服务
        this.mServerThread.start();
    }

    /**
     * 获取上次的主机名
     *
     * @return 主机名
     */
    private String getLastHostName() {
        String hostName = android.os.Build.MODEL;
        try {
            hostName = TransferServer.getInstance().getTransferData().getHostName();
        } catch (Exception e) {
        }

        return hostName;
    }

    /**
     * 接收文件
     *
     * @param receive           接收线程
     * @param onReceiveListener 接收监听接口
     */
    private void receiveFile(final ReceiveThread receive, OnReceiveListener onReceiveListener) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                //设置传输状态
                TransferService.this.isTransfer = true;

                //发送文件接收开始监听
                TransferListenerTool.fileReceive(onReceiveListener);

                //开始接收文件
                receive.start();

                //回调频率
                long callBackRate = TransferServer.getInstance().getTransferData().getCallBackRate();

                do {
                    try {
                        //休眠
                        Thread.sleep(callBackRate);
                    } catch (Exception e) {
                    }

                    //发送文件接收进度监听
                    TransferListenerTool.fileReceiveProgress((int) (receive.getProgress() * 100.0), receive.getRate(), onReceiveListener);

                    //判断是否接收完成
                } while (!receive.isEnd());

                if (receive.isError() || (int) (receive.getProgress() * 100.0) != 100) {
                    //发送文件接收错误监听
                    TransferListenerTool.fileReceiveError(onReceiveListener);

                    LogTool.i(TransferServer.TAG, "接收出错");
                } else {
                    //发送文件接收结束监听
                    TransferListenerTool.fileReceiveEnd(onReceiveListener);

                    LogTool.i(TransferServer.TAG, "接收完成");
                }

                //设置文件接收结束
                TransferService.this.isTransfer = false;
            }
        }.start();
    }

    /**
     * 接收操作
     */
    public class MyReceiveOper implements ServerThread.ReceiveOper {
        @Override
        public void operate(final ReceiveThread receive) {
            //判断是否正在传输
            while (TransferService.this.isTransfer) {
            }

            //获取自定义接收监听
            OnCustomReceiveListener onCustomReceiveListener = TransferServer.getInstance().getOnCustomReceiveListener();
            //判断是否有自定义接收监听
            if (onCustomReceiveListener == null) {
                //接收文件
                TransferService.this.receiveFile(receive, null);
            } else {
                onCustomReceiveListener.onReceive(mReceiveListener -> {
                    //接收文件
                    TransferService.this.receiveFile(receive, mReceiveListener);
                }, receive.getFileName());
            }
        }
    }

    //服务绑定
    public class ServerBinder extends Binder {

        /**
         * 发送文件
         *
         * @param ip             接收端IP
         * @param port           接收端端口
         * @param file_paths     文件路径列表
         * @param onSendListener 发送监听接口
         */
        public void sendFileList(final String ip, final int port, final ArrayList<String> file_paths, OnSendListener onSendListener) {
            //判断是否正在传输
            if (TransferService.this.isTransfer) {
                return;
            }

            try {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        //设置传输状态
                        TransferService.this.isTransfer = true;

                        //发送文件发送开始监听
                        TransferListenerTool.fileSend(onSendListener);

                        ArrayList<String> list = new ArrayList<>(file_paths);

                        for (String path : list) {
                            File file = new File(path);
                            if (!file.exists()) {
                                continue;
                            }
                            try {
                                TransferService.this.mSendThread = new SendThread(ip, port, file);

                                //启动发送线程
                                TransferService.this.mSendThread.start();
                            } catch (Exception e) {
                                continue;
                            }

                            //回调频率
                            long callBackRate = TransferServer.getInstance().getTransferData().getCallBackRate();

                            while (true) {
                                try {
                                    //休眠
                                    Thread.sleep(callBackRate);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                //发送文件发送进度监听
                                TransferListenerTool.fileSendProgress((int) (TransferService.this.mSendThread.getProgress() * 100.0), TransferService.this.mSendThread.getRate(), onSendListener);

                                //判断是否发送完成
                                if (TransferService.this.mSendThread.isEnd()) {
                                    break;
                                }
                            }
                        }

                        if (TransferService.this.mSendThread.isError()) {
                            //发送文件发送错误监听
                            TransferListenerTool.fileSendError(onSendListener);

                            LogTool.i(TransferServer.TAG, "发送出错");
                        } else {
                            //发送文件发送结束监听
                            TransferListenerTool.fileSendEnd(onSendListener);

                            LogTool.i(TransferServer.TAG, "发送结束");
                        }

                        TransferService.this.mSendThread = null;
                        TransferService.this.isTransfer = false;
                    }
                }.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 获取主机名
         *
         * @return 主机名
         */
        public String getHostName() {
            return TransferService.this.mServerThread.getHostName();
        }

        /**
         * 设置主机名
         *
         * @param hostName 主机名
         */
        public void setHostName(String hostName) {
            TransferService.this.mServerThread.setHostName(hostName);
            TransferServer.getInstance().setHostName(hostName);
        }

        /**
         * 停止发送
         */
        public void stopSend() {
            if (TransferService.this.mSendThread != null) {
                //关闭发送线程
                TransferService.this.mSendThread.close();
            }
        }
    }
}