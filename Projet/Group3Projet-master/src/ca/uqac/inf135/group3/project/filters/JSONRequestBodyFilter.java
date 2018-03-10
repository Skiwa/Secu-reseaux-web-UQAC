package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;
import ca.uqac.inf135.group3.project.tools.json.JSON;
import ca.uqac.inf135.group3.project.tools.json.JSONException;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import ca.uqac.inf135.group3.project.tools.json.JSONParser;

import java.io.IOException;

public class JSONRequestBodyFilter implements RouteFilter {
    private final JSON templateJSON;


    public JSONRequestBodyFilter(JSON templateJSON) {
        this.templateJSON = templateJSON;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        String stringBody = exchangeHelper.getRequestBody();

        try {
            JSONParser parser = new JSONParser(stringBody);

            if (parser.isObject()) {
                final JSONObject jsonObject = parser.getObject();
                exchangeHelper.putValue("json", jsonObject);

                if (templateJSON == null || templateJSON.match(parser.getObject())) {
                    return true;
                }
            }
        } catch (JSONException e) {
            //Nothing more to do
        }

        if (templateJSON != null) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("message", "Request must contain a valid JSON object matching the following template")
                    .add("template", templateJSON)
            );
        }
        else {
            exchangeHelper.badRequest(new JSONObject()
                    .add("message", "Request must contain a valid JSON object or array")
            );
        }
        return false;
    }


    @Override
    public String getDescription() {
        return templateJSON.describeTemplate();
    }
}
