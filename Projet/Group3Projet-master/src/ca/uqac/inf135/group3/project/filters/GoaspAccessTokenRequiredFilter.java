package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.jwt.JwtToken;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;
import ca.uqac.inf135.group3.project.tools.json.JSONException;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import ca.uqac.inf135.group3.project.tools.json.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GoaspAccessTokenRequiredFilter implements RouteFilter {

    private String scope;

    public GoaspAccessTokenRequiredFilter(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        final String auth = exchangeHelper.getAuthorization();

        if (auth != null) {
            //split the authentication string
            final String[] parts = auth.split(" ");

            //We expect 2 strings ("Bearer" and the token)
            if (parts.length == 2) {
                if ("BEARER".equals(parts[0].toUpperCase())) {
                    final String token = parts[1];

                    JwtToken jwtToken = new JwtToken(token);
                    JSONObject tokenPayload = jwtToken.getJSONPayload();

                    if (tokenPayload != null && tokenPayload.containsKey("username")) {

                        //Call GOASP's token validation service
                        String validatePayload = String.format(
                                "token=%s&scope=%s",
                                URLEncoder.encode(token, "UTF-8"),
                                URLEncoder.encode(scope, "UTF-8")
                        );
                        String type = "application/x-www-form-urlencoded";
                        byte[] validatePayloadBytes = validatePayload.getBytes(StandardCharsets.UTF_8);

                        try {
                            //Create URL
                            URL url = new URL(String.format("%s%s", GoaspConst.DOMAIN, GoaspConst.VALIDATE_TOKEN_PATH));

                            //Define connection
                            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                            conn.setDoOutput(true);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", type);
                            conn.setRequestProperty("Content-Length", String.valueOf(validatePayloadBytes.length));

                            //Send request
                            OutputStream out = conn.getOutputStream();
                            out.write(validatePayloadBytes);
                            out.flush();

                            //Check response code
                            if (conn.getResponseCode() == 200) {

                                //Read response
                                InputStream in = conn.getInputStream();
                                Scanner scanner = new Scanner(in).useDelimiter("\\A");
                                String response = scanner.hasNext() ? scanner.next() : "";
                                scanner.close();

                                try {
                                    JSONObject json = new JSONParser(response).getObject();

                                    if (json.containsKey("token") && token.equals(json.getString("token"))) {
                                        if (json.containsKey("status") && "valid".equals(json.getString("status"))) {
                                            //Only if we reached this point that everything is fine

                                            String userID = String.format("goasp_%s", tokenPayload.getString("username"));
                                            exchangeHelper.putValue("userID", userID);

                                            return true;
                                        }
                                    }
                                } catch (JSONException e) {
                                    //Let the unauthorized do it's job
                                }
                            }
                        } catch (IOException e) {
                            //Let the unauthorized do it's job
                        }
                    }
                }
            }
        }

        //No authenticated user
        exchangeHelper.unauthorized();
        //Do NOT continue
        return false;
    }

    @Override
    public String getDescription() {
        return "Scopes: " + scope;
    }
}
