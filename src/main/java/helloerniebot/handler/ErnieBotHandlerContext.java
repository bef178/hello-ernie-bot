package helloerniebot.handler;

import java.time.Instant;
import java.util.List;

import helloerniebot.controller.entity.ErnieBotControllerRequest;
import helloerniebot.mapper.entity.ErnieBotMessage;

public class ErnieBotHandlerContext {

    private final Instant startTime;

    private Instant lastTime;

    public final ErnieBotControllerRequest request;

    public List<ErnieBotMessage> messages;

    public ErnieBotHandlerContext(ErnieBotControllerRequest request) {
        this.startTime = Instant.now();
        this.request = request;
    }

    public long elapsed() {
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }

    public long elapsedSinceLastAndUpdate() {
        if (lastTime == null) {
            lastTime = startTime;
        }
        Instant now = Instant.now();
        long elapsedSinceLast = now.toEpochMilli() - lastTime.toEpochMilli();
        lastTime = now;
        return elapsedSinceLast;
    }
}
