package com.next.module.transfer.server.listener;

import com.next.module.transfer.server.service.TransferService;

/**
 * ClassName:自定义接收监听接口
 *
 * @author Afton
 * @time 2023/11/21
 * @auditor
 */
public interface OnCustomReceiveListener {

    /**
     * 接收文件
     *
     * @param receiveFile 接收文件对象
     * @param fileName    文件名
     */
    void onReceive(TransferService.ReceiveFile receiveFile, String fileName);
}