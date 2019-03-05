package bla.cnt.puresrv.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Emcue {

    INSTANCE;

    private static Topic devnull = new Topic("devnull");

    private static Map<String, Topic> topicList = new HashMap<>();

    private List<EmcueListener> listeners = new ArrayList<>();

    public Topic createTopicIfAbsent(String name) {
        return topicList.computeIfAbsent(name, n -> {
            Topic newTopic = new Topic(name);
            listeners.forEach(l -> l.topicCreated(newTopic));
            return newTopic;
        });
    }

    public Topic getTopicByName(String name) {
        Topic t = topicList.get(name);
        return t != null ? t : devnull;
    }

    public static Map<String, Topic> getTopicList() {
        return topicList;
    }

    public void addListener(EmcueListener l) {
        listeners.add(l);
    }

}
