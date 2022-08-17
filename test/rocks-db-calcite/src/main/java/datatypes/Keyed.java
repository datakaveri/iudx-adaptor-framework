package datatypes;

import in.org.iudx.adaptor.datatypes.Message;

public class Keyed<M, S> {
    private Message message;
    private String key;

    public Keyed(Message msg, String key) {
        this.message = msg;
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
