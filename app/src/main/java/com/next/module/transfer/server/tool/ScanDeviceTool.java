package com.next.module.transfer.server.tool;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:扫描设备工具类
 *
 * @author Afton
 * @time 2024/7/1
 * @auditor
 */
public class ScanDeviceTool {

    //核心池大小
    private static final int CORE_POOL_SIZE = 1;
    //线程池最大线程数
    private static final int MAX_IMUM_POOL_SIZE = 255;

    //扫描设备监听接口
    public interface OnScanDeviceListener {

        /**
         * 开始扫描
         */
        void onStart();

        /**
         * 扫描到设备
         *
         * @param deviceName 设备名称
         * @param ipAddress  设备ip地址
         */
        void onScan(String deviceName, String ipAddress);

        /**
         * 扫描结束
         */
        void onEnd();
    }

    //线程池对象
    private ThreadPoolExecutor executor;

    /**
     * 扫描设备
     *
     * @param context              上下文
     * @param port                 端口号
     * @param onScanDeviceListener 扫描设备监听接口
     */
    public void start(Context context, int port, OnScanDeviceListener onScanDeviceListener) {
        //创建线程池
        this.creatExecutor();

        String address = getIp(context);
        String currnetIp = address.substring(0, address.lastIndexOf('.'));

        //扫描开始
        onScanDeviceListener.onStart();

        for (int i = 1; i < 255; i++) {
            this.executor.execute(this.creatRunnable(onScanDeviceListener, address, currnetIp, i, port));
        }

        this.executor.shutdown();

        while (this.executor != null) {
            try {
                if (this.executor.isTerminated()) {
                    onScanDeviceListener.onEnd();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止扫描
     */
    public void stop() {
        this.executor.shutdownNow();
        this.executor = null;
    }

    /**
     * 创建Runnable
     *
     * @param onScanDeviceListener 扫描设备监听接口
     * @param address              ip地址
     * @param currnetIp            ip地址前缀
     * @param lastAddress          ip地址后缀
     * @param port                 端口号
     * @return
     */
    private Runnable creatRunnable(OnScanDeviceListener onScanDeviceListener, String address, String currnetIp, int lastAddress, int port) {
        return () -> {
            try {
                int len = 0;
                byte[] data = new byte[8192];
                String targetIp1 = currnetIp + "." + lastAddress;
                if (targetIp1.equals(address)) return;

                Socket sc = new Socket();
                sc.connect(new InetSocketAddress(targetIp1, port), 250);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(sc.getInputStream()));
                String hostName = "";
                if ((len = dis.read(data)) != -1) {
                    hostName = new String(data, 0, len, "GBK");
                    //扫描到设备
                    onScanDeviceListener.onScan(hostName, targetIp1);
                }
                sc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * 创建线程池
     */
    private void creatExecutor() {
        this.executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(
                CORE_POOL_SIZE));
    }

    /**
     * 获取ip地址
     *
     * @param context 上下文
     * @return ip地址
     */
    private String getIp(Context context) {
        ArrayList<String> ipv4List = this.getIpv4s();
        if (!ipv4List.isEmpty()) {
            return (String) ipv4List.get(0);
        } else {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return this.intToIp(ipAddress);
        }
    }

    /**
     * 获取ipV4地址
     *
     * @return ipV4地址
     */
    private ArrayList<String> getIpv4s() {
        ArrayList<String> list = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (true) {
                    NetworkInterface anInterface;
                    do {
                        if (!interfaces.hasMoreElements()) {
                            return list;
                        }

                        anInterface = (NetworkInterface) interfaces.nextElement();
                    } while (anInterface.isLoopback());

                    Enumeration<InetAddress> addresses = anInterface.getInetAddresses();

                    while (addresses.hasMoreElements()) {
                        InetAddress address = (InetAddress) addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            list.add(address.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * ip转换
     *
     * @param i
     * @return ip地址
     */
    private String intToIp(int i) {
        return (i & 255) + "." + (i >> 8 & 255) + "." + (i >> 16 & 255) + "." + (i >> 24 & 255);
    }
}