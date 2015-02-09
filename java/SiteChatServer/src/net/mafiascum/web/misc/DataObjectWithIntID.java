package net.mafiascum.web.misc;


public class DataObjectWithIntID {
  
  protected int id;
  protected static int NEW = -1;
  
  public DataObjectWithIntID() {
    
    id = NEW;
  }
  
  public boolean isNew() {
    
    return id == NEW;
  }
  
  public int getId() {
    
    return id;
  }
  
  public void setId(int id) {
    
    this.id = id;
  }
}
