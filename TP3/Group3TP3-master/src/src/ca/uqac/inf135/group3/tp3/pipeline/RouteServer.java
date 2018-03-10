package ca.uqac.inf135.group3.tp3.pipeline;

import ca.uqac.inf135.group3.tp3.tools.HttpMethod;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RouteServer {
    private final Executor executor = Executors.newCachedThreadPool();
    private final int port;
    private HttpServer httpServer;
    private Map<String, UriRouter> uriRouters = new HashMap<>();
    private boolean valid;

    public RouteServer(int port) throws Exception {
        valid = true;

        this.port = port;

        //Try to create the server
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            valid = false;
            throw new Exception(String.format("Unable to open local port %d", port), e);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public Route addRoute(HttpMethod method, String uri, RouteHandler handler) {
        if (isValid()) {
            System.out.println(String.format("Registering %s route on '%s': %s", method != null ? method.toString() : "catchall", uri, handler.getClass().getSimpleName()));
            final UriRouter router;
            if (!uriRouters.containsKey(uri)) {
                router = new UriRouter();
                httpServer.createContext(uri, router);
                uriRouters.put(uri, router);
            } else {
                router = uriRouters.get(uri);
            }

            return router.addRoute(method, handler);
        }

        //We are not in a valid state
        return null;
    }

    public boolean start() {
        if (isValid()) {
            httpServer.setExecutor(executor);
            httpServer.start();

            return true;
        }

        return false;
    }

    public boolean stop() {
        if (isValid()) {
            //Allow 5 seconds to stop gracefully
            httpServer.stop(5);
            return true;
        }

        return false;
    }

}
