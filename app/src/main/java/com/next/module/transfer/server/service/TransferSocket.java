package com.next.module.transfer.server.service;

/**
 * ClassName:传输Socket类
 *
 * @author Afton
 * @time 2023/11/19
 * @auditor
 */
public class TransferSocket extends TransferBase {

    //文件名
    protected String mFileName;

    //已发送长度
    protected long mTransferLenght;

    //文件总长度
    protected long mFileLenght;

    //发送速度
    protected long mCurrRate;

    //发送是否结束的标志，发送完成或者被接收端拒绝后都属于发送结束
    protected boolean isEnd;

    //发送是否失败的标志，发送或者接收连接失败
    protected boolean isError;

    //缓冲区
    protected byte[] data;

    //长度
    protected int len;

    /**
     * 获取文件总长度
     *
     * @return 长度
     */
    public long getFileLenght() {
        return this.mFileLenght;
    }

    /**
     * 获取发送是否结束
     *
     * @return true/false
     */
    public boolean isEnd() {
        //发送结束或者发送速度为0
        return this.isEnd && this.mCurrRate == 0;
    }

    /**
     * 获取发送是否失败
     *
     * @return true/false
     */
    public boolean isError() {
        //发送失败或者发送速度为0
        return this.isError && this.mCurrRate == 0;
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String getFileName() {
        return this.mFileName;
    }

    /**
     * 获取发送进度
     *
     * @return 进度
     */
    public double getProgress() {
        if (this.mFileLenght == 0) {
            return 0.0;
        }
        //计算进度
        return (double) this.mTransferLenght / (double) this.mFileLenght;
    }

    /**
     * 获取发送速度
     *
     * @return 速度
     */
    public long getRate() {
        return this.mCurrRate;
    }
}