package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public abstract class SiteChatInboundPacketOperator {

  protected StringUtil stringUtil;
  protected MiscUtil miscUtil;
  protected SiteChatUtil siteChatUtil;
  
  public abstract void process(SiteChatMessageProcessor processor, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception;
  
  public SiteChatInboundPacketOperator() {
    setStringUtil(StringUtil.get());
    setMiscUtil(MiscUtil.get());
    setSiteChatUtil(SiteChatUtil.get());
  }
  
  public SiteChatUtil getSiteChatUtil() {
    return siteChatUtil;
  }
  
  public void setSiteChatUtil(SiteChatUtil siteChatUtil) {
    this.siteChatUtil = siteChatUtil;
  }

  public StringUtil getStringUtil() {
    return stringUtil;
  }

  public void setStringUtil(StringUtil stringUtil) {
    this.stringUtil = stringUtil;
  }

  public MiscUtil getMiscUtil() {
    return miscUtil;
  }

  public void setMiscUtil(MiscUtil miscUtil) {
    this.miscUtil = miscUtil;
  }
}
