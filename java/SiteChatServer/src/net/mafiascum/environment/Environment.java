package net.mafiascum.environment;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.NameValueItemSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class EnvironmentStatic {

  static final J5EnumSet<Environment> enumSet = new J5EnumSet<Environment>();

  static final NameValueItemSet standardNameValueItemSet = new NameValueItemSet();

  static synchronized void addEnum (Environment enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
    standardNameValueItemSet.addItem(name, enumRef);
  }
}

public enum Environment implements VEnum {

  prod(0, "Production", "prod"),
  dev(1, "Development", "dev");
  
  private String standardName;
  private String abbreviatedName;

  private Environment (int id, String standardName, String abbreviatedName) {
    this.standardName = standardName;
    this.abbreviatedName = abbreviatedName;
    EnvironmentStatic.addEnum(this, id, standardName);
  }

  public int value () { return EnvironmentStatic.enumSet.getValue(this); }
  public String toString () { return EnvironmentStatic.enumSet.toString(this); }

  public String getStandardName () { return standardName; }
  public String getAbbreviatedName () { return abbreviatedName; }

  public static Environment getEnum(int value) throws IndexOutOfBoundsException { return EnvironmentStatic.enumSet.getEnum(value); }
  public static Environment getEnumByAbbreviatedName(String abbreviatedName) {
    
    Iterator<Environment> iter = getSetIterator();
    while(iter.hasNext()) {
      
      Environment Environment = iter.next();
      
      if(Environment.getAbbreviatedName().equals(abbreviatedName)) {
        
        return Environment;
      }
    }
    
    return null;
  }
  public static Iterator<Environment> getSetIterator () { return EnvironmentStatic.enumSet.iterator(); }
  public static VEnumSet getSet () { return EnvironmentStatic.enumSet; };

  public static NameValueItemSet getStandardNameValueItemSet () { return EnvironmentStatic.standardNameValueItemSet; }
};
