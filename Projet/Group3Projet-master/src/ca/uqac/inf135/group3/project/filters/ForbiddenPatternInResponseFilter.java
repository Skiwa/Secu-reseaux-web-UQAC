package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;
import ca.uqac.inf135.group3.project.tools.stsp.StspTag;

import java.io.IOException;
import java.util.regex.Pattern;

//NOTE: This filter is automatically added as Post-filter on each route for 'script' keyword pattern
public class ForbiddenPatternInResponseFilter implements RouteFilter {

    private String description;
    private String pattern;

    public ForbiddenPatternInResponseFilter(String description, String pattern) {
        this.description = description;
        this.pattern = pattern;
    }

    private boolean stringMatchesPattern(String str) {
        return Pattern.matches(pattern, str);
    }

    private boolean stspMatchesPattern(StspFile stsp) {
        for (StspTag tag : stsp.getTagList()) {
            if (tag.value instanceof StspFile) {
                if (stspMatchesPattern((StspFile) tag.value)) {
                    return true;
                }
            }
            else {
                if (stringMatchesPattern(tag.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean fail(ExchangeHelper exchangeHelper) {
        exchangeHelper.forbidden(String.format("%s detected in untrusted segment of response", description));
        return false;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        if (exchangeHelper.isResponded()) {
            Object response = exchangeHelper.getResponse();

            if (response instanceof StspFile) {
                if (stspMatchesPattern((StspFile) response)) {
                    return fail(exchangeHelper);
                }
            }
            else {
                String responseStr = exchangeHelper.getResponseString();
                if (stringMatchesPattern(responseStr)) {
                    return fail(exchangeHelper);
                }
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return String.format("%s: %s", description, pattern);
    }
}
