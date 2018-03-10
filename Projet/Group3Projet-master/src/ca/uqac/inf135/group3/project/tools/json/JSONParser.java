package ca.uqac.inf135.group3.project.tools.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JSONParser {
    private final Reader reader;
    private final JSON jsonContent;
    private boolean hasNextChar;
    private int nextChar;

    public JSONParser(String content) throws JSONException {
        this.reader = new StringReader(content);
        this.jsonContent = parse();
        this.hasNextChar = false;
        this.nextChar = -1;
    }

    private int readNextChar() throws JSONException {
        if (hasNextChar) {
            //There's a nextChar that has been put back to stream
            hasNextChar = false;
            return nextChar;
        }
        else {
            try {
                nextChar = reader.read();
                return nextChar;
            } catch (IOException e) {
                throw new JSONException("An error occurred reading JSON", e);
            }
        }
    }

    private void putCharBack() {
        hasNextChar = true;
    }

    private int readNextSignificantChar() throws JSONException {
        int cRead;
        while ((cRead = readNextChar()) >= 0) {
            //Skip insignificant characters
            switch (cRead) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    continue;
            }

            break;
        }

        return cRead;
    }

    private String readString() throws JSONException {
        final StringBuilder sb = new StringBuilder();

        //Read string until we find closing quote (")
        int readChar;
        while ((readChar = readNextChar()) >= 0) {
            if (readChar == '\\') {
                //Escape character found, read the next one and convert it accordingly
                readChar = readNextChar();

                if (readChar < 0) {
                    throw new JSONUnexpectedEndOfStreamException("escaped character");
                }
                else {
                    switch (readChar) {
                        case 'r':
                            sb.append("\r");
                            break;
                        case 'n':
                            sb.append("\n");
                            break;
                        case 't':
                            sb.append("\t");
                            break;
                        case '\\':
                            sb.append("\\");
                            break;
                        case '"':
                            sb.append("\"");
                            break;
                        default:
                            //Not a know/supported espaced char, add both "as-is"
                            sb.append('\\');
                            sb.append((char) readChar);
                    }
                }
            }
            else if (readChar == '"') {
                //OK we found the closing quote
                return sb.toString();
            }
            else {
                sb.append((char) readChar);
            }
        }

        throw new JSONException("Unexpected end of stream. Expecting key name or '\"'");
    }

    private Object readNumeric() throws JSONException {
        final StringBuilder sb = new StringBuilder();

        for(;;) {
            int charRead = readNextChar();

            if (charRead >= '0' && charRead <= '9' || charRead == '.') {
                sb.append((char) charRead);
                //NOTE: we don't care about formatting, we'll let Integer.parse and Double.parse do their job
            }
            else {
                //End of the numeric value
                putCharBack();
                break;
            }
        }

        String str = sb.toString();
        if (str.contains(".")) {
            try {
                return Double.parseDouble(str);
            }
            catch (NumberFormatException e) {
                throw new JSONException(String.format("Error parsing floating point value: %s", str), e);
            }
        }
        else {
            try {
                return Long.parseLong(str, 10);
            }
            catch (NumberFormatException e) {
                throw new JSONException(String.format("Error parsing integer value: %s", str), e);
            }
        }
    }

    private Object readKeyword() throws JSONException {
        final StringBuilder sb = new StringBuilder();

        for(;;) {
            int charRead = readNextChar();

            if (charRead >= 'a' && charRead <= 'z') {
                sb.append((char) charRead);
            }
            else {
                //End of the keyword
                putCharBack();
                break;
            }
        }

        String str = sb.toString();
        //NOTE: We only support these 3 keywords:
        if ("true".equals(str)) {
            return true;
        }
        else if ("false".equals(str)) {
            return false;
        }
        else if ("null".equals(str)) {
            return null;
        }
        else {
            throw new JSONException(String.format("Invalid keyword found: %s. Expected: true, false or null", str));
        }
    }

    private Object readValue() throws JSONException {
        final String EXPECTED_CHARS = "'{', '[', or literal";
        int readChar = readNextSignificantChar();

        if (readChar < 0) {
            throw new JSONUnexpectedEndOfStreamException(EXPECTED_CHARS);
        }
        else if (readChar == '{') {
            return parseJSONObject();
        }
        else if (readChar == '[') {
            return parseJSONArray();
        }
        else if (readChar == '"') {
            return readString();
        }
        else if (readChar >= '0' && readChar <= '9') {
            //Numeric literal (either long or double)
            putCharBack();
            return readNumeric();
        }
        else if (readChar >= 'a' && readChar <= 'z') {
            //Keyword, either true, false or null
            putCharBack();
            return readKeyword();
        }
        else {
            throw new JSONUnexpectedCharacterException(readChar, EXPECTED_CHARS);
        }
    }

    private JSONObject parseJSONObject() throws JSONException {
        final JSONObject ret = new JSONObject();
        boolean comaAllowed = false;

        //Read until we find the closing brace ('}')
        for (;;) {
            int readChar = readNextSignificantChar();

            if (readChar < 0) {
                if (comaAllowed) {
                    throw new JSONUnexpectedEndOfStreamException("'}' or ','");
                }
                else {
                    throw new JSONUnexpectedEndOfStreamException('}');
                }
            }
            else if (readChar == ',') {
                if (comaAllowed) {
                    comaAllowed = false;
                }
                else {
                    throw new JSONUnexpectedCharacterException(readChar, "'}' or '\"'");
                }
            }
            else if (readChar == '}') {
                //End of JSONObject found, return the object
                return ret;
            }
            else if (readChar == '"') {
                if (comaAllowed) {
                    //A coma was necessary before we were able to read another key/pair value
                    throw new JSONUnexpectedCharacterException(readChar, ',');
                }
                else {
                    //Read a key
                    String key = readString();

                    //Now we expect a separation (:)
                    readChar = readNextSignificantChar();

                    if (readChar == ':') {
                        //Now read the value
                        Object value = readValue();

                        //Add key/value pair to JSONObject
                        ret.add(key, value);

                        //coma is allowed for another key/value pair
                        comaAllowed = true;
                    } else {
                        throw new JSONUnexpectedCharacterException(readChar, ':');
                    }
                }
            }
            else {
                throw new JSONUnexpectedCharacterException(readChar, '"');
            }
        }
    }

    private JSONArray parseJSONArray() throws JSONException {
        final JSONArray ret = new JSONArray();
        boolean comaAllowed = false;

        //Read until we find the closing brace ('}')
        for (;;) {
            int readChar = readNextSignificantChar();

            if (readChar < 0) {
                if (comaAllowed) {
                    throw new JSONUnexpectedEndOfStreamException("object, array, literal, ']' or ','");
                }
                else {
                    throw new JSONUnexpectedEndOfStreamException("object, array, literal or ']'");
                }
            }
            else if (readChar == ',') {
                if (comaAllowed) {
                    comaAllowed = false;
                }
                else {
                    throw new JSONUnexpectedCharacterException(readChar, ']');
                }
            }
            else if (readChar == ']') {
                //End of JSONArray found, return the object
                return ret;
            }
            else {
                if (comaAllowed) {
                    //A coma was necessary before we were able to read another value
                    throw new JSONUnexpectedCharacterException(readChar, ',');
                }
                else {
                    //Read the value
                    putCharBack();
                    Object value = readValue();

                    //Add value to JSONArray
                    ret.add(value);

                    //coma is allowed for another value
                    comaAllowed = true;
                }
            }
        }
    }

    private JSON parse() throws JSONException {
        JSON retJSON;
        int cRead = readNextSignificantChar();

        if (cRead < 0) {
            throw new JSONUnexpectedEndOfStreamException("'{' or '['");
        } else if (cRead == '{') {
            //It's a JSONObject
            retJSON = parseJSONObject();

            //Mak sure, there's nothing more to read
            cRead = readNextSignificantChar();
            if (cRead >= 0) {
                throw new JSONUnexpectedCharacterException(cRead, "end of stream");
            }
        } else if (cRead == '[') {
            retJSON = parseJSONArray();

            //Mak sure, there's nothing more to read
            cRead = readNextSignificantChar();
            if (cRead >= 0) {
                throw new JSONUnexpectedCharacterException(cRead, "end of stream");
            }
        } else {
            throw new JSONUnexpectedCharacterException(cRead, "'{' or '['");
        }

        return retJSON;
    }

    public boolean isObject() {
        return jsonContent instanceof JSONObject;
    }
    public boolean isArray() {
        return jsonContent instanceof JSONArray;
    }

    public JSON getJSON() {
        return jsonContent;
    }

    public JSONObject getObject() {
        return (JSONObject) jsonContent;
    }

    public JSONArray getArray() {
        return (JSONArray) jsonContent;
    }
}
