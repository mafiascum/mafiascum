package net.mafiascum.web.sitechat.server;

public class Descriptor {

  protected String id;
  protected String ipAddress;
  
  public Descriptor(String id, String ipAddress) {
    setId(id);
    setIpAddress(ipAddress);
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getIpAddress() {
    return ipAddress;
  }
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
}
