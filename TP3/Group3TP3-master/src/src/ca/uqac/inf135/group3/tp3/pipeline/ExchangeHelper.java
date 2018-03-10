package ca.uqac.inf135.group3.tp3.pipeline;

import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.tools.HttpMethod;
import ca.uqac.inf135.group3.tp3.tools.json.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExchangeHelper {
    private static final int OK = HttpURLConnection.HTTP_OK;
    private static final int CREATED = HttpURLConnection.HTTP_CREATED;

    private static final int UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;
    private static final int BAD_METHOD = HttpURLConnection.HTTP_BAD_METHOD;
    private static final int BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST;
    private static final int NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;
    private static final int GONE = HttpURLConnection.HTTP_GONE;
    private static final int INTERNAL_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;


    private final HttpExchange httpExchange;
    private final Map<String, Object> customValues = new HashMap<>();

    private boolean responded = false;

    public ExchangeHelper(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    private HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public HttpMethod getMethod() {
        return HttpMethod.methodFromString(getHttpExchange().getRequestMethod());
    }

    public String getRequestBody() {
        InputStream inputStream = httpExchange.getRequestBody();
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String body = scanner.hasNext() ? scanner.next() : "";

        scanner.close();
        try {
            inputStream.reset();
        }
        catch (IOException e) {
            int i = 1;
        }

        return body;
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

    public User getUser() {
        return (User) getValue("user");
    }

    public JSONObject getJSON() {
        return (JSONObject) getValue("json");
    }

    public boolean isResponded() {
        return responded;
    }

    private void respond(int code, JSON json) throws IOException {
        if (!responded) {
            responded = true;
            final byte[] responseBytes = json.toString().getBytes();

            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(code, responseBytes.length);

            httpExchange.getResponseBody().write(responseBytes);
            httpExchange.getResponseBody().close();

            System.out.println(String.format("  Replied code %d: %s", code, json.toString()));
        }
    }

    //Success response
    public void ok(JSON json) throws IOException {
        respond(OK, json);
    }

    public void created(JSON json) throws IOException {
        respond(CREATED, json);
    }

    //Error responses
    private void respondError(int code, String message, Object data) throws IOException {
        JSONObject response = new JSONObject()
                .add("error", new JSONObject()
                        .add("code", code)
                        .add("message", message)
                        .add("data", data)
                );

        respond(code, response);
    }

    public void badMethod(JSONArray expected) throws IOException {
        JSONObject response = new JSONObject()
                .add("received", getMethod())
                .add("expected", expected);

        respondError(BAD_METHOD, "Bad method", response);
    }

    public void badRequest(Object details) throws IOException {
        respondError(BAD_REQUEST, "Bad request", details);
    }

    public void gone(String resType, int id) throws IOException {
        respondError(GONE, "Gone", new JSONObject()
                .add("status", "Resource not found")
                .add("type", resType)
                .add("id", id)
        );
    }

    public void notFound() throws IOException {
        respondError(NOT_FOUND, "Not found", getHttpExchange().getRequestURI().getPath());
    }

    public void internalError(String action) throws IOException {
        respondError(INTERNAL_ERROR, "Internal error", String.format("An error occurred %s. Please try again later", action));
    }

    public void unauthorized(Object details) throws IOException {
        getHttpExchange().getResponseHeaders().add("WWW-Authenticate", "Bearer");
        respondError(UNAUTHORIZED, "Unauthorized", details);
    }
    public void unauthorized() throws IOException {
        unauthorized("You are not authorized to perform this action. Log in using /api/login api first.");
    }

}
