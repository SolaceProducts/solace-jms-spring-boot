package com.solace.labs.spring.boot.autoconfigure;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.property.JMSProperties;


@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, SolConnectionFactory.class})
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJmsAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoConfiguration.class);

	@Autowired
	private SolaceJmsProperties properties;

	@Bean
	public SolConnectionFactoryImpl connectionFactory() {
	    
	    try {
	        JMSProperties props;
            props = new JMSProperties((Hashtable<?,?>)null);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);
            cf.setHost(properties.getHost());
            cf.setUsername(properties.getClientUsername());
            cf.setPassword(properties.getClientPassword());
            cf.setVPN(properties.getMsgVpn());
            cf.setDirectTransport(properties.isDirectTransport());       
    	    
            return cf;
	    } catch (Exception ex) {
        
	        logger.error("Exception found during Solace Connection Factory creation.", ex);
	        
            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-<version>.jar "
                    + "is the classpath", ex);
	    }
	}

}