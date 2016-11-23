package net.mafiascum.web.sitechat.server.event;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class SiteChatServerEventTypeStatic {
  
  static final J5EnumSet<SiteChatServerEventType> enumSet = new J5EnumSet<SiteChatServerEventType>();
  
  static synchronized void addEnum(SiteChatServerEventType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
  }
}

public enum SiteChatServerEventType implements VEnum {
  
  open   (0, "Open"   ),
  message(1, "Message"),
  close  (2, "Close"  );
  
  private String standardName;
  
  private SiteChatServerEventType(int id, String standardName) {
    this.standardName = standardName;
    SiteChatServerEventTypeStatic.addEnum(this, id, standardName);
  }
  
  public int value() {
    return SiteChatServerEventTypeStatic.enumSet.getValue(this);
  }
  
  public String toString() {
    return SiteChatServerEventTypeStatic.enumSet.toString(this);
  }
  
  public String getStandardName() {
    return standardName;
  }
  
  public static SiteChatServerEventType getEnum(int value) throws IndexOutOfBoundsException {
    return SiteChatServerEventTypeStatic.enumSet.getEnum(value);
  }
  
  public static Iterator<SiteChatServerEventType> getSetIterator() {
    return SiteChatServerEventTypeStatic.enumSet.iterator();
  }
  
  public static VEnumSet getSet() {
    return SiteChatServerEventTypeStatic.enumSet;
  };
};