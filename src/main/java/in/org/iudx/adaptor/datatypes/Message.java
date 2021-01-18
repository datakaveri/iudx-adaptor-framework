/**
 * <h1>Message.java</h1>
 * Generic http response message wrapper
 */

package in.org.iudx.adaptor.datatypes;

import java.time.Instant;
import java.io.Serializable;

/**
 * {@link HttpEntity} Response message wrapper
 * TODO: 
 *  - Getters and setters according to specification
 *  - Validations
 */
public class Message implements Serializable {

  public String body;
  public String key;
  public Instant timestamp;

  private static final long serialVersionUID = 11L;

  /**
   * {@link Message} Creates an empty wrapper
   */
  public Message(){
  }

   /**
   * Serialize this object
   * 
   * @return {@link String}
   */
  @Override
  public String toString() {
    return body;
  }

   /**
   * Compare this object
   * TODO: todo
   * 
   * @return {@link boolean}
   */
  @Override
  public boolean equals(Object other) {
    /** Todo:
     *    - Equal with the key
     **/
    return false;
  }

   /**
   * Hash for this object
   * TODO: todo
   * 
   * @return {@link int}
   */
  @Override
  public int hashCode() {
    /** Todo:
     *    - Hash with the id+timestamp
     **/
    return 0;
  }


  public Message setResponseBody(String body) {
    this.body = body;
    return this;
  }

  public Message setKey(String key) {
    this.key = key;
    return this;
  }

  public Message setEventTimestamp(Instant timestamp) {
    /** Todo:
     *    - Return Instance time
     **/
    this.timestamp = timestamp;
    return this;
  }

   /**
   * Hash for this object
   * TODO: Some validations?
   * 
   * @return {@link int}
   */
  public long getEventTime() {
    /** Todo:
     *    - Return Instance time
     **/
    return timestamp.toEpochMilli();
  }

}
