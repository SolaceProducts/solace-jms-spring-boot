
package com.solace.labs.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("solace.jms")
public class SolaceJmsProperties {

     /**
     * Solace Message Router Host address. Port is optional and intelligently defaulted by the Solace JMS API.
     */
    private String host = "localhost";

    /**
     * Solace Message Router Message-VPN
     */
    private String msgVpn = "default";
    
    /**
     * Solace Message Router Client Username
     */
    private String clientUsername;
    
    /**
     * Solace Message Router Client Password
     */
    private String clientPassword;
    
    /**
     * A flag to control whether or not to enable the Solace direct transport JMS feature. Enabling this feature allows for higher performance but limits the JMS features that are supported.
     */
    private boolean directTransport = false;
    
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public String getMsgVpn() {
        return msgVpn;
    }

    public void setMsgVpn(String msgVpn) {
        this.msgVpn = msgVpn;
    }

    public boolean isDirectTransport() {
        return directTransport;
    }

    public void setDirectTransport(boolean directTransport) {
        this.directTransport = directTransport;
    }

    

}