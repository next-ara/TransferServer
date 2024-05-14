package com.next.module.transfer.server.listener;

/**
 * ClassName:发送监听接口
 *
 * @author Afton
 * @time 2023/8/20
 * @auditor
 */
public interface OnSendListener {

    /**
     * 文件发送开始
     */
    void onStart();

    /**
     * 文件发送进度
     *
     * @param progress 进度
     * @param rate     速度
     */
    void onProgress(int progress, long rate);

    /**
     * 文件发送出错
     */
    void onError();

    /**
     * 文件发送结束
     */
    void onFinish();
}