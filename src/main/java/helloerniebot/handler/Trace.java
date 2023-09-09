package helloerniebot.handler;

import java.io.Serializable;

public class Trace implements Serializable {

    public Long elapsed;

    public Long elapsedSinceLast;

    public ProxyTrace ernieBot;
}
