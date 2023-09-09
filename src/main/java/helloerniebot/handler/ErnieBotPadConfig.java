package helloerniebot.handler;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ErnieBotPadConfig extends AnswerFluxPadder.Config {

    @Value("${upstream-service.ernie-bot.pad.head-delay}")
    int headPadDelay;

    @Value("${upstream-service.ernie-bot.pad.head-words}")
    String headPadWords;

    @Value("${upstream-service.ernie-bot.pad.head-catch-delay}")
    int headCatchPadDelay;

    @Value("${upstream-service.ernie-bot.pad.head-catch-words}")
    String headCatchPadWords;

    @Value("${upstream-service.ernie-bot.pad.next-delay}")
    int nextPadDelay;

    @Value("${upstream-service.ernie-bot.pad.next-words}")
    String nextPadWords;

    @Value("${upstream-service.ernie-bot.pad.next-catch-delay}")
    int nextCatchPadDelay;

    @Value("${upstream-service.ernie-bot.pad.next-catch-words}")
    String nextCatchPadWords;

    @PostConstruct
    public void propagate() {
        super.headPadDelay = headPadDelay;
        super.headPadWords = headPadWords;
        super.headCatchPadDelay = headCatchPadDelay;
        super.headCatchPadWords = headCatchPadWords;

        super.nextPadDelay = nextPadDelay;
        super.nextPadWords = nextPadWords;
        super.nextCatchPadDelay = nextCatchPadDelay;
        super.nextCatchPadWords = nextCatchPadWords;
    }
}
