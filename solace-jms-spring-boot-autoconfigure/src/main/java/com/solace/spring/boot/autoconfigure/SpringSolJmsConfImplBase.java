package com.solace.spring.boot.autoconfigure;

import com.solace.services.loader.model.SolaceServiceCredentials;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.property.JMSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Properties;

abstract class SpringSolJmsConfImplBase {
    private static final Logger logger = LoggerFactory.getLogger(SpringSolJmsConfImplBase.class);
    private SolaceJmsProperties properties;

    SpringSolJmsConfImplBase(SolaceJmsProperties properties) {
        this.properties = properties;
    }

    SolConnectionFactoryImpl getSolConnectionFactory(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Hashtable<String, String> ht = new Hashtable<>(properties.getApiProperties());
            JMSProperties props = new JMSProperties(ht);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);

            cf.setHost(solaceServiceCredentials.getSmfHost() != null ?
                    solaceServiceCredentials.getSmfHost() : properties.getHost());

            cf.setVPN(solaceServiceCredentials.getMsgVpnName() != null ?
                    solaceServiceCredentials.getMsgVpnName() : properties.getMsgVpn());

            cf.setUsername(solaceServiceCredentials.getClientUsername() != null ?
                    solaceServiceCredentials.getClientUsername() : properties.getClientUsername());

            cf.setPassword(solaceServiceCredentials.getClientPassword() != null ?
                    solaceServiceCredentials.getClientPassword() : properties.getClientPassword());

            cf.setDirectTransport(properties.isDirectTransport());
            return cf;
        } catch (Exception ex) {
            logger.error("Exception found during Solace Connection Factory creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }

    JndiTemplate getJndiTemplate(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Properties env = new Properties();
            env.putAll(properties.getApiProperties());
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");

            env.put(InitialContext.PROVIDER_URL, solaceServiceCredentials.getJmsJndiUri() != null ?
                    solaceServiceCredentials.getJmsJndiUri() : properties.getHost());
            env.put(Context.SECURITY_PRINCIPAL,
                    solaceServiceCredentials.getClientUsername() != null && solaceServiceCredentials.getMsgVpnName() != null ?
                            solaceServiceCredentials.getClientUsername() + '@' + solaceServiceCredentials.getMsgVpnName() :
                            properties.getClientUsername() + '@' + properties.getMsgVpn());

            env.put(Context.SECURITY_CREDENTIALS, solaceServiceCredentials.getClientPassword() != null ?
                    solaceServiceCredentials.getClientPassword() :
                    properties.getClientPassword());

            JndiTemplate jndiTemplate = new JndiTemplate();
            jndiTemplate.setEnvironment(env);
            return jndiTemplate;
        } catch (Exception ex) {
            logger.error("Exception found during Solace JNDI Initial Context creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "JNDI Initial Context, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }
}
