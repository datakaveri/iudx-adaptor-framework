package in.org.iudx.adaptor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class JsonFlatten {

    private final Map<String, ValueNode> json = new LinkedHashMap<>();
    private final JsonNode root;

    public JsonFlatten(JsonNode node) {
        this.root = Objects.requireNonNull(node);
    }

    public Map<String, ValueNode> flatten() {
        flattenJson(root, null, json);
        return json;
    }

    public static void flattenJson(JsonNode node, String parent, Map<String, ValueNode> map) {
        if (node instanceof ValueNode) {
            map.put(parent, (ValueNode)node);
        } else {
            String prefix = parent == null ? "" : parent + ".";
            if (node instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode)node;
                for(int i = 0; i < arrayNode.size(); i++) {
                    flattenJson(arrayNode.get(i), prefix + i, map);
                }
            } else if (node instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) node;
                for (Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> field = it.next();
                    flattenJson(field.getValue(), prefix + field.getKey(), map);
                }
            } else {
                throw new RuntimeException("unknown json node");
            }
        }
    }
}
