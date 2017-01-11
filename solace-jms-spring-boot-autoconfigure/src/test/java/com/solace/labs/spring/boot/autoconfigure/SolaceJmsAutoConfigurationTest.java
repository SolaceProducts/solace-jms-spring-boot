package com.solace.labs.spring.boot.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.solacesystems.jms.SolConnectionFactoryImpl;

public class SolaceJmsAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	@After
	public void tearDown() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void defaultNativeConnectionFactory() {
		load(EmptyConfiguration.class, "");
		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
		SolConnectionFactoryImpl connectionFactory = this.context
				.getBean(SolConnectionFactoryImpl.class);
		assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
        assertEquals("tcp://localhost", connectionFactory.getHost());
        assertEquals("default", connectionFactory.getVPN());
        assertNull(connectionFactory.getUsername());
        assertNull(connectionFactory.getPassword());
        assertFalse(connectionFactory.getDirectTransport());
	}

	@Test
	public void customNativeConnectionFactory() {
		load(EmptyConfiguration.class, "solace.jms.host=192.168.1.80:55500",
				"solace.jms.clientUsername=bob", "solace.jms.clientPassword=password",
				"solace.jms.msgVpn=newVpn");
		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
		SolConnectionFactoryImpl connectionFactory = this.context
				.getBean(SolConnectionFactoryImpl.class);
		assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
		assertEquals("tcp://192.168.1.80:55500", connectionFactory.getHost());
        assertEquals("newVpn", connectionFactory.getVPN());
        assertEquals("bob", connectionFactory.getUsername());
        assertEquals("password", connectionFactory.getPassword());
        assertFalse(connectionFactory.getDirectTransport());
	}



	@Configuration
	static class EmptyConfiguration {}

	private void load(Class<?> config, String... environment) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.register(config);
		applicationContext.register(SolaceJmsAutoConfiguration.class,
				JmsAutoConfiguration.class);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
