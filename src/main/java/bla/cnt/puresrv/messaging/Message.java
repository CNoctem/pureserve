package bla.cnt.puresrv.messaging;

public class Message<T> {

    private T content;

    public T getContent() {
        return content;
    }

    public Message setContent(T content) {
        this.content = content;
        return this;
    }

}
