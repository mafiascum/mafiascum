package net.mafiascum.web.sitechat.server.conversation;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.NameValueItemSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class SiteChatConversationTypeStatic {

  static final J5EnumSet<SiteChatConversationType> enumSet = new J5EnumSet<SiteChatConversationType>();

  static final NameValueItemSet standardNameValueItemSet = new NameValueItemSet();

  static synchronized void addEnum (SiteChatConversationType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
    standardNameValueItemSet.addItem(name, enumRef);
  }
}

public enum SiteChatConversationType implements VEnum {

  Private(0, "Private", 'P'),
  Conversation(1, "Conversation", 'C');
  
  private String standardName;
  private char symbol;

  private SiteChatConversationType (int id, String standardName, char symbol) {
    this.standardName = standardName;
    this.symbol = symbol;
    SiteChatConversationTypeStatic.addEnum(this, id, standardName);
  }

  public int value () { return SiteChatConversationTypeStatic.enumSet.getValue(this); }
  public String toString () { return SiteChatConversationTypeStatic.enumSet.toString(this); }

  public String getStandardName () { return standardName; }
  public char getSymbol() { return symbol; }

  public static SiteChatConversationType getEnum(int value) throws IndexOutOfBoundsException { return SiteChatConversationTypeStatic.enumSet.getEnum(value); }
  public static SiteChatConversationType getEnumByStandardName(String standardName) {
    
    Iterator<SiteChatConversationType> iter = getSetIterator();
    while(iter.hasNext()) {
      
      SiteChatConversationType SiteChatConversationType = iter.next();
      
      if(SiteChatConversationType.getStandardName().equals(standardName)) {
        
        return SiteChatConversationType;
      }
    }
    
    return null;
  }
  public static Iterator<SiteChatConversationType> getSetIterator () { return SiteChatConversationTypeStatic.enumSet.iterator(); }
  public static VEnumSet getSet () { return SiteChatConversationTypeStatic.enumSet; };

  public static NameValueItemSet getStandardNameValueItemSet () { return SiteChatConversationTypeStatic.standardNameValueItemSet; }
};
