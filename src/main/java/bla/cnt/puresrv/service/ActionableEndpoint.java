package bla.cnt.puresrv.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ActionableEndpoint implements HttpHandler {

    private final Action action;

    private OutputStream out;
    private HttpExchange exchange;
    private int status = -1;
    private String response = "";

    private Map<String, String> requestQuery;

    public ActionableEndpoint(Action action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        requestQuery = queryToMap(exchange.getRequestURI().getQuery());

        out = exchange.getResponseBody();
        action.act(this);
    }

    public String getParam(String param) {
        return requestQuery.get(param);
    }

    public interface Action {
        void act(ActionableEndpoint owner);
    }

    protected ActionableEndpoint setStatus(int status) {
        this.status = status;
        return this;
    }

    public ActionableEndpoint setResponse(String response) {
        this.response = response;
        return this;
    }

    public void redirect(String url) {
        exchange.getResponseHeaders().set("Location", url);
        status = 302;
        send();
    }

    public void sendFile(String filename) {
        response = fileToString(filename);
        send();
    }

    public void sendFile(String filename, Map<String, Object> vars) {
        String content = fileToString(filename);
        for (String var : vars.keySet())
            content = content.replace("${" + var + "}", vars.get(var).toString());
        response = content;
        send();
    }

    public void send() {
        try {
            exchange.sendResponseHeaders(status == -1 ? 200 : status, response.length());
            out.write(response.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> queryToMap(String query) {
        if (query == null) return new HashMap<>(1);
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private static String fileToString(String filename) {
        StringBuilder sb = new StringBuilder();
        try (InputStream stream = ActionableEndpoint.class
                .getClassLoader().getResourceAsStream(filename)) {

            InputStreamReader reader = new InputStreamReader(stream);
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
