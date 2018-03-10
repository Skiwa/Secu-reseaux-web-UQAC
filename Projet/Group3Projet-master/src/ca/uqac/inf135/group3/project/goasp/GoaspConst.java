package ca.uqac.inf135.group3.project.goasp;

public class GoaspConst {
    public static final String APP_NAME = "GOASP (Group3 OAuth2 Security Provider)";

    public static final String DOMAIN = "https://group3.stremblay.com";
    public static final String BASE_PATH = "/goasp";
    public static final String SESSION_COOKIE_NAME = "goasp_session";

    public static final String REQUEST_PATH = String.format("%s/%s",BASE_PATH, "request");
    public static final String LOGIN_PATH = String.format("%s/%s",BASE_PATH, "login");
    public static final String LOGOUT_PATH = String.format("%s/%s",BASE_PATH, "logout");
    public static final String REGISTER_PATH = String.format("%s/%s",BASE_PATH, "register");
    public static final String TOKEN_PATH = String.format("%s/%s",BASE_PATH, "token");
    public static final String VALIDATE_TOKEN_PATH = String.format("%s/%s",BASE_PATH, "validate");

    public static final String[] LOGIN_REQUIRED_PARAMETERS = new String[] {"goaspredirect"};
    public static final String[] LOGIN_POST_ADITIONNAL_REQUIRED_PARAMETERS = new String[] {"username", "password", "submit"};

    public static final String[] LOGOUT_REQUIRED_PARAMETERS = new String[] {"goaspredirect"};

    public static final String[] REGISTER_REQUIRED_PARAMETERS = new String[] {"goaspredirect"};
    public static final String[] REGISTER_POST_ADITIONNAL_REQUIRED_PARAMETERS = new String[] {"username", "email", "confemail", "password", "confpwd", "submit"};


    public static final String[] REQUEST_REQUIRED_PARAMETERS = new String[] {"response_type", "client_id", "redirect_uri", "scope", "state"};
    public static final String[] REQUEST_POST_ADITIONNAL_REQUIRED_PARAMETERS = new String[] {"goaspredirect", "securityToken", "submit"};

    public static final String[] TOKEN_REQUIRED_PARAMETERS = new String[] {"grant_type", "code", "redirect_uri", "client_id"};
    public static final String[] VALIDATE_TOKEN_REQUIRED_PARAMETERS = new String[] {"token", "scope"};
}
