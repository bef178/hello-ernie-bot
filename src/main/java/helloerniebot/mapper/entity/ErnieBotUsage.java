package helloerniebot.mapper.entity;

import lombok.Data;

@Data
public class ErnieBotUsage {

    public int prompt_tokens;

    public int completion_tokens;

    public int total_tokens;
}
