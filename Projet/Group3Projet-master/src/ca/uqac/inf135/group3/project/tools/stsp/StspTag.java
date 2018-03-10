package ca.uqac.inf135.group3.project.tools.stsp;

import ca.uqac.inf135.group3.project.tools.html.HTML;

public class StspTag {
    public final String tag;
    public Object value = null;

    public StspTag(String tag) {
        this.tag = tag;
    }

    public void setValue(Object obj) {
        this.value = obj;
    }

    public boolean match(String tag) {
        return this.tag != null && this.tag.equals(tag);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        else if (value instanceof HTML) {
            return  ((HTML) value).build();
        }
        else {
            return value.toString();
        }
    }
}
