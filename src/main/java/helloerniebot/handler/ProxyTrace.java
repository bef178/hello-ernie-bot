package helloerniebot.handler;

import java.io.Serializable;

public class ProxyTrace implements Serializable {

    public String provider;

    public Object rawRequest;

    public Object rawResponse;

    public Long rawLatency;
}
