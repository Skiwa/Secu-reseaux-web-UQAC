package ca.uqac.inf135.group3.tp3.pipeline;

import ca.uqac.inf135.group3.tp3.tools.HttpMethod;
import ca.uqac.inf135.group3.tp3.tools.json.JSONArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UriRouter implements HttpHandler {
    private final List<Route> routes = new ArrayList<>();

    public UriRouter() {
    }

    public Route addRoute(HttpMethod method, RouteHandler handler) {
        Route route = new Route(method, handler);
        routes.add(route);

        return route;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ExchangeHelper exchangeHelper = new ExchangeHelper(httpExchange);

        for (Route route : routes) {
            if (route.handle(exchangeHelper)) {
                //Give a default response in case the handler didn't gave a response
                if (!exchangeHelper.isResponded()) {
                    exchangeHelper.internalError(" handing request");
                }
                //The request was handled
                return;
            }
        }

        //Build expected methods and received method
        JSONArray expectedMethods = new JSONArray();
        for (Route route : routes) {
            expectedMethods.add(route.getMethod());
        }

        //Return answer
        exchangeHelper.badMethod(expectedMethods);
    }
}
