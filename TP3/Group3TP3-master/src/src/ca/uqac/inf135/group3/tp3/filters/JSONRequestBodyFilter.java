package ca.uqac.inf135.group3.tp3.filters;

import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.pipeline.RouteFilter;
import ca.uqac.inf135.group3.tp3.tools.json.JSON;
import ca.uqac.inf135.group3.tp3.tools.json.JSONException;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;
import ca.uqac.inf135.group3.tp3.tools.json.JSONParser;

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
                    //Validate strings content for forbidden script tags
                    final int keys = jsonObject.count();
                    for (int i=0; i < keys; ++i) {
                        Object obj = jsonObject.get(i);

                        if (obj instanceof String) {
                            String strVal = (String) obj;

                            if (strVal.toLowerCase().contains("<script")) {
                                exchangeHelper.badRequest(new JSONObject()
                                        .add("message", "'script' HTML tags are forbidden")
                                        .add("property", jsonObject.getKey(i))
                                );
                                return false;
                            }
                        }
                    }

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
}
