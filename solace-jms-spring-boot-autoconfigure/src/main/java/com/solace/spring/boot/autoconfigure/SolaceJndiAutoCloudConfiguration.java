/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.solace.spring.boot.autoconfigure;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;

import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory;
import com.solacesystems.jndi.SolJNDIInitialContextFactory;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, SolJNDIInitialContextFactory.class, CloudFactory.class })
@ConditionalOnMissingBean(JndiTemplate.class)
@Conditional(CloudCondition.class)
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJndiAutoCloudConfiguration extends SpringSolJmsConfCloudFactoryImplBase implements SpringSolJmsJndiTemplateCloudFactory {

	private static final Logger logger = LoggerFactory.getLogger(SolaceJndiAutoCloudConfiguration.class);

	@Autowired
	private SolaceJmsProperties properties;

	public JndiTemplate getJndiTemplate(SolaceMessagingInfo solacemessaging) {
		try {
            Properties env = new Properties();
            env.putAll(properties.getApiProperties());
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");

            
			// Use provided cloud information where available
			if (solacemessaging.getJmsJndiUri() != null)
				env.put(InitialContext.PROVIDER_URL, solacemessaging.getJmsJndiUri());
			else
				env.put(InitialContext.PROVIDER_URL, properties.getHost());
			
			if (solacemessaging.getMsgVpnName() != null && 
					solacemessaging.getClientUsername() != null)
				env.put(Context.SECURITY_PRINCIPAL, solacemessaging.getClientUsername() +
						'@' + solacemessaging.getMsgVpnName());
			else
				env.put(Context.SECURITY_PRINCIPAL, properties.getClientUsername() + '@' + properties.getMsgVpn());

			if (solacemessaging.getClientPassword() != null)
				env.put(Context.SECURITY_CREDENTIALS, solacemessaging.getClientPassword());
			else
				env.put(Context.SECURITY_CREDENTIALS, properties.getClientPassword());

            JndiTemplate jndiTemplate = new JndiTemplate();
            jndiTemplate.setEnvironment(env);
            
            return jndiTemplate;
		} catch (Exception ex) {
			logger.error("Exception found during Solace JndiTemplate creation.", ex);
			throw new IllegalStateException("Unable to create Solace JndiTemplate", ex);
		}
	}

	@Bean
	public JndiTemplate getJndiTemplate() {
		return getJndiTemplate(findFirstSolaceMessagingInfo());
	}
}