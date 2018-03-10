package ca.uqac.inf135.group3.project.tools.http;

public class HttpMethod {
    public static final HttpMethod GET = new HttpMethod("GET");
    public static final HttpMethod POST = new HttpMethod("POST");
    public static final HttpMethod DELETE = new HttpMethod("DELETE");
    public static final HttpMethod PATCH = new HttpMethod("PATCH");

    public static HttpMethod methodFromString(String method) {
        if (method != null) {
            String nice = method.toUpperCase().trim();

            if (GET.textMethod.equals(nice)) {
                return HttpMethod.GET;
            } else if (POST.textMethod.equals(nice)) {
                return HttpMethod.POST;
            } else if (DELETE.textMethod.equals(nice)) {
                return HttpMethod.DELETE;
            } else if (PATCH.textMethod.equals(method)) {
                return HttpMethod.PATCH;
            }
        }

        return new HttpMethod(method != null ? method : "");
    }

    private final String textMethod;

    private HttpMethod(String textMethod) {
        this.textMethod = textMethod;
    }

    @Override
    public String toString() {
        return textMethod;
    }
}
