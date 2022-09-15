package in.org.iudx.adaptor.server.databroker;

public enum PermissionOpType {

  ADD_READ("read"),
  ADD_WRITE("write"),

  DELETE_READ("read"), 
  DELETE_WRITE("write");

  public String permission;

  PermissionOpType(String permission) {
    this.permission = permission;
  }
}
