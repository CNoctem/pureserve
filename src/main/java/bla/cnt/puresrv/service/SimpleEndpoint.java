package bla.cnt.puresrv.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SimpleEndpoint implements HttpHandler {

    private final String contextRoot;

    private OutputStream out;
    private HttpExchange exchange;
    private int status;
    private String response;

    public SimpleEndpoint(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    protected abstract void handleExchange();

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        out = exchange.getResponseBody();
        handleExchange();
    }

    protected SimpleEndpoint setStatus(int status) {
        this.status = status;
        return this;
    }

    protected SimpleEndpoint setResponse(String response) {
        this.response = response;
        return this;
    }

    protected void send() {
        try {
            exchange.sendResponseHeaders(status, response.length());
            out.write(response.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
