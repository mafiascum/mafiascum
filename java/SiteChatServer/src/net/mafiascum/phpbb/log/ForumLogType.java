package net.mafiascum.phpbb.log;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class ForumLogTypeStatic {
  
  static final J5EnumSet<ForumLogType> enumSet = new J5EnumSet<ForumLogType>();
  
  static synchronized void addEnum(ForumLogType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
  }
}

public enum ForumLogType implements VEnum {
  
  admin(0, "Admin"  ),
  mod(1, "Mod"),
  critical(2, "Critical"),
  users(3, "Users");
  
  private String standardName;
  
  private ForumLogType(int id, String standardName) {
    this.standardName = standardName;
    ForumLogTypeStatic.addEnum(this, id, standardName);
  }
  
  public int value() {
    return ForumLogTypeStatic.enumSet.getValue(this);
  }
  
  public String toString() {
    return ForumLogTypeStatic.enumSet.toString(this);
  }
  
  public String getStandardName() {
    return standardName;
  }
  
  public static ForumLogType getEnum(int value) throws IndexOutOfBoundsException {
    return ForumLogTypeStatic.enumSet.getEnum(value);
  }
  
  public static Iterator<ForumLogType> getSetIterator() {
    return ForumLogTypeStatic.enumSet.iterator();
  }
  
  public static VEnumSet getSet() {
    return ForumLogTypeStatic.enumSet;
  };
};