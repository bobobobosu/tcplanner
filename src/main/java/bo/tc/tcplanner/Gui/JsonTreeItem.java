package bo.tc.tcplanner.Gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TreeItem;

import java.util.Map;

public class JsonTreeItem {
    private static void prependString(TreeItem<Value> item, String string) {
        String val = item.getValue().text;
        item.getValue().text = (val == null
                ? string
                : string + " : " + val);
    }

    private enum Type {
        OBJECT(new Rectangle2D(45, 52, 16, 18)),
        ARRAY(new Rectangle2D(61, 88, 16, 18)),
        PROPERTY(new Rectangle2D(31, 13, 16, 18));

        private final Rectangle2D viewport;

        Type(Rectangle2D viewport) {
            this.viewport = viewport;
        }

    }

    public static final class Value {

        public String text;
        public final Type type;

        public Value(Type type) {
            this.type = type;
        }

        public Value(String text, Type type) {
            this.text = text;
            this.type = type;
        }

    }

    public static TreeItem<Value> createTree(JsonElement element) {
        if (element.isJsonNull()) {
            return new TreeItem<>(new Value("null", Type.PROPERTY));
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            return new TreeItem<>(new Value(primitive.isString()
                    ? '"' + primitive.getAsString() + '"'
                    : primitive.getAsString(), Type.PROPERTY));
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            TreeItem<Value> item = new TreeItem<>(new Value(Type.ARRAY));
            // for (int i = 0, max = Math.min(1, array.size()); i < max; i++) {
            for (int i = 0, max = array.size(); i < max; i++) {
                TreeItem<Value> child = createTree(array.get(i));
                prependString(child, Integer.toString(i));
                item.getChildren().add(child);
            }
            return item;
        } else {
            JsonObject object = element.getAsJsonObject();
            TreeItem<Value> item = new TreeItem<>(new Value(Type.OBJECT));
            for (Map.Entry<String, JsonElement> property : object.entrySet()) {
                TreeItem<Value> child = createTree(property.getValue());
                prependString(child, property.getKey());
                item.getChildren().add(child);
            }
            return item;
        }
    }
}
