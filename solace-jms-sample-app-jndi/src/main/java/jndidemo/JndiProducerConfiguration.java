package jndidemo;

import javax.jms.ConnectionFactory;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

@Configuration
public class JndiProducerConfiguration {

    // Resource definitions: connection factory and queue destination
    @Value("${solace.jms.demoConnectionFactoryJndiName}")
    private String connectionFactoryJndiName;


    // Use from the jndi connection config
    @Autowired private JndiTemplate jndiTemplate;
    @Autowired private SpringSolJmsJndiTemplateCloudFactory springSolJmsJndiTemplateCloudFactory;
    @Autowired private SolaceServiceCredentials solaceServiceCredentials;

    /*
        For backwards compatibility:
        - As before, these exist only in the specific scenario where the app is deployed in Cloud Foundry.*/
    @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

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
