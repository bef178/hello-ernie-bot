package helloerniebot.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import helloerniebot.mapper.entity.ErnieBotMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErnieBotMessageManager {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private final ValueStore store;

    @Autowired
    public ErnieBotMessageManager(ValueStore valueStore) {
        this.store = valueStore;
    }

    public void loadHistory(ErnieBotHandlerContext context) {
        @SuppressWarnings("unchecked")
        List<ErnieBotMessage> history = (List<ErnieBotMessage>) store.loadValue(context.request.sessionId);
        if (history == null) {
            history = new LinkedList<>();
        }
        context.messages = history;
    }

    public void saveHistory(ErnieBotHandlerContext context) {
        Object ignored = store.saveValue(context.request.sessionId, context.messages);
    }

    public void addUserMessage(ErnieBotHandlerContext context, String text) {
        addToHistory(context, buildUserMessage(text));
    }

    public void addAssistantMessage(ErnieBotHandlerContext context, String text) {
        addToHistory(context, buildAssistantMessage(text));
    }

    private void addToHistory(ErnieBotHandlerContext context, ErnieBotMessage message) {
        final int MAX_TOKENS = 5000;

        List<ErnieBotMessage> history = context.messages;

        if (message == null || message.role == null) {
            return;
        }

        ErnieBotMessage last = null;
        if (!history.isEmpty()) {
            last = history.get(history.size() - 1);
        }

        if (last == null) {
            history.add(message);
        } else if (!Objects.equals(last.role, message.role)) {
            history.add(message);
        } else {
            history.clear();
            if (Objects.equals(ROLE_USER, message.role)) {
                history.add(message);
            }
        }

        int numTokens = history.stream().map(a -> a.content.length()).reduce(Integer::sum).orElse(0);
        while (numTokens > MAX_TOKENS) {
            for (int i = 0; i < 2; i++) {
                if (history.isEmpty()) {
                    break;
                }
                ErnieBotMessage one = history.remove(0);
                numTokens -= one.content.length();
            }
        }
    }

    private ErnieBotMessage buildUserMessage(String text) {
        ErnieBotMessage message = new ErnieBotMessage();
        message.role = ROLE_USER;
        message.content = text;
        return message;
    }

    private ErnieBotMessage buildAssistantMessage(String text) {
        ErnieBotMessage message = new ErnieBotMessage();
        message.role = ROLE_ASSISTANT;
        message.content = text;
        return message;
    }
}
