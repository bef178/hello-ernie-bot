package helloerniebot.mapper.entity;

import java.util.List;

import lombok.Data;

@Data
public class ErnieBotRequest {

    public List<ErnieBotMessage> messages;

    public boolean stream;
}
