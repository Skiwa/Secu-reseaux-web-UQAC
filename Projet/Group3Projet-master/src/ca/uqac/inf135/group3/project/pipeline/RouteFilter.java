package ca.uqac.inf135.group3.project.pipeline;

import java.io.IOException;

public interface RouteFilter {
    boolean doFilter(ExchangeHelper exchangeHelper) throws IOException;
    String getDescription();
}
