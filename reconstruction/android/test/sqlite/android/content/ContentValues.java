package android.content;
import java.util.LinkedHashMap;
import java.util.Map;
public class ContentValues {
    public final Map<String,String> values = new LinkedHashMap<>();
    public void put(String key, String value) { values.put(key,value); }
    public void put(String key, Integer value) { values.put(key,value.toString()); }
    public void put(String key, Long value) { values.put(key,value.toString()); }
}
