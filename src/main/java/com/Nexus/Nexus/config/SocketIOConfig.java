package com.Nexus.Nexus.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "socketio.server.enabled", havingValue = "true", matchIfMissing = true)
public class SocketIOConfig {
    
    @Value("${socketio.server.host:localhost}")
    private String host;
    
    @Value("${socketio.server.port:9092}")
    private Integer port;
    
    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        
        // Enable CORS
        config.setOrigin("*");
        
        // Configure connection settings
        config.setBossThreads(1);
        config.setWorkerThreads(100);
        config.setAllowCustomRequests(true);
        config.setUpgradeTimeout(10000);
        config.setPingTimeout(5000);
        config.setPingInterval(2000);
        
        return new SocketIOServer(config);
    }
}
