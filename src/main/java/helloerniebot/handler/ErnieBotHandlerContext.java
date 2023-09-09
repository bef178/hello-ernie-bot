package helloerniebot.handler;

import java.time.Instant;
import java.util.List;

import helloerniebot.controller.entity.ErnieBotControllerRequest;
import helloerniebot.controller.entity.ErnieBotControllerResponse;
import helloerniebot.mapper.entity.ErnieBotMessage;
import reactor.core.publisher.Flux;

public class ErnieBotHandlerContext {

    public final Instant startTime;

    Instant lastTime;

    public final ErnieBotControllerRequest request;

    public Flux<ErnieBotControllerResponse> responseFlux;

    public List<ErnieBotMessage> messages;

    public ErnieBotHandlerContext(ErnieBotControllerRequest request) {
        this.startTime = Instant.now();
        this.request = request;
    }

    public long elapsed() {
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }
}
