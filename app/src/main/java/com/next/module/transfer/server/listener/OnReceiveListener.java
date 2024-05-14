package com.next.module.transfer.server.listener;

/**
 * ClassName:接收监听接口
 *
 * @author Afton
 * @time 2023/8/20
 * @auditor
 */
public interface OnReceiveListener {

    /**
     * 文件接收开始
     */
    void onStart();

    /**
     * 文件接收进度
     *
     * @param progress 进度
     * @param rate     速度
     */
    void onProgress(int progress, long rate);

    /**
     * 文件接收出错
     */
    void onError();

    /**
     * 文件接收结束
     */
    void onFinish();
}