package ca.uqac.inf135.group3.project.tools.json;

public class JSONUnexpectedEndOfStreamException extends JSONException {
    private static final String FORMAT_STRING = "Unexpected end of stream. Expecting %s";

    private static String formatMessage(String expectedString) {
        return String.format(FORMAT_STRING, expectedString);
    }

    public JSONUnexpectedEndOfStreamException(String expectedString) {
        super(formatMessage(expectedString));
    }

    public JSONUnexpectedEndOfStreamException(char expectedChar) {
        super(formatMessage(String.format("'%c'", expectedChar)));
    }

    public JSONUnexpectedEndOfStreamException(String expectedString, Throwable cause) {
        super(formatMessage(expectedString), cause);
    }

    public JSONUnexpectedEndOfStreamException(char expectedChar, Throwable cause) {
        super(formatMessage(String.format("'%c'", expectedChar)), cause);
    }
}
