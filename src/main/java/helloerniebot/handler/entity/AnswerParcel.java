package helloerniebot.handler.entity;

import java.io.Serializable;

import helloerniebot.handler.ProxyTrace;

public class AnswerParcel implements Serializable {

    public String answerText;

    public Integer index;

    public Boolean isPad;

    public Boolean isEnd;

    public Integer errorCode;

    public String errorMessage;

    public ProxyTrace trace;
}
