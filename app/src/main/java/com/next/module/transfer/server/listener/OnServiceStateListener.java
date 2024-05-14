package com.next.module.transfer.server.listener;

/**
 * ClassName:服务状态监听接口
 *
 * @author Afton
 * @time 2023/11/21
 * @auditor
 */
public interface OnServiceStateListener {

    /**
     * 打开服务
     */
    void onOpen();

    /**
     * 关闭服务
     */
    void onClose();
}