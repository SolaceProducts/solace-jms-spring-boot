package com.solace.spring.boot.autoconfigure;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.jndi.JndiTemplate;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;

abstract class SolaceJndiAutoConfigurationBase implements SpringSolJmsJndiTemplateCloudFactory {

    private static final Logger logger = LoggerFactory.getLogger(SolaceJndiAutoConfigurationBase.class);
    private SolaceJmsProperties properties;

    SolaceJndiAutoConfigurationBase(SolaceJmsProperties properties) {
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
    public JndiTemplate getJndiTemplate() {
        return getJndiTemplate(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public JndiTemplate getJndiTemplate(String id) {
        SolaceServiceCredentials solaceServiceCredentials = findSolaceServiceCredentialsById(id);
        return solaceServiceCredentials == null ? null : getJndiTemplate(solaceServiceCredentials);
    }

    @Override
    public JndiTemplate getJndiTemplate(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            SolaceServiceCredentials credentials = solaceServiceCredentials != null ?
                    solaceServiceCredentials : new SolaceServiceCredentialsImpl();

            Properties env = new Properties();
            env.putAll(properties.getApiProperties());
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");

            env.put(InitialContext.PROVIDER_URL, credentials.getJmsJndiUri() != null ?
                    credentials.getJmsJndiUri() : properties.getHost());
            env.put(Context.SECURITY_PRINCIPAL,
                    credentials.getClientUsername() != null && credentials.getMsgVpnName() != null ?
                            credentials.getClientUsername() + '@' + credentials.getMsgVpnName() :
                            properties.getClientUsername() + '@' + properties.getMsgVpn());

            env.put(Context.SECURITY_CREDENTIALS, credentials.getClientPassword() != null ?
                    credentials.getClientPassword() : properties.getClientPassword());

            JndiTemplate jndiTemplate = new JndiTemplate();
            jndiTemplate.setEnvironment(env);
            return jndiTemplate;
        } catch (Exception ex) {
            logger.error("Exception found during Solace JNDI Initial Context creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "JNDI Initial Context, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
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
}
