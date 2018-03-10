package ca.uqac.inf135.group3.tp3.tools.json;

import java.util.ArrayList;
import java.util.List;

public class JSONArray extends JSON {
    private List<Object> items = new ArrayList<>();

    public JSONArray() {

    }
    public <T> JSONArray(T[] array) {
        for (T item : array) {
            add(item);
        }
    }

    public JSONArray add(Object item) {
        items.add(item);

        return this;
    }

    public int count() {
        return items.size();
    }

    public Object get(int index) {
        if (index >= 0 && index < count()) {
            return items.get(index);
        }
        return null;
    }

    public boolean match(Object obj) {
        if (obj instanceof JSONArray) {
            JSONArray other = (JSONArray) obj;

            int count = items.size();
            if (other.items.size() == count) {
                for (int i = 0; i < count; ++i) {
                    if (items.get(i) instanceof JSON) {
                        JSON json = (JSON) items.get(i);

                        if (!json.match(other.items.get(i))) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        //Initiate with opening bracket
        StringBuilder sb = new StringBuilder("[");

        boolean first = true;
        for (Object item : items) {
            if (first) {
                first = false;
            }
            else {
                //Add a coma between items
                sb.append(",");
            }

            //Add value
            sb.append(JSON.formatObject(item));
        }

        //Add closing bracket
        sb.append("]");

        return sb.toString();
    }
}
