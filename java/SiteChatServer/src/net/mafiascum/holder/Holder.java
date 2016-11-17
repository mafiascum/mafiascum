package net.mafiascum.holder;

public class Holder<T> {
  
  protected T value;
  
  public Holder() {
  }
  public Holder(T value) {
    setValue(value);
  }
  public T getValue() {
    return value;
  }
  public void setValue(T value) {
    this.value = value;
  }
}
