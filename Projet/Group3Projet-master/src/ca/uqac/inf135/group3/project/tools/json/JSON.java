package ca.uqac.inf135.group3.project.tools.json;

public abstract class JSON {

    public static String formatString(String in) {
        return "\"" + in
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\"", "\\\"")
                + "\"";
    }

    public static String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        else if (obj instanceof Boolean || obj instanceof Number || obj instanceof JSON) {
            return obj.toString();
        }
        else
            return formatString(obj.toString());
    }

    public abstract boolean match(Object obj);

    public abstract String describeTemplate();

    @Override
    public abstract String toString();
}
