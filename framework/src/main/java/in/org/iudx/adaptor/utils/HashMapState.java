package in.org.iudx.adaptor.utils;

import in.org.iudx.adaptor.datatypes.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class HashMapState {

    private HashMap<String, Message> map;

    HashMapState() {
        this.map = new HashMap<>();
    }

    public void addMessage(Message msg) {
        this.map.put(msg.key, msg);
    }

    public Message removeMessage(Message msg) {
        return this.map.remove(msg.key);
    }

    public boolean isDuplicate(Message msg) {
        return this.map.containsKey(msg.key);
    }

    public ObjectOutputStream serialize() throws Exception {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outBytes);
        out.writeObject(this.map);
        return out;
    }

    public void deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(inBytes);
        this.map = (HashMap<String, Message>) in.readObject();
    }
}
