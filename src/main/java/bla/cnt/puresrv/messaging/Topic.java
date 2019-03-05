package bla.cnt.puresrv.messaging;

import java.util.ArrayList;
import java.util.List;

public class Topic {

    private final String name;
    private List<Subscriber> subscribers = new ArrayList<>();

    Topic(String name) {
        this.name = name;
    }

    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    public void publish(Message<?> message) {
        subscribers.forEach(s -> s.receive(this, message));
    }

    public String getName() {
        return name;
    }
}
