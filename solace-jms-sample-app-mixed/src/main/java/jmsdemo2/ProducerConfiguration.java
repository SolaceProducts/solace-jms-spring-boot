package jmsdemo2;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

@Configuration
public class ProducerConfiguration {
	@Value("${solace.jms.demoConnectionFactoryJndiName}")
	private String connectionFactoryJndiName;
	@Autowired private JndiTemplate jndiTemplate;

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

	// Example use of CachingConnectionFactory for the producer
	@Bean
	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(cachingConnectionFactory());
	}

	@Bean
	public JmsTemplate jmsPublisher() {
		JmsTemplate jmsTemplate =  new JmsTemplate(cachingConnectionFactory());
		jmsTemplate.setPubSubDomain(true);
		return jmsTemplate;
	}

	@Bean
	public JndiDestinationResolver jndiDestinationResolver() {
		JndiDestinationResolver jdr = new JndiDestinationResolver();
		jdr.setCache(true);
		jdr.setJndiTemplate(jndiTemplate);
		return jdr;
	}

	@Bean
	public JmsTemplate jndiJmsTemplate() {
		JmsTemplate jmsTemplate =  new JmsTemplate(cachingConnectionFactory());
		jmsTemplate.setDestinationResolver(jndiDestinationResolver());
		return jmsTemplate;
	}

	@Bean
	public JmsTemplate jndiJmsPublisher() {
		JmsTemplate jmsTemplate =  new JmsTemplate(cachingConnectionFactory());
		jmsTemplate.setDestinationResolver(jndiDestinationResolver());
		jmsTemplate.setPubSubDomain(true);
		return jmsTemplate;
	}

}
