package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;
import ca.uqac.inf135.group3.project.tools.html.HTML;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;

public class RequiredRequestParametersFilter implements RouteFilter {
    String[] requiredNames = new String[0];
    JSONObject errorReply = null;

    public RequiredRequestParametersFilter(String... requiredNames) {
        addRequired(requiredNames);
    }

    public RequiredRequestParametersFilter addRequired(String... newParameters) {
        String[] newArray = new String[requiredNames.length + newParameters.length];

        //Copy old array to new Array
        System.arraycopy(requiredNames, 0, newArray, 0, requiredNames.length);
        //Copy new parameters to new Array
        System.arraycopy(newParameters, 0, newArray, requiredNames.length, newParameters.length);

        //Replace attribute with new array
        requiredNames = newArray;

        return this;
    }

    public RequiredRequestParametersFilter setErrorReply(JSONObject errorReply) {
        this.errorReply = errorReply;
        return this;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {

        for (String name : requiredNames) {
            String value = exchangeHelper.getParameter(name);
            if (value == null) {
                //Prebuild the parameters list
                HTML ul = HTML.ul();
                for (String name2 : requiredNames) {
                    ul.add(HTML.li(name2));
                }

                if (errorReply != null) {
                    exchangeHelper.badRequest(errorReply, true);
                }
                else {
                    exchangeHelper.badRequest(new HTML(null,
                            "Request must contain these parameters:",
                            HTML.br(),
                            ul
                    ));
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public String getDescription() {
        final StringBuilder sb = new StringBuilder();

        if (requiredNames.length > 0) {
            for (String name : requiredNames) {
                sb.append(" ");
                sb.append(name);
            }
        }

        return sb.toString();
    }
}
