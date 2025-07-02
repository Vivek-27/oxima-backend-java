//package com.oxima.oxima.websocket;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.oxima.oxima.model.Location;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class LocationWebSocketHandler implements WebSocketHandler {
//
//    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        System.out.println("Frontend is connected to webSocket with session id: "+session);
//        sessions.add(session);
//    }
//
//    @Override
//    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {}
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) {}
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        sessions.remove(session);
//    }
//
//    @Override
//    public boolean supportsPartialMessages() {
//        return false;
//    }
//
//    public boolean broadcast(Location location) {
//        String json;
//        try {
//            json = objectMapper.writeValueAsString(location);
//            synchronized (sessions) {
//                for (WebSocketSession session : sessions) {
//                    session.sendMessage(new TextMessage(json));
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return true;
//
//    }
//}
