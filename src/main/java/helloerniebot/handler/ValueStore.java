package helloerniebot.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ValueStore {

    private static final Map<String, Object> store = new LinkedHashMap<>();

    public boolean hasValue(String key) {
        return store.containsKey(key);
    }

    public Object loadValue(String key) {
        return store.get(key);
    }

    /**
     * return old value if exists
     */
    public Object saveValue(String key, Object value) {
        return store.put(key, value);
    }
}
