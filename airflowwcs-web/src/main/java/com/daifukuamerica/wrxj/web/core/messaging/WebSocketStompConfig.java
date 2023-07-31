package com.daifukuamerica.wrxj.web.core.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;


/**
 * The web socket configuration for the STOMP endpoint
 * 
 * Clients' browser will establish a websocket connection with the server, which has established 
 * it's own connection with the messaging server. The @EnableWebSocketMessageBroker allows us to 
 * both enable the message broker and override the default config settings. 
 * 
 * 
 * Author: dystout
 *
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig extends AbstractWebSocketMessageBrokerConfigurer
{

	/**
	 * Allow web socket connections to the '/stomp' endpoint in the {context} 
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry)
	{
		registry.addEndpoint("/stomp").setAllowedOrigins("*").withSockJS();
	}
	
	@Bean
    public PresenceChannelInterceptor presenceChannelInterceptor() {
        return new PresenceChannelInterceptor();
    }
 
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(presenceChannelInterceptor());
    }
 
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(8);
        registration.setInterceptors(presenceChannelInterceptor());
    }

	/**
	 * Coonfigure the message broker for the connection
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry)
	{
		registry.enableStompBrokerRelay("jms.topic")
				.setRelayHost("localhost")
				.setRelayPort(61616);
		registry.setApplicationDestinationPrefixes("/app"); 
	}
}
