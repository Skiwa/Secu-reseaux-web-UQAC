package ca.uqac.inf135.group3.project.tools.html;

public class HTMLAttribute {
    public static HTMLAttribute classes(String classes) { return new HTMLAttribute("class", classes); }
    public static HTMLAttribute integrity(String integrity) { return new HTMLAttribute("integrity", integrity); }
    public static HTMLAttribute crossorigin(String crossorigin) { return new HTMLAttribute("crossorigin", crossorigin); }

    private String escapeValue() {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                ;
    }

    public String name;
    public String value;

    public HTMLAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(" %s=\"%s\"", name, escapeValue());
    }
}
