package com.daifukuamerica.wrxj.web.core.connection;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;


//TODO - determine which broker is active, i believe i accidentally configured two

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig extends AbstractWebSocketMessageBrokerConfigurer
{
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
//    	config.enableStompBrokerRelay("/topic","/queue").setRelayHost("jnp://localhost").setRelayPort(5099); 
        config.setApplicationDestinationPrefixes("/app");
        
    }
 
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
         registry.addEndpoint("/jms").withSockJS();
    }
	
}
