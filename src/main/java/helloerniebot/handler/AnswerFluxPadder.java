package helloerniebot.handler;

import java.util.Timer;
import java.util.TimerTask;

import helloerniebot.handler.entity.AnswerParcel;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

@Slf4j
public class AnswerFluxPadder {

    private final FluxSink<AnswerParcel> emitter;
    private final Config config;

    private final Timer headPadTimer = new Timer();
    private final Timer headCatchPadTimer = new Timer();
    private Timer nextPadTimer = new Timer();
    private Timer nextCatchPadTimer = new Timer();

    public AnswerFluxPadder(FluxSink<AnswerParcel> emitter, Config config) {
        this.emitter = emitter;
        this.config = config;
    }

    public void onSubscribe() {
        if (config == null) {
            return;
        }

        if (config.headPadDelay > 0) {
            headPadTimer.schedule(new InsertHeadPadTask(), config.headPadDelay);
        }
    }

    public void onNext() {
        if (config == null) {
            return;
        }

        headPadTimer.cancel();
        headCatchPadTimer.cancel();

        nextPadTimer.cancel();
        nextPadTimer = new Timer();
        nextCatchPadTimer.cancel();
        nextCatchPadTimer = new Timer();

        if (config.nextPadDelay > 0) {
            nextPadTimer.schedule(new InsertNextPadTask(), config.nextPadDelay);
        }
    }

    public void onComplete() {
        if (config == null) {
            return;
        }

        headPadTimer.cancel();
        headCatchPadTimer.cancel();

        nextPadTimer.cancel();
        nextCatchPadTimer.cancel();
    }

    private AnswerParcel buildPadAnswer(String resultText, Boolean isEnd) {
        AnswerParcel answerParcel = new AnswerParcel();
        answerParcel.answerText = resultText;
        answerParcel.isPad = true;
        answerParcel.isEnd = isEnd;
        return answerParcel;
    }

    class InsertHeadPadTask extends TimerTask {

        @Override
        public void run() {
            emitter.next(buildPadAnswer(config.headPadWords, null));

            if (config.headCatchPadDelay > 0) {
                headCatchPadTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        emitter.next(buildPadAnswer(config.headCatchPadWords, true));
                        emitter.complete();
                    }
                }, config.headCatchPadDelay);
            }
        }
    }

    class InsertNextPadTask extends TimerTask {

        @Override
        public void run() {
            emitter.next(buildPadAnswer(config.nextPadWords, null));

            if (config.nextCatchPadDelay > 0) {
                nextCatchPadTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        emitter.next(buildPadAnswer(config.nextCatchPadWords, true));
                        emitter.complete();
                    }
                }, config.nextCatchPadDelay);
            }
        }
    }

    public static class Config {

        int headPadDelay;
        String headPadWords = "headPadWords";

        int headCatchPadDelay;
        String headCatchPadWords = "headCatchPadWords";

        int nextPadDelay;
        String nextPadWords = "nextPadWords";

        int nextCatchPadDelay;
        String nextCatchPadWords = "nextCatchPadWords";
    }
}