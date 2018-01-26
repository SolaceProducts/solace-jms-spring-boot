package jndidemo;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class JndiDemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(JndiDemoApplication.class, args);
    }

    @Service
    static class MessageProducer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

        @Autowired
        JmsTemplate producerJmsTemplate;

        @Value("${solace.jms.demoProducerQueueJndiName}")
        private String queueJndiName;
        
        @Override
        public void run(String... strings) throws Exception {
            String msg = "Hello World";
            logger.info("============= Sending " + msg);
            this.producerJmsTemplate.convertAndSend(queueJndiName, msg);
        }
    }

    @Component
    static class MessageHandler {
 
        private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

        @Autowired
        JmsTemplate producerJmsTemplate;

        // Retrieve the name of the queue from the application.properties file
        @JmsListener(destination = "${solace.jms.demoConsumerQueueJndiName}")
        public void processMsg(Message msg) {
        	StringBuffer msgAsStr = new StringBuffer("============= Received \nHeaders:");
        	MessageHeaders hdrs = msg.getHeaders();
        	msgAsStr.append("\nUUID: "+hdrs.getId());
        	msgAsStr.append("\nTimestamp: "+hdrs.getTimestamp());
        	Iterator<String> keyIter = hdrs.keySet().iterator();
        	while (keyIter.hasNext()) {
        		String key = keyIter.next();
            	msgAsStr.append("\n"+key+": "+hdrs.get(key));        		
        	}
        	msgAsStr.append("\nPayload: "+msg.getPayload());
            logger.info(msgAsStr.toString());
        }
    }
}
