package in.org.iudx.adaptor.datatypes;

import java.time.Instant;
import java.io.Serializable;

public class GenericJsonMessage implements Serializable {

  public String body;
  public String key;
  public Instant timestamp;


  public GenericJsonMessage(){
  }

  @Override
  public String toString() {
    return body;
  }

  @Override
  public boolean equals(Object other) {
    /** Todo:
     *    - Equal with the key
     **/
    return false;
  }

  @Override
  public int hashCode() {
    /** Todo:
     *    - Hash with the id+timestamp
     **/
    return 0;
  }

  public long getEventTime() {
    /** Todo:
     *    - Return Instance time
     **/
    return 0;
  }

}
