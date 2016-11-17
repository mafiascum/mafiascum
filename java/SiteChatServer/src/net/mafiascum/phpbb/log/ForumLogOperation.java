package net.mafiascum.phpbb.log;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class ForumLogOperationStatic {
  
  static final J5EnumSet<ForumLogOperation> enumSet = new J5EnumSet<ForumLogOperation>();
  
  static synchronized void addEnum(ForumLogOperation enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
  }
}

public enum ForumLogOperation implements VEnum {
  
  siteChatIgnore(0, "Site Chat Ignore", "LOG_SC_IGNORE"  ),
  siteChatBan(1, "Site Chat Ban", "LOG_SC_BAN");
  
  private String standardName;
  private String languageKey;
  
  private ForumLogOperation(int id, String standardName, String languageKey) {
    this.standardName = standardName;
    this.languageKey = languageKey;
    ForumLogOperationStatic.addEnum(this, id, standardName);
  }
  
  public int value() {
    return ForumLogOperationStatic.enumSet.getValue(this);
  }
  
  public String toString() {
    return ForumLogOperationStatic.enumSet.toString(this);
  }
  
  public String getStandardName() {
    return standardName;
  }
  
  public String getLanguageKey() {
    return languageKey;
  }
  
  public static ForumLogOperation getEnum(int value) throws IndexOutOfBoundsException {
    return ForumLogOperationStatic.enumSet.getEnum(value);
  }
  
  public static Iterator<ForumLogOperation> getSetIterator() {
    return ForumLogOperationStatic.enumSet.iterator();
  }
  
  public static VEnumSet getSet() {
    return ForumLogOperationStatic.enumSet;
  };
};