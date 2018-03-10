package ca.uqac.inf135.group3.project.pipeline;

import ca.uqac.inf135.group3.project.tools.http.HttpMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private final HttpMethod method;
    private final RouteHandler handler;
    private final List<RouteFilter> preFilters = new ArrayList<>();
    private final List<RouteFilter> postFilters = new ArrayList<>();

    public Route(HttpMethod method, RouteHandler handler) {
        this.method = method;
        this.handler = handler;
    }

    public HttpMethod getMethod() {
        return method;
    }

    private String getFullDescriptionFromFilter(RouteFilter filter) {
        final String description = filter.getDescription();
        if  (description != null) {
            return String.format(" (%s)", description);
        } else {
            return "";
        }
    }

    private void printAddFilterLog(String type, RouteFilter filter) {
        System.out.println(
                String.format(
                        "  Adding %s-filter on route '%s': %s%s",
                        type,
                        handler.getClass().getSimpleName(),
                        filter.getClass().getSimpleName(),
                        getFullDescriptionFromFilter(filter)
                )
        );
    }

    public Route addPreFilter(RouteFilter filter) {
        printAddFilterLog("pre", filter);
        preFilters.add(filter);
        return this;
    }
    public Route addPostFilter(RouteFilter filter) {
        printAddFilterLog("post", filter);
        postFilters.add(filter);
        return this;
    }

    public boolean handle(ExchangeHelper exchangeHelper) throws IOException {
        //Filter request by HTTP method first
        if (method == null || method == exchangeHelper.getMethod()) {
            System.out.println(String.format("%s request received on path '%s', using handler: %s", exchangeHelper.getMethod(), exchangeHelper.getUriPath(), handler.getClass().getSimpleName()));

            //Run pre filters
            for (RouteFilter filter : preFilters) {
                if (!filter.doFilter(exchangeHelper)) {
                    //We don't continue, but the request was handled
                    return true;
                }
            }

            //OK, we are a route for that method
            handler.handle(exchangeHelper);

            //Run post filters
            for (RouteFilter filter : postFilters) {
                if (!filter.doFilter(exchangeHelper)) {
                    //We don't continue, but the request was handled
                    return true;
                }
            }

            //We handled that request
            return true;
        }

        //We did NOT handle that request
        return false;
    }
}
