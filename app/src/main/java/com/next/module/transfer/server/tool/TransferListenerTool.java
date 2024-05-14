package com.next.module.transfer.server.tool;

import com.next.module.transfer.server.TransferServer;
import com.next.module.transfer.server.listener.OnReceiveListener;
import com.next.module.transfer.server.listener.OnSendListener;

import java.text.DecimalFormat;

/**
 * ClassName:传输监听工具类
 *
 * @author Afton
 * @time 2023/8/20
 * @auditor
 */
public class TransferListenerTool {

    /**
     * 文件速度转换
     *
     * @param length 长度
     * @return 速度文本
     */
    private static String getRateString(long length) {
        DecimalFormat df = new DecimalFormat("0.0");
        double tem = length;
        tem /= 1024.0;
        if (1024.0 - tem > 0.0000000001) {
            return df.format(tem) + "KB";
        }
        tem /= 1024.0;
        if (1024.0 - tem > 0.0000000001) {
            return df.format(tem) + "MB";
        }
        tem /= 1024.0;
        return df.format(tem) + "GB";
    }

    /**
     * 文件接收开始
     *
     * @param onReceiveListener 接收监听接口
     */
    public static void fileReceive(OnReceiveListener onReceiveListener) {
        if (onReceiveListener != null) {
            onReceiveListener.onStart();
        }

        LogTool.i(TransferServer.TAG, "开始接收文件");
    }

    /**
     * 文件接收进度
     *
     * @param progress          进度
     * @param rate              速度
     * @param onReceiveListener 接收监听接口
     */
    public static void fileReceiveProgress(int progress, long rate, OnReceiveListener onReceiveListener) {
        if (onReceiveListener != null) {
            onReceiveListener.onProgress(progress, rate);
        }

        LogTool.i(TransferServer.TAG, "当前接收进度：" + progress + "%，速度：" + getRateString(rate) + "/s");
    }

    /**
     * 文件接收结束
     *
     * @param onReceiveListener 接收监听接口
     */
    public static void fileReceiveEnd(OnReceiveListener onReceiveListener) {
        if (onReceiveListener != null) {
            onReceiveListener.onFinish();
        }

        LogTool.i(TransferServer.TAG, "文件接收结束");
    }

    /**
     * 文件发送开始
     *
     * @param onSendListener 发送监听接口
     */
    public static void fileSend(OnSendListener onSendListener) {
        if (onSendListener != null) {
            onSendListener.onStart();
        }

        LogTool.i(TransferServer.TAG, "开始发送文件");
    }

    /**
     * 文件发送进度
     *
     * @param progress       进度
     * @param rate           速度
     * @param onSendListener 发送监听接口
     */
    public static void fileSendProgress(int progress, long rate, OnSendListener onSendListener) {
        if (onSendListener != null) {
            onSendListener.onProgress(progress, rate);
        }

        LogTool.i(TransferServer.TAG, "当前发送进度：" + progress + "%，速度：" + getRateString(rate) + "/s");
    }

    /**
     * 文件发送结束
     *
     * @param onSendListener 发送监听接口
     */
    public static void fileSendEnd(OnSendListener onSendListener) {
        if (onSendListener != null) {
            onSendListener.onFinish();
        }

        LogTool.i(TransferServer.TAG, "文件发送结束");
    }

    /**
     * 文件发送出错
     *
     * @param onSendListener 发送监听接口
     */
    public static void fileSendError(OnSendListener onSendListener) {
        if (onSendListener != null) {
            onSendListener.onError();
        }

        LogTool.i(TransferServer.TAG, "文件发送出错");
    }

    /**
     * 文件接收出错
     *
     * @param onReceiveListener 接收监听接口
     */
    public static void fileReceiveError(OnReceiveListener onReceiveListener) {
        if (onReceiveListener != null) {
            onReceiveListener.onError();
        }

        LogTool.i(TransferServer.TAG, "文件接收出错");
    }
}