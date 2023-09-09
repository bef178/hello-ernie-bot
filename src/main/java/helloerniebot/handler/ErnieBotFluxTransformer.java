package helloerniebot.handler;

import java.time.Instant;

import helloerniebot.common.JsonUtil;
import helloerniebot.handler.entity.AnswerParcel;
import helloerniebot.mapper.entity.ErnieBotRequest;
import helloerniebot.mapper.entity.ErnieBotResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

@Slf4j
public class ErnieBotFluxTransformer implements Subscriber<ErnieBotResponse>, Disposable {

    private final FluxSink<AnswerParcel> emitter;
    private final ErnieBotHandlerContext context;
    private final ErnieBotRequest req;
    private final ErnieBotMessageManager ernieBotMessageManager;
    private final AnswerFluxPadder padder;

    private Subscription subscription;

    private Instant startTime;

    private final StringBuffer answerBuffer = new StringBuffer();

    public ErnieBotFluxTransformer(FluxSink<AnswerParcel> emitter, ErnieBotHandlerContext context, ErnieBotRequest req, ErnieBotMessageManager ernieBotMessageManager, AnswerFluxPadder.Config padConfig) {
        this.emitter = emitter;
        this.context = context;
        this.req = req;
        this.ernieBotMessageManager = ernieBotMessageManager;
        this.padder = padConfig == null ? null : new AnswerFluxPadder(emitter, padConfig);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.startTime = Instant.now();
        subscription.request(1);
        if (padder != null) {
            padder.onSubscribe();
        }
    }

    @Override
    public void onNext(ErnieBotResponse resp) {
        log.info("onNext: {}", JsonUtil.jacksonSerialize(resp));
        subscription.request(1);
        if (padder != null) {
            padder.onNext();
        }

        String partialAnswerText = resp.result;
        if (partialAnswerText != null && !partialAnswerText.isEmpty()) {
            answerBuffer.append(partialAnswerText);
        }

        AnswerParcel answerParcel = buildAnswer(resp);
        emitter.next(answerParcel);
    }

    protected AnswerParcel buildAnswer(ErnieBotResponse resp) {
        AnswerParcel answerParcel = new AnswerParcel();
        answerParcel.answerText = resp.result;
        answerParcel.index = resp.sentence_id;
        answerParcel.isEnd = (resp.is_end != null && !resp.is_end) ? null : resp.is_end;
        answerParcel.trace = new ProxyTrace();
        if (answerParcel.index != null && answerParcel.index == 0) {
            answerParcel.trace.rawRequest = req;
        }
        answerParcel.trace.rawResponse = resp;
        answerParcel.trace.rawLatency = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        return answerParcel;
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("onError", throwable);
        // save current answer if any error
        ernieBotMessageManager.addAssistantMessage(context, answerBuffer.toString());
        ernieBotMessageManager.saveHistory(context);
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        log.info("onComplete");
        ernieBotMessageManager.addAssistantMessage(context, answerBuffer.toString());
        ernieBotMessageManager.saveHistory(context);
        emitter.complete();
    }

    @Override
    public void dispose() {
        subscription.cancel();
        padder.onComplete();
    }

    @Override
    public boolean isDisposed() {
        return Disposable.super.isDisposed();
    }
}