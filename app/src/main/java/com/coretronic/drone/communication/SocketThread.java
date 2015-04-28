package com.coretronic.drone.communication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by jiaLian on 15/4/22.
 */
public abstract class SocketThread extends Thread {
    private static final String TAG = SocketThread.class.getSimpleName();
    public static final String BROADCAST_ACTION_SOCKET_STATUS = "com.coretronic.drone.g2.socket.status";
    public static final String STATUS = "status";
    public static final String SOCKET_STATUS_CONNECTED = "socket_status_connected";
    public static final String SOCKET_STATUS_CONNECTFAILURE = "socket_status_connect_failure";
    public static final String SOCKET_STATUS_FAILURE = "socket_status_failure";

    public static final int DELAY_TIME = 500;

    private Context context;
    private Socket socket = null;
    private String dstAddress = null;
    private int dstPort;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private String workStatus;
    private boolean isRunning = false;
    private int connectCount = 0;

    public abstract void onReceiver(byte[] readBuffer);

    public SocketThread(Context context, String dstAddress, int dstPort) {
        this.context = context;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        isRunning = true;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        try {
            while (isRunning) {
                if (socket == null && connectCount < 5) {
                    connectSocket();
                    connectCount++;
                }
                Thread.sleep(DELAY_TIME);
                if (socket != null && !socket.isClosed()) {
                    if (socket.isConnected()) {
                        if (!socket.isInputShutdown()) {
                            int count = 0;
                            while (count == 0) {
                                count += inStream.available();
                            }
                            while (bytes < count) {
                                bytes += inStream.read(buffer, bytes, count - bytes);
                            }
                            byte[] readBuffer = new byte[bytes];
                            System.arraycopy(buffer, 0, readBuffer, 0, bytes);
                            onReceiver(readBuffer);
                            bytes = 0;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            cancel();
        }
    }

    public void connectSocket() {
        try {
            socket = new Socket(dstAddress, dstPort);
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
            workStatus = SOCKET_STATUS_CONNECTED;
        } catch (SocketException ex) {
            Log.v(TAG, "socketException ");
            ex.printStackTrace();
            workStatus = SOCKET_STATUS_CONNECTFAILURE;

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            workStatus = SOCKET_STATUS_CONNECTFAILURE;

        } catch (Exception ex) {
            ex.printStackTrace();
            workStatus = SOCKET_STATUS_CONNECTFAILURE;


        } finally {
            Log.v(TAG, "Work Status: " + workStatus);
            sendBroadcast();
        }
    }

    public void sendBroadcast() {
        Intent intent = new Intent(BROADCAST_ACTION_SOCKET_STATUS);
        intent.putExtra(STATUS, workStatus);
        context.sendBroadcast(intent);
    }

    public void write(byte[] buffer) {
        if (!socket.isConnected() || (socket.isClosed())) // isConnected（）返回的是是否曾经连接过，isClosed()返回是否处于关闭状态，只有当isConnected（）返回true，isClosed（）返回false的时候，网络处于连接状态
        {
            for (int i = 0; i < 3 && workStatus == null; i++) {// 如果连接处于关闭状态，重试三次，如果连接正常了，跳出循环
                socket = null;
                connectSocket();
                if (socket.isConnected() && (!socket.isClosed())) {
                    break;
                }
            }
            if (!socket.isConnected() || (socket.isClosed()))// 如果此时连接还是不正常，提示错误，并跳出循环
            {
                workStatus = SOCKET_STATUS_CONNECTFAILURE;
                Log.v(TAG, "workStatus is not connected!111444");
                return;
            }

        }

        if (!socket.isOutputShutdown()) {// 输入输出流是否关闭
            try {
                outStream.write(buffer);
            } catch (Exception e) {
                Log.v(TAG, "workStatus is not connected!55555666666");
                e.printStackTrace();
                workStatus = SOCKET_STATUS_FAILURE;
            }
        } else {
            workStatus = SOCKET_STATUS_CONNECTFAILURE;
        }
    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public void cancel() {
        try {
            isRunning = false;
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            workStatus = SOCKET_STATUS_CONNECTFAILURE;// 如果出现异常，提示网络连接出现问题。
            e.printStackTrace();
        }
    }
}
