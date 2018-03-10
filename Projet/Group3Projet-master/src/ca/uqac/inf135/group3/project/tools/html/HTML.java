package ca.uqac.inf135.group3.project.tools.html;

public class HTML {
    public static HTML objects(Object... objects) { return new HTML(null, objects); }
    public static HTML page(Object... objects) { return new HTML("html", objects); }
    public static HTML head(Object... objects) { return new HTML("head", objects); }
    public static HTML title(String title) { return new HTML("title", title); }
    public static HTML stylesheet(String href, Object... objects) {
        return new HTML("link").param("rel", "stylesheet").param("href", href).add(objects);
    }
    public static HTML body(Object... objects) { return new HTML("body", objects); }
    public static HTML div(Object... objects) { return new HTML("div", objects); }
    public static HTML p(Object... objects) { return new HTML("p", objects); }
    public static HTML h(int level, Object... objects) {return new HTML(String.format("h%d", level), objects); }
    public static HTML ul(Object... objects) { return new HTML("ul", objects); }
    public static HTML li(Object... objects) { return new HTML("li", objects); }
    public static HTML script(String src, Object... objects) {
        return new HTML("script").param("src", src).add(objects).add(/*Make sure the closing tag will be separated from the opening tag by forcing a fake content*/);
    }

    public static HTML br() { return new HTML("br"); }

    public static HTMLAttribute attrib(String key, String value) {
        return new HTMLAttribute(key, value);
    }

    private final StringBuilder sb = new StringBuilder();

    private String tag;
    private boolean hasContent;
    private boolean closed;

    private static String escapeHTML(String html) {
        //Minimalistic, but enough for the sake of security
        return html
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                ;
    }

    public HTML() {
        this(null);
    }
    public HTML(String tag) {
        if (tag != null) {
            this.tag = tag.toLowerCase();
        }
        else {
            this.tag = null;
        }
        this.hasContent = false;
        this.closed = false;

        if (this.tag != null) {
            //Special case for html tag, we start document with HTML-5 DOCTYPE
            if ("html".equals(this.tag)) {
                this.appendLine("<!DOCTYPE html>");
            }
            this.append("<");
            this.append(this.tag);
        }
    }
    public HTML(String tag, Object... objects) {
        this(tag);

        add(objects);
        /*for (Object obj : objects) {
            add(obj);
        }*/
    }

    private void append(String content) {
        if (content != null) {
            sb.append(content);
        }
    }
    private void appendLine(String content) {
        append(content);
        sb.append("\n");
    }

    public HTML param(String key, String value) {
        if (!hasContent) {
            append(attrib(key, value).toString());
        }
        return this;
    }

    private void setHasContent() {
        if (!hasContent) {
            if (tag != null) {
                appendLine(">");
            }
            hasContent = true;
        }
    }

    public HTML add(Object... objects) {
        if (objects != null && objects.length > 0) {
            for (Object obj : objects) {
                if (obj != null) {
                    if (obj instanceof HTMLAttribute) {
                        if (!hasContent) {
                            append(obj.toString());
                        }
                    } else {
                        setHasContent();
                        if (obj instanceof HTML) {
                            append(((HTML) obj).build());
                        } else {
                            appendLine(escapeHTML(obj.toString()));
                        }
                    }
                }
                else {
                    setHasContent();
                }
            }
        }
        else {
            setHasContent();
        }
        return this;
    }

    public String build() {
        if (!closed) {
            if (tag != null) {
                if (hasContent) {
                    append("</");
                    append(tag);
                    appendLine(">");
                } else {
                    appendLine(" />");
                }
            }
            closed = true;
        }
        return sb.toString();
    }
}
