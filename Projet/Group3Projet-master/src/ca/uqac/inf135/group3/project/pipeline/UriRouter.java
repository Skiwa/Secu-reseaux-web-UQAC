package ca.uqac.inf135.group3.project.pipeline;

import ca.uqac.inf135.group3.project.filters.ForbiddenPatternInRequestFilter;
import ca.uqac.inf135.group3.project.filters.ForbiddenPatternInResponseFilter;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.json.JSONArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UriRouter implements HttpHandler {
    private final List<Route> routes = new ArrayList<>();

    ForbiddenPatternInRequestFilter autoScriptPreFilter = new ForbiddenPatternInRequestFilter("'script' keyword", ".*[Ss][Cc][Rr][Ii][Pp][Tt].*");
    ForbiddenPatternInResponseFilter autoScriptPostFilter = new ForbiddenPatternInResponseFilter("'script' keyword", ".*[Ss][Cc][Rr][Ii][Pp][Tt].*");

    public UriRouter() {
    }

    public Route addRoute(HttpMethod method, RouteHandler handler) {
        Route route = new Route(method, handler);

        //Automatically add the "script in requests/response" filters
        route.addPreFilter(autoScriptPreFilter);
        route.addPostFilter(autoScriptPostFilter);
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
                    exchangeHelper.internalError(" handling request");
                }

                //Send the response to user
                exchangeHelper.sendFinalResponse();

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
        exchangeHelper.sendFinalResponse();
    }
}
