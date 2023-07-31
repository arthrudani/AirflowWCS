package com.daifukuoc.wrxj.custom.ebs.communication;

public enum ConnConfigKeys {
    PORT_NAME("PortName"),
    INTEGRATOR_NAME("IntegratorName"),
    IP_ADDRESS("IPAddress"),
    PORT_NUMBER("PortNumber"),
    KEEP_ALIVE_INTERVAL("KeepAliveInterval"),
    RETRY_INTERVAL("RetryInterval"),
    ACK_TIMEOUT("AckTimeout"),
    ACK_MAX_RETRY("AckMaxRetry"),
    USE_STXETX("UseStxEtx");

    private final String key;

    ConnConfigKeys(String key) {
        this.key = key;
    }

    public String getValue() {
        return key;
    }
}
