package jndidemo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@EnableJms
public class JndiConsumerConfiguration {

	// Resource definitions: connection factory and queue destination
    @Value("${solace.jms.demoConnectionFactoryJndiName}")
    private String connectionFactoryJndiName;

    private static final Logger logger = LoggerFactory.getLogger(JndiConsumerConfiguration.class);

    @Autowired
    JndiTemplate jndiTemplate;
    
    @Bean
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate);
        factoryBean.setJndiName(connectionFactoryJndiName);
        return factoryBean;
    }
    
    // Configure the destination resolver for the consumer:
    // Here we are using JndiDestinationResolver for JNDI destinations
    // Other options include using DynamicDestinationResolver for non-JNDI destinations
    @Bean
    public JndiDestinationResolver jndiDestinationResolver() {
    	JndiDestinationResolver jdr = new JndiDestinationResolver();
        jdr.setCache(true);
        jdr.setJndiTemplate(jndiTemplate);
        return jdr;
    }
    
	// Example configuration of the JmsListenerContainerFactory
    @Bean
    public DefaultJmsListenerContainerFactory cFactory(DemoErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory((ConnectionFactory) connectionFactory().getObject());
        factory.setDestinationResolver(jndiDestinationResolver());
        factory.setErrorHandler(errorHandler);
        factory.setConcurrency("3-10");
       return factory;
    }

    @Service
    public class DemoErrorHandler implements ErrorHandler{   

        @Override
        public void handleError(Throwable t) {
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
        	PrintStream ps = new PrintStream(os);
        	t.printStackTrace(ps);
        	try {
				String output = os.toString("UTF8");
	            logger.error("============= Error processing message: " + t.getMessage()+"\n"+output);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
 
        }
    }

}
