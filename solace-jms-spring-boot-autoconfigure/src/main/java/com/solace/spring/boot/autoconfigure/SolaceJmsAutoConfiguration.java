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

import java.util.Hashtable;

import javax.jms.ConnectionFactory;

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

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.property.JMSProperties;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({SolaceJmsAutoCloudConfiguration.class, SolaceJndiAutoConfiguration.class})
@ConditionalOnClass({ ConnectionFactory.class, SolConnectionFactory.class })
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJmsAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoConfiguration.class);

    @Autowired
    private SolaceJmsProperties properties;
    private SolaceCredentialsLoader solaceServicesInfoLoader = new SolaceCredentialsLoader();

    @Bean
    public SolConnectionFactoryImpl connectionFactory() {
        return connectionFactory(findFirstSolaceServiceCredentials());
    }

    public SolConnectionFactoryImpl connectionFactory(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Hashtable<String, String> ht = new Hashtable<>(properties.getApiProperties());
            JMSProperties props = new JMSProperties(ht);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);

            if (solaceServiceCredentials.getSmfHosts() != null && !solaceServiceCredentials.getSmfHosts().isEmpty())
                cf.setHost(solaceServiceCredentials.getSmfHosts().get(0));
            else
                cf.setHost(properties.getHost());

            if (solaceServiceCredentials.getMsgVpnName() != null)
                cf.setVPN(solaceServiceCredentials.getMsgVpnName());
            else
                cf.setVPN(properties.getMsgVpn());

            if (solaceServiceCredentials.getClientUsername() != null)
                cf.setUsername(solaceServiceCredentials.getClientUsername());
            else
                cf.setUsername(properties.getClientUsername());

            if (solaceServiceCredentials.getClientPassword() != null)
                cf.setPassword(solaceServiceCredentials.getClientPassword());
            else
                cf.setPassword(properties.getClientPassword());

            cf.setDirectTransport(properties.isDirectTransport());

            return cf;
        } catch (Exception ex) {

            logger.error("Exception found during Solace Connection Factory creation.", ex);

            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }

    private SolaceServiceCredentials findFirstSolaceServiceCredentials() {
        SolaceServiceCredentials credentials = solaceServicesInfoLoader.getSolaceServiceInfo();
        return credentials != null ? credentials : new SolaceServiceCredentials();
    }

}