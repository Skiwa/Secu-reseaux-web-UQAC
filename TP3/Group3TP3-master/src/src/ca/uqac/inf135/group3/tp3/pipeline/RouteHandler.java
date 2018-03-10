package ca.uqac.inf135.group3.tp3.pipeline;

import java.io.IOException;

public interface RouteHandler {

    void handle(ExchangeHelper exchangeHelper) throws IOException;
}
