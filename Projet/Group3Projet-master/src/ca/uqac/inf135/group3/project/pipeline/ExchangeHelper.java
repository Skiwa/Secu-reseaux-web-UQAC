package ca.uqac.inf135.group3.project.pipeline;

import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspSession;
import ca.uqac.inf135.group3.project.tools.html.HTML;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.json.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExchangeHelper {
    private static final String TYPE_JSON = "application/json";
    private static final String TYPE_HTML = "text/html";
    private static final String TYPE_TEXT = "text/plain";

    private static String removeCrLf(String text) {
        return text
                .replace("\r\n", " ")
                .replace("\r", " ")
                .replace("\n", " ")
                ;
    }

    public static String unescapeParameter(String escapedParameter) {
        final String hex = "0123456789abcdefABCDEF";
        final StringBuilder sb = new StringBuilder();

        int pos=0;
        while (pos < escapedParameter.length()) {
            char c = escapedParameter.charAt(pos);

            if (c == '%' && escapedParameter.length() >= pos+2) {
                final char c1 = escapedParameter.charAt(++pos);
                final char c2 = escapedParameter.charAt(++pos);

                int p1 = hex.indexOf(c1);
                int p2 = hex.indexOf(c2);

                if (p1 >=0 && p2 >= 0) {
                    //Account for capital A-F
                    if (p1 >= 16) {
                        p1 -= 6;
                    }
                    if (p2 >= 16) {
                        p2 -= 6;
                    }
                    sb.append((char) (p1*16+p2) );
                }
                else {
                    sb.append(c);
                    sb.append(c1);
                    sb.append(c2);
                }
            }
            else if (c == '+') {
                sb.append(' ');
            }
            else {
                sb.append(c);
            }

            ++pos;
        }

        return sb.toString();
    }

    public static String escapeParameter(String unescapedParameter) {
        final String hex = "0123456789abcdef";
        final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.";
        final StringBuilder sb = new StringBuilder();

        for (int pos = 0; pos < unescapedParameter.length(); ++pos) {
            char c = unescapedParameter.charAt(pos);

            if (c == ' ') {
                sb.append('+');
            }
            else {
                final int validCharIndex = validChars.indexOf(c);

                if (validCharIndex >= 0) {
                    sb.append(c);
                }
                else {
                    int intChar = c;

                    if (intChar < 0 || intChar >= 256) {
                        intChar = (intChar+256) % 256;
                    }

                    int pos1 = intChar / 16;
                    int pos2 = intChar % 16;
                    sb.append('%');
                    sb.append(hex.substring(pos1, pos1+1));
                    sb.append(hex.substring(pos2, pos2+1));
                }
            }
        }

        return sb.toString();
    }

    public static String escapeHTML(String unescaped) {
        final StringBuilder sb = new StringBuilder();

        for (int pos = 0; pos < unescaped.length(); ++pos) {
            char c = unescaped.charAt(pos);

            if (c == '&') {
                sb.append("&amp;");
            }
            else if (c == '<') {
                sb.append("&lt;");
            }
            else if (c == '>') {
                sb.append("&gt;");
            }
            else if (c == '"') {
                sb.append("&quot;");
            }
            else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static final int OK = HttpURLConnection.HTTP_OK;
    private static final int CREATED = HttpURLConnection.HTTP_CREATED;

    private static final int MOVED_TEMPORARILY = HttpURLConnection.HTTP_MOVED_TEMP;

    private static final int FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN;
    private static final int UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;
    private static final int BAD_METHOD = HttpURLConnection.HTTP_BAD_METHOD;
    private static final int BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST;
    private static final int NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;
    private static final int GONE = HttpURLConnection.HTTP_GONE;
    private static final int INTERNAL_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;


    private final HttpExchange httpExchange;
    private final Map<String, Object> customValues = new HashMap<>();

    private int responseCode = 0;
    private String responseType;
    private Object responseContent;

    public ExchangeHelper(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    private HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public HttpMethod getMethod() {
        return HttpMethod.methodFromString(getHttpExchange().getRequestMethod());
    }

    private void parseRequestParameters() {
        final String paramString;
        if (getMethod() == HttpMethod.GET) {
            paramString = getRequestQuery();
        } else {
            paramString = getRequestBody();
        }

        final String[] parameters = paramString.split("&");

        for (String parameter : parameters) {
            final int pos = parameter.indexOf('=');
            if (pos > 0) {
                final String name = parameter.substring(0, pos).toLowerCase();

                putValue("param." + name, unescapeParameter(parameter.substring(pos + 1)));
            }
        }
    }

    public String getParameter(String paramName) {
        if (!hasValues("requestParamParsed")) {
            parseRequestParameters();
            putValue("requestParamParsed", true);
        }

        final String key = "param." + paramName.toLowerCase();
        if (hasValues(key)) {
            return (String) getValue(key);
        }
        return  null;
    }

    private String requestBody = null;

    public String getRequestBody() {
        if (requestBody == null) {
            InputStream inputStream = httpExchange.getRequestBody();
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            requestBody = body != null ? body : "";
        }
        return requestBody;
    }

    public String getRequestQuery() {
        final String query = httpExchange.getRequestURI().getQuery();

        return query != null ? query : "";
    }

    public String getAuthorization() {
        List<String> authList = getHttpExchange().getRequestHeaders().get("Authorization");

        if (authList != null && authList.size() == 1) {
            return authList.get(0);
        }
        return null;
    }

    public String getUriPath() {
        return httpExchange.getRequestURI().getPath();
    }

    public boolean hasValues(String key) {
        return customValues.containsKey(key.toUpperCase());
    }

    public void putValue(String key, Object value) {
        customValues.put(key.toUpperCase(), value);
    }

    public Object getValue(String key) {
        if (hasValues(key)) {
            return customValues.get(key.toUpperCase());
        }
        else {
            return null;
        }
    }

    public String getUserID() {
        Object userID = getValue("userID");
        return userID != null ? userID.toString() : "";
    }

    public JSONObject getJSON() {
        return (JSONObject) getValue("json");
    }

    public GoaspSession getSession() {
        return (GoaspSession) getValue("session");
    }

    public boolean isResponded() {
        return responseCode != 0;
    }

    public void sendFinalResponse() throws IOException {
        final String responseString;
        final String showResponseString;
        if (responseCode / 100 == 3) {
            showResponseString = responseContent.toString();
            getHttpExchange().getResponseHeaders().add("Location", showResponseString);

            responseString = "";
        }
        else {
            responseString = getResponseString();
            showResponseString = responseString;
        }

        final byte[] responseBytes = responseString.getBytes();

        httpExchange.getResponseHeaders().add("Content-Type", responseType);
        httpExchange.sendResponseHeaders(responseCode, responseBytes.length);

        httpExchange.getResponseBody().write(responseBytes);
        httpExchange.getResponseBody().close();

        System.out.println(String.format("  Replied code %d: %s", responseCode, removeCrLf(showResponseString)));
    }

    public void setCacheControl(String cacheType) {
        httpExchange.getResponseHeaders().add("Cache-Control", cacheType);
    }
    public void setCacheControlNoStore() {
        setCacheControl("no-store");
    }

    public void setCookie(String name, String value, String path, Integer maxAgeSeconds, boolean secure, boolean httpOnly) {
        //Assemble cookie string
        String cookie = String.format(
                "%s=%s;Path=%s%s%s%s",
                name,
                escapeParameter(value),
                path != null ? escapeParameter(path) : "/", //NOTE: /s will be convertes to %2F
                maxAgeSeconds != null ? "; Max-Age=" + maxAgeSeconds.toString() : "",
                secure ? "; Secure" : "",
                httpOnly ? "; HttpOnly" : ""
        );

        httpExchange.getResponseHeaders().add("Set-Cookie", cookie);
    }

    public String getCookie(String name) {
        if (httpExchange.getRequestHeaders().containsKey("Cookie")) {
            final List<String> cookieHeaders = httpExchange.getRequestHeaders().get("Cookie");

            for (String cookieHeader : cookieHeaders) {
                final String[] cookies = cookieHeader.split("; ");

                for (String cookie : cookies) {
                    final int pos = cookie.indexOf('=');

                    final String cookieName;
                    final String cookieValue;
                    if (pos >= 0) {
                        cookieName = cookie.substring(0, pos);
                        cookieValue = cookie.substring(pos+1);
                    }
                    else {
                        cookieName = cookie;
                        cookieValue = "";
                    }

                    if (cookieName.toLowerCase().equals(name.toLowerCase())) {
                        return cookieValue;
                    }
                }
            }
        }
        return null;
    }

    public Object getResponse() {
        return responseContent;
    }
    public String getResponseString() {
        if (getResponse() == null) {
            return "";
        }
        else if (getResponse() instanceof HTML) {
            return ((HTML) getResponse()).build();
        }
        else {
            return getResponse().toString();
        }
    }

    public void setResponse(String response) {
        responseContent = response;
    }

    private void respond(int code, String contentType, Object response) {
        responseCode = code;
        responseType = contentType;
        responseContent = response;
    }

    private void respondAutoType(int code, Object object) {
        if (object instanceof JSON) {
            respond(code, TYPE_JSON, object);
        }
        else if (object instanceof HTML) {
            respond(code, TYPE_HTML, object);
        }
        else if (object instanceof StspFile) {
            respond(code, TYPE_HTML, object);
        }
        else {
            respond(code, TYPE_TEXT, object);
        }
    }

    //Success responseContent
    public void ok(Object content) {
        respondAutoType(OK, content);
    }

    public void created(Object content) {
        respondAutoType(CREATED, content);
    }

    //Error responses
    private void respondError(int code, String message, Object data, boolean raw) {
        if (raw || data instanceof StspFile) {
            respondAutoType(code, data);
        } else {
            if (data instanceof HTML) {
                StspFile page = new StspFile("errors/generic_error_page.html");
                page.setHTML("title", message);
                page.setHTML("error", message);
                page.setHTML("details", (HTML) data);
                respond(code, TYPE_HTML, page);
            } else {
                JSONObject jsonResponse = new JSONObject()
                        .add("error", new JSONObject()
                                .add("code", code)
                                .add("message", message)
                                .add("data", data)
                        );

                respondAutoType(code, jsonResponse);
            }
        }
    }
    private void respondError(int code, String message, Object data) {
        respondError(code, message, data, false);
    }

    public void badMethod(JSONArray expectedMethods) {
        JSONObject response = new JSONObject()
                .add("received", getMethod())
                .add("expected", expectedMethods);

        respondError(BAD_METHOD, "Bad method", response);
    }

    public void badRequest(Object details, boolean raw) {
        respondError(BAD_REQUEST, "Bad request", details, raw);
    }
    public void badRequest(Object details) {
        badRequest(details, false);
    }

    public void gone(String resType, int id) {
        respondError(GONE, "Gone", new JSONObject()
                .add("status", "Resource not found")
                .add("type", resType)
                .add("id", id)
        );
    }

    public void notFound() {
        respondError(NOT_FOUND, "Not found", getHttpExchange().getRequestURI().getPath());
    }

    public void internalError(String action) {
        respondError(INTERNAL_ERROR, "Internal error", String.format("An error occurred %s. Please try again later", action));
    }

    public void unauthorized(Object details) {
        getHttpExchange().getResponseHeaders().add("WWW-Authenticate", "Bearer");
        respondError(UNAUTHORIZED, "Unauthorized", details);
    }
    public void unauthorized() {
        unauthorized("You are not authorized to perform this action.");
    }

    public void forbidden(Object details, boolean raw) {
        respondError(FORBIDDEN, "Forbidden", details, raw);
    }
    public void forbidden(Object details) {
        forbidden(details, false);
    }

    public void redirectTo(String url) {
        respond(MOVED_TEMPORARILY, "", url);
    }

    public String getRemoteHost() {
        return httpExchange.getRemoteAddress().toString();
    }

}
