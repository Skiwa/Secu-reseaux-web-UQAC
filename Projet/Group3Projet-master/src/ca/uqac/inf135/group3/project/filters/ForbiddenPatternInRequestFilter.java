package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;

import java.io.IOException;
import java.util.regex.Pattern;

//NOTE: This filter is automatically added as Pre-filter on each route for 'script' keyword pattern
public class ForbiddenPatternInRequestFilter implements RouteFilter {

    private String description;
    private String pattern;

    public ForbiddenPatternInRequestFilter(String description, String pattern) {
        this.description = description;
        this.pattern = pattern;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        String request;
        if (exchangeHelper.getMethod() == HttpMethod.GET) {
            request = exchangeHelper.getRequestQuery();
        }
        else {
            request = exchangeHelper.getRequestBody();
        }

        request = ExchangeHelper.escapeParameter(request);

        if (Pattern.matches(pattern, request)) {
            exchangeHelper.forbidden(String.format("%s is strictly forbidden in requests", description));
            return false;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return String.format("%s: %s", description, pattern);
    }
}
