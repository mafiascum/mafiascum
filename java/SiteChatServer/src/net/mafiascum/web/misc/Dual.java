package net.mafiascum.web.misc;

public class Dual<T,U> {

  protected T object1;
  protected U object2;
  
  public Dual(T object1, U object2) {
    
    setObject1(object1);
    setObject2(object2);
  }
  
  public T getObject1() {
    
    return object1;
  }
  
  public void setObject1(T object1) {

    this.object1 = object1;
  }
  
  public U getObject2() {
    
    return object2;
  }
  
  public void setObject2(U object2) {
    
    this.object2 = object2;
  }
  
  public boolean equals(Object otherObject) {
   
    if(otherObject == this)
      return true;
    if(otherObject instanceof Dual == false || otherObject == null)
      return false;
    
    @SuppressWarnings("rawtypes")
    Dual dual = (Dual)otherObject;
    
    if(dual.getObject1() == null)
      return getObject1() == null;
    if(dual.getObject2() == null)
      return getObject2() == null;
    
    return dual.getObject1().equals(getObject1()) && dual.getObject2().equals(getObject2());
  }
}
