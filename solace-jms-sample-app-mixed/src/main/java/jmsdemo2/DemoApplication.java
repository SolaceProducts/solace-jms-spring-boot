package jmsdemo2;

import java.util.Iterator;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SubscriptionNameProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Service
    static class MessageProducer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

        @Autowired()
        @Qualifier("jndiJmsTemplate")
        private JmsTemplate jmsTemplate;

      @Autowired()
      @Qualifier("jmsPublisher")
      private JmsTemplate publisher;

      @Autowired()
      @Qualifier("jndiJmsPublisher")
      private JmsTemplate jndiPublisher;

        /*
        For backwards compatibility:
        - As before, these exist only in the specific scenario where the app is deployed in Cloud Foundry.*/
        @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

        @Value("${solace.jms.demoConsumerQueueJndiName}")
        private String queueName;

      @Value("${solace.jms.demoTopicName}")
      private String topicName;

      @Value("${solace.jms.demoTopicName2}")
      private String topicName2;

        @Override
        public void run(String... strings) throws Exception {
            String msg = "Hello World";

          logger.info("============= Sending to topic " + topicName + ", msg=" + msg);
          this.publisher.convertAndSend(topicName, msg);

          logger.info("============= Sending to queue " + queueName  +", msg="+ msg);
          try {
            this.jmsTemplate.convertAndSend(queueName, msg);
          } catch (Exception e) {
            logger.error("failed to send msg to queue", e);
          }

          logger.info("============= Sending to topic " + topicName2 +", msg="+ msg);
          try {
            this.publisher.convertAndSend(topicName2, msg);
          } catch (Exception e) {
            logger.error("failed to send msg to topic endpoint", e);
          }
        }
    }


  @Component
  static class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    // Retrieve the name of the queue from the application.properties file
    @JmsListener(destination = "${solace.jms.demoConsumerQueueJndiName}",containerFactory="cFactory")
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


  @Component
  static class TopicMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(TopicMessageHandler.class);

    // Retrieve the name of the queue from the application.properties file
    @JmsListener(destination = "${solace.jms.demoTopicName2}",containerFactory="tFactory", concurrency="1", subscription="${solace.jms.demoSubscriptionName}")
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
