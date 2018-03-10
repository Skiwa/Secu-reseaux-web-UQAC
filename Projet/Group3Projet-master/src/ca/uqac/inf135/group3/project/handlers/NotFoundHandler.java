package ca.uqac.inf135.group3.project.handlers;

import ca.uqac.inf135.group3.project.pipeline.RouteHandler;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;

import java.io.IOException;

public class NotFoundHandler implements RouteHandler {

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        System.out.println(exchangeHelper.getRemoteHost());
        exchangeHelper.notFound();
    }
}
