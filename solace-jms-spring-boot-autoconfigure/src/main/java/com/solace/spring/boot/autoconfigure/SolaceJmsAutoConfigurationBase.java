package com.solace.spring.boot.autoconfigure;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import com.solacesystems.jms.property.JMSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.Hashtable;
import java.util.List;

abstract class SolaceJmsAutoConfigurationBase implements SpringSolJmsConnectionFactoryCloudFactory {
    private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoConfigurationBase.class);

    private SolaceJmsProperties properties;

    SolaceJmsAutoConfigurationBase(SolaceJmsProperties properties) {
        this.properties = properties;
    }

    abstract SolaceServiceCredentials findFirstSolaceServiceCredentialsImpl();

    @Override
    public abstract List<SolaceServiceCredentials> getSolaceServiceCredentials();

    @Bean
    @Override
    public SolaceServiceCredentials findFirstSolaceServiceCredentials() {
        return findFirstSolaceServiceCredentialsImpl();
    }

    @Bean
    @Override
    public SolConnectionFactory getSolConnectionFactory() {
        return getSolConnectionFactory(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public SolConnectionFactory getSolConnectionFactory(String id) {
        SolaceServiceCredentials solaceServiceCredentials = findSolaceServiceCredentialsById(id);
        return solaceServiceCredentials == null ? null : getSolConnectionFactory(solaceServiceCredentials);
    }

    @Override
    public SolConnectionFactory getSolConnectionFactory(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Hashtable<String, String> ht = new Hashtable<>(properties.getApiProperties());
            JMSProperties props = new JMSProperties(ht);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);
            SolaceServiceCredentials credentials = solaceServiceCredentials != null ?
                    solaceServiceCredentials : new SolaceServiceCredentialsImpl();

            cf.setHost(credentials.getSmfHost() != null ?
                    credentials.getSmfHost() : properties.getHost());

            cf.setVPN(credentials.getMsgVpnName() != null ?
                    credentials.getMsgVpnName() : properties.getMsgVpn());

            cf.setUsername(credentials.getClientUsername() != null ?
                    credentials.getClientUsername() : properties.getClientUsername());

            cf.setPassword(credentials.getClientPassword() != null ?
                    credentials.getClientPassword() : properties.getClientPassword());

            cf.setDirectTransport(properties.isDirectTransport());
            return cf;
        } catch (Exception ex) {
            logger.error("Exception found during Solace Connection Factory creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }

    @Override @Deprecated
    public List<SolaceMessagingInfo> getSolaceMessagingInfos() {
        return null;
    }

    private SolaceServiceCredentials findSolaceServiceCredentialsById(String id) {
        for (SolaceServiceCredentials credentials : getSolaceServiceCredentials())
            if (credentials.getId().equals(id)) return credentials;
        return null;
    }

    void setProperties(SolaceJmsProperties properties) {
        this.properties = properties;
    }
}
