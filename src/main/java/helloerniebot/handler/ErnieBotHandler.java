package helloerniebot.handler;

import java.time.Instant;

import helloerniebot.common.HttpUtil;
import helloerniebot.common.JsonUtil;
import helloerniebot.controller.entity.ErnieBotControllerResponse;
import helloerniebot.handler.entity.AnswerParcel;
import helloerniebot.mapper.entity.ErnieBotAccessTokenResponse;
import helloerniebot.mapper.entity.ErnieBotRequest;
import helloerniebot.mapper.entity.ErnieBotResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ErnieBotHandler {

    @Value("${upstream-service.ernie-bot.token-url-template}")
    String tokenUrlTemplate;

    @Value("${upstream-service.ernie-bot.client-id}")
    String clientId;

    @Value("${upstream-service.ernie-bot.client-secret}")
    String clientSecret;

    @Value("${upstream-service.ernie-bot.completions-url-template}")
    String completionsUrlTemplate;

    @Value("${upstream-service.ernie-bot.access-token}")
    String accessToken;

    private final ErnieBotPadConfig padConfig;

    private final ErnieBotMessageManager messageManager;

    @Autowired
    public ErnieBotHandler(ErnieBotPadConfig padConfig, ErnieBotMessageManager messageManager) {
        this.padConfig = padConfig;
        this.messageManager = messageManager;
    }

    public void execute(ErnieBotHandlerContext context) {
        if (accessToken == null) {
            accessToken = doRequestAccessToken();
        }

        messageManager.loadHistory(context);
        messageManager.addUserMessage(context, context.request.queryText);

        ErnieBotRequest ernieBotRequest = new ErnieBotRequest();
        ernieBotRequest.messages = context.messages;
        ernieBotRequest.stream = true;

        Flux<AnswerParcel> answerFlux = Flux.create(emitter -> {
            ErnieBotFluxTransformer transformer = new ErnieBotFluxTransformer(emitter, context, messageManager, padConfig);
            Flux<ErnieBotResponse> rawResponseFlux = doRequest(context, ernieBotRequest);
            rawResponseFlux.subscribe(transformer);
            emitter.onDispose(transformer);
        });

        context.responseFlux = answerFlux.mapNotNull(answerParcel -> {
            if (answerParcel == null) {
                return null;
            }
            ErnieBotControllerResponse response = new ErnieBotControllerResponse();
            response.answerText = answerParcel.answerText;
            response.isPad = answerParcel.isPad;
            response.isEnd = answerParcel.isEnd;
            response.trace = buildTrace(context, answerParcel, ernieBotRequest);
            return response;
        });
    }

    private String doRequestAccessToken() {
        String url = tokenUrlTemplate.replace("{client_id}", clientId)
                .replace("{client_secret}", clientSecret);

        String responsePayloadString = HttpUtil.httpGet(url);
        ErnieBotAccessTokenResponse accessTokenResponse = JsonUtil.jacksonDeserialize(responsePayloadString, ErnieBotAccessTokenResponse.class);
        return accessTokenResponse.access_token;
    }

    private Flux<ErnieBotResponse> doRequest(ErnieBotHandlerContext context, ErnieBotRequest request) {
        String url = completionsUrlTemplate.replace("{access_token}", accessToken);
        WebClient webClient = HttpUtil.buildWebClient(url);

        log.info("sending ErnieBotRequest: {}, elapsed: {}", JsonUtil.jacksonSerialize(request), context.elapsed());
        return webClient.post()
                .body(BodyInserters.fromValue(request))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(ErnieBotResponse.class)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("error", e);
                    throw new RuntimeException("error", e);
                });
    }

    private Trace buildTrace(ErnieBotHandlerContext context, AnswerParcel answer, ErnieBotRequest req) {
        Trace trace = new Trace();

        trace.elapsed = context.elapsed();
        if (context.lastTime != null) {
            trace.elapsedSinceLast = Instant.now().toEpochMilli() - context.lastTime.toEpochMilli();
        }
        context.lastTime = Instant.now();

        trace.ernieBot = answer.trace;
        if (answer.index != null && answer.index == 0) {
            trace.ernieBot.rawRequest = req;
        }

        return trace;
    }
}
