package jndidemo;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory;

@Configuration
public class JndiProducerConfiguration {

    // Resource definitions: connection factory and queue destination
    @Value("${solace.jms.demoConnectionFactoryJndiName}")
    private String connectionFactoryJndiName;


    // Use from the jndi connection config
    @Autowired private JndiTemplate jndiTemplate;

    // Examples of other options to get JndiTemplate in a cloud environment with possibly multiple providers available:
    // Use this to access JndiTemplate of the first service found or look up a specific one by
    // SolaceServiceCredentials
    // @Autowired private SpringSolJmsJndiTemplateCloudFactory springSolJmsJndiTemplateCloudFactory;
    // @Autowired private SolaceServiceCredentials solaceServiceCredentials;
    // For backwards compatibility:
    // @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

    @Bean
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate);
        factoryBean.setJndiName(connectionFactoryJndiName);
        return factoryBean;
    }

    @Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		CachingConnectionFactory ccf = new CachingConnectionFactory((ConnectionFactory) connectionFactory().getObject());
		ccf.setSessionCacheSize(10);
		return ccf;
	}

    // Configure the destination resolver for the producer:
    // Here we are using JndiDestinationResolver for JNDI destinations
    // Other options include using DynamicDestinationResolver for non-JNDI destinations
    @Bean
    public JndiDestinationResolver jndiDestinationResolver() {
    	JndiDestinationResolver jdr = new JndiDestinationResolver();
        jdr.setCache(true);
        jdr.setJndiTemplate(jndiTemplate);
        return jdr;
    }

	@Bean
	public JmsTemplate producerJmsTemplate() {
		JmsTemplate jt = new JmsTemplate(cachingConnectionFactory());
		jt.setDeliveryPersistent(true);
		jt.setDestinationResolver(jndiDestinationResolver());
		return jt;
	}
}
