package ca.uqac.inf135.group3.tp3.handlers;

import ca.uqac.inf135.group3.tp3.pipeline.RouteHandler;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;

import java.io.IOException;

public class NotFoundHandler implements RouteHandler {

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        exchangeHelper.notFound();
    }
}
