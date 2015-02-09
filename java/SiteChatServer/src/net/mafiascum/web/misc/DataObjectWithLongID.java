package net.mafiascum.web.misc;

public class DataObjectWithLongID {
  
  protected long id;
  protected static long NEW = -1;
  
  public DataObjectWithLongID() {
    
    id = NEW;
  }
  
  public boolean isNew() {
    
    return id == NEW;
  }
  
  public long getId() {
    
    return id;
  }
  
  public void setId(long id) {
    
    this.id = id;
  }
}
