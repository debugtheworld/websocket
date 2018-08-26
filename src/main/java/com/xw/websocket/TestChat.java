package com.xw.websocket;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/chat", configurator = MyConfigurator.class)
public class TestChat {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<Session> webSocketSet = new CopyOnWriteArraySet<Session>();
    //线程安全的Map
    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<String, Session>();//建立连接的方法

    private String remoteAddr = "";

    @OnOpen
    public void onOpen(Session session) {
        addOnlineCount(); //在线数加
        webSocketSet.add(session);
        remoteAddr = getRomteAddr(session);
        webSocketMap.put(remoteAddr, session);
        Iterator<Session> iterator = webSocketSet.iterator();
        try {
            while (iterator.hasNext()) {
                sendMessage("ip为" + remoteAddr + "的用户进入了聊天室，当前在线用户数量为" + getOnlineCount(), iterator.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        subOnlineCount(); //在线数减
        webSocketSet.remove(session);
        webSocketMap.remove(remoteAddr);
        Iterator<Session> iterator = webSocketSet.iterator();
        try {
            while (iterator.hasNext()) {
                sendMessage("ip为" + remoteAddr + "的用户离开了聊天室，当前在线用户数量为" + getOnlineCount(), iterator.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Iterator<Session> iterator = webSocketSet.iterator();
        try {
            while (iterator.hasNext()) {
                sendMessage(remoteAddr + ":  " + message, iterator.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message, Session session) throws IOException {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        TestChat.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        TestChat.onlineCount--;
    }

    public String getRomteAddr(Session session) {
        String remoteAddr = session.getUserProperties().get("remoteAddr").toString();
        return remoteAddr;
    }
}
