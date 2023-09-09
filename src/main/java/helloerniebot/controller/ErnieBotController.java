package helloerniebot.controller;

import java.time.Instant;

import helloerniebot.controller.entity.ErnieBotControllerRequest;
import helloerniebot.controller.entity.ErnieBotControllerResponse;
import helloerniebot.handler.ErnieBotHandlerContext;
import helloerniebot.handler.ErnieBotHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import helloerniebot.common.JsonUtil;

@RestController
@Slf4j
public class ErnieBotController {

    private final ErnieBotHandler ernieBotHandler;

    @Autowired
    public ErnieBotController(ErnieBotHandler ernieBotHandler) {
        this.ernieBotHandler = ernieBotHandler;
    }

    @PostMapping(value = { "/api/ernie-bot" }, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ErnieBotControllerResponse> onRequest(@RequestBody ErnieBotControllerRequest request) {
        log.info("Instrumentation: request: {}, time: {}", JsonUtil.jacksonSerialize(request), Instant.now());
        return execute(request).index().map(p -> {
            Long index = p.getT1();
            ErnieBotControllerResponse response = p.getT2();
            log.info("Instrumentation: response: {}, index: {}, time: {}", JsonUtil.jacksonSerialize(response), index, Instant.now());
            return response;
        });
    }

    private Flux<ErnieBotControllerResponse> execute(ErnieBotControllerRequest request) {
        try {
            validateRequest(request);
        } catch (IllegalArgumentException e) {
            ErnieBotControllerResponse fallbackResponse = new ErnieBotControllerResponse();
            fallbackResponse.errorCode = 400;
            fallbackResponse.errorMessage = e.getMessage();
            return Flux.just(fallbackResponse);
        }

        try {
            ErnieBotHandlerContext context = new ErnieBotHandlerContext(request);
            ernieBotHandler.execute(context);
            return context.responseFlux;
        } catch (Exception e) {
            log.error("Failed to execute", e);
            ErnieBotControllerResponse fallbackResponse = new ErnieBotControllerResponse();
            fallbackResponse.errorCode = 500;
            fallbackResponse.errorMessage = e.getMessage();
            return Flux.just(fallbackResponse);
        }
    }

    private void validateRequest(ErnieBotControllerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request should not be null");
        }

        if (request.requestId == null || request.requestId.isEmpty()) {
            throw new IllegalArgumentException("requestId should not be null or empty");
        }

        if (request.queryText == null || request.queryText.isEmpty()) {
            throw new IllegalArgumentException("queryText should not be null or empty");
        }
    }
}
