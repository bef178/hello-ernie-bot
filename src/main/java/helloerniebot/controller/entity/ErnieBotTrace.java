package helloerniebot.controller.entity;

import java.io.Serializable;

import helloerniebot.handler.ProxyTrace;

public class ErnieBotTrace implements Serializable {

    public Long elapsed;

    public Long elapsedSinceLast;

    public ProxyTrace ernieBot;
}
