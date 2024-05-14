package com.next.module.transfer.server.tool;

import android.util.Log;

import com.next.module.transfer.server.TransferServer;

/**
 * ClassName:日志工具类
 *
 * @author Afton
 * @time 2023/11/20
 * @auditor
 */
public class LogTool {

    /**
     * 打印日志
     *
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void i(String tag, String msg) {
        if (TransferServer.getInstance().getTransferData().isOpenLog()) {
            Log.i(tag, msg);
        }
    }

    /**
     * 打印错误日志
     *
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void e(String tag, String msg) {
        if (TransferServer.getInstance().getTransferData().isOpenLog()) {
            Log.e(tag, msg);
        }
    }

    /**
     * 打印调试日志
     *
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void d(String tag, String msg) {
        if (TransferServer.getInstance().getTransferData().isOpenLog()) {
            Log.d(tag, msg);
        }
    }

    /**
     * 打印调试日志
     *
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void v(String tag, String msg) {
        if (TransferServer.getInstance().getTransferData().isOpenLog()) {
            Log.v(tag, msg);
        }
    }

    /**
     * 打印警告日志
     *
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void w(String tag, String msg) {
        if (TransferServer.getInstance().getTransferData().isOpenLog()) {
            Log.w(tag, msg);
        }
    }
}