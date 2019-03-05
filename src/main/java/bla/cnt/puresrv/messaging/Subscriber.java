package bla.cnt.puresrv.messaging;

public interface Subscriber {

    void receive(Topic topic, Message<?> message);

}
