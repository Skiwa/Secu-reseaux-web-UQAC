package ca.uqac.inf135.group3.tp3.tools.json;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JSONObject extends JSON {
    public class Entry {
        public String key;
        public Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public JSONObject() {

    }

    public JSONObject add(String key, Object value) {
        entries.add(new Entry(key, value));
        return this;
    }

    public boolean containsKey(String key) {
        for (Entry entry : entries) {
            if (key.equals(entry.key)) {
                return true;
            }
        }
        return false;
    }

    public Object get(int index) {
        if (index >= 0 && index < count()) {
            return entries.get(index).value;
        }
        return null;
    }
    public String getKey(int index) {
        if (index >= 0 && index < count()) {
            return entries.get(index).key;
        }
        return null;
    }

    public Object get(String key) {
        for (Entry entry : entries) {
            if (key.equals(entry.key)) {
                return entry.value;
            }
        }
        return null;
    }

    public String getString(String key) {
        Object val = get(key);
        if (val instanceof String) {
            return (String) get(key);
        }
        return null;
    }
    public int getInt(String key, int def) {
        Object val = get(key);
        if (val instanceof Long) {
            long longVal = (long) val;
            return (int) longVal;
        }
        else if (val instanceof Boolean) {
            return (Boolean)val ? 1 : 0;
        }
        else {
            return def;
        }
    }
    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int count() {
        return entries.size();
    }

    public boolean match(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject other = (JSONObject) obj;

            if (other.entries.size() == entries.size()) {
                for (Entry entry : entries) {
                    if (entry.value instanceof JSON) {
                        JSON json = (JSON) entry.value;

                        if (!json.match(other.get(entry.key))) {
                            return false;
                        }
                    }
                    if (!other.containsKey(entry.key)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        //Initiate with opening brace
        StringBuilder sb = new StringBuilder("{");

        boolean first = true;
        for (Entry entry : entries) {
            if (first) {
                first = false;
            }
            else {
                //Add a coma between entries
                sb.append(",");
            }
            //Add key
            sb.append(formatString(entry.key));

            //Add separator
            sb.append(":");

            //Add value
            sb.append(formatObject(entry.value));
        }

        //Add closing brace
        sb.append("}");

        return sb.toString();
    }
}
