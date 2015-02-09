package net.mafiascum.web.misc;

public class DataObjectWithShortID {
  
  protected short id;
  protected static short NEW = -1;
  
  public DataObjectWithShortID() {
    
    id = NEW;
  }
  
  public boolean isNew() {
    
    return id == NEW;
  }
  
  public short getId() {
    
    return id;
  }
  
  public void setId(short id) {
    
    this.id = id;
  }
}
