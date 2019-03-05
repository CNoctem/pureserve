package bla.cnt.puresrv.service;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static Logger log = LoggerFactory.getLogger(Server.class);

    private static Map<Integer, Server> cache = new HashMap<>();

    public static Server serverAt(int port) {
        return cache.computeIfAbsent(port, p -> new Server(port));
    }

    private HttpServer srv;

    private Server(int port) {
        try {
            srv = HttpServer.create(new InetSocketAddress(port), 0);
            srv.setExecutor(null); // creates a default executor
            srv.start();
            log.info("Server at {} has been started.", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerEndpoint(String contextRoot, SimpleEndpoint e) {
        srv.createContext(contextRoot, e);
    }

    public void registerEndpoint(String contextRoot, ActionableEndpoint.Action a) {
        log.info("Registering endpoint at {}.", contextRoot);
        srv.createContext(contextRoot, new ActionableEndpoint(a));
        log.info("Registered endpoint at {}.", contextRoot);
    }

}
