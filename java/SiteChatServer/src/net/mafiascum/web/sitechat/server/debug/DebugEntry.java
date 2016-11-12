package net.mafiascum.web.sitechat.server.debug;

public class DebugEntry {

  protected String id;
  protected int initiatingUserId;
  protected int targetUserId;
  protected String code;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public int getInitiatingUserId() {
    return initiatingUserId;
  }
  public void setInitiatingUserId(int initiatingUserId) {
    this.initiatingUserId = initiatingUserId;
  }
  public int getTargetUserId() {
    return targetUserId;
  }
  public void setTargetUserId(int targetUserId) {
    this.targetUserId = targetUserId;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
}
