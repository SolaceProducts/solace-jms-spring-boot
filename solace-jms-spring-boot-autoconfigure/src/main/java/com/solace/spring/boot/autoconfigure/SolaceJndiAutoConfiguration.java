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

import com.solace.services.loader.SolaceCredentialsLoader;
import com.solace.services.loader.model.SolaceServiceCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;

import com.solacesystems.jndi.SolJNDIInitialContextFactory;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter(SolaceJndiAutoCloudConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, SolJNDIInitialContextFactory.class })
@ConditionalOnMissingBean(JndiTemplate.class)
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJndiAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SolaceJndiAutoConfiguration.class);

    @Autowired
    private SolaceJmsProperties properties;
    private SolaceCredentialsLoader solaceServicesInfoLoader = new SolaceCredentialsLoader();

    @Bean
    public JndiTemplate jndiTemplate() {
        return jndiTemplate(findFirstSolaceServiceCredentials());
    }

    public JndiTemplate jndiTemplate(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Properties env = new Properties();
            env.putAll(properties.getApiProperties());
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");

            if (solaceServiceCredentials.getJmsJndiUris() != null && !solaceServiceCredentials.getJmsJndiUris().isEmpty())
                env.put(InitialContext.PROVIDER_URL, solaceServiceCredentials.getJmsJndiUris().get(0));
            else
                env.put(InitialContext.PROVIDER_URL, properties.getHost());

            if (solaceServiceCredentials.getClientUsername() != null && solaceServiceCredentials.getMsgVpnName() != null)
                env.put(Context.SECURITY_PRINCIPAL,
                        solaceServiceCredentials.getClientUsername() + '@' + solaceServiceCredentials.getMsgVpnName());
            else
                env.put(Context.SECURITY_PRINCIPAL, properties.getClientUsername() + '@' + properties.getMsgVpn());

            if (solaceServiceCredentials.getClientPassword() != null)
                env.put(Context.SECURITY_CREDENTIALS, solaceServiceCredentials.getClientPassword());
            else
                env.put(Context.SECURITY_CREDENTIALS, properties.getClientPassword());

            JndiTemplate jndiTemplate = new JndiTemplate();
            jndiTemplate.setEnvironment(env);
            return jndiTemplate;
        } catch (Exception ex) {

            logger.error("Exception found during Solace JNDI Initial Context creation.", ex);

            throw new IllegalStateException("Unable to create Solace "
                    + "JNDI Initial Context, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }

    @Bean
    public SolaceServiceCredentials findFirstSolaceServiceCredentials() {
        SolaceServiceCredentials credentials = solaceServicesInfoLoader.getSolaceServiceInfo();
        return credentials != null ? credentials : new SolaceServiceCredentials();
    }

}
