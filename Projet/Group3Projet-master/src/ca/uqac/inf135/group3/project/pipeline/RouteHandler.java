package ca.uqac.inf135.group3.project.pipeline;

import java.io.IOException;

public interface RouteHandler {

    void handle(ExchangeHelper exchangeHelper) throws IOException;
}
