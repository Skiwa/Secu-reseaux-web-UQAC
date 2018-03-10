package ca.uqac.inf135.group3.project.tools.json;

public class JSONUnexpectedCharacterException extends JSONException {
    private static final String FORMAT_STRING = "Unexpected character '%c'. Expecting %s";

    private static String formatMessage(int unexpectedChar, String expectedString) {
        return String.format(FORMAT_STRING, (char) unexpectedChar, expectedString);
    }

    public JSONUnexpectedCharacterException(int unexpectedChar, String expectedString) {
        super(formatMessage(unexpectedChar, expectedString));
    }
    public JSONUnexpectedCharacterException(int unexpectedChar, char expectedChar) {
        super(formatMessage(unexpectedChar, String.format("'%c'", expectedChar)));
    }

    public JSONUnexpectedCharacterException(int unexpectedChar, String expectedString, Throwable cause) {
        super(formatMessage(unexpectedChar, expectedString), cause);
    }
    public JSONUnexpectedCharacterException(int unexpectedChar, int expectedChar, Throwable cause) {
        super(formatMessage(unexpectedChar, String.format("'%c'", expectedChar)), cause);
    }
}
