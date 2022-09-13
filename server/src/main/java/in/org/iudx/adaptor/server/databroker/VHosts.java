package in.org.iudx.adaptor.server.databroker;

/**
 * This enum contains a mapping for IUDX vhosts available with config json Keys in databroker
 * verticle.
 *
 */
public enum VHosts {


  IUDX_PROD("prodVhost"), IUDX_INTERNAL("internalVhost"), IUDX_EXTERNAL("externalVhost");

  public String value;

  VHosts(String value) {
    this.value = value;
  }

}
