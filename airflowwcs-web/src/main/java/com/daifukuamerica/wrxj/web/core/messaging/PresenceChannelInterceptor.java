package com.daifukuamerica.wrxj.web.core.messaging;

import org.springframework.messaging.Message;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresenceChannelInterceptor extends ChannelInterceptorAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(PresenceChannelInterceptor.class);

 
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
 
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
 
        // ignore non-STOMP messages like heartbeat messages
        if(sha.getCommand() == null) {
            return;
        }
 
        String sessionId = sha.getSessionId();
 
        switch(sha.getCommand()) {
            case CONNECT:
                logger.error("STOMP Connect [sessionId: {}]", sessionId);
                break;
            case CONNECTED:
                logger.error("STOMP Connected [sessionId: {}]", sessionId);
                break;
            case DISCONNECT:
                logger.error("STOMP Disconnect [sessionId: {}]", sessionId);
                break;
            default:
                break;
 
        }
    }
}
