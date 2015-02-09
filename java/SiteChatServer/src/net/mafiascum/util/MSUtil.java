package net.mafiascum.util;

import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class MSUtil {

  protected DateUtil dateUtil;
  protected MiscUtil miscUtil;
  protected QueryUtil queryUtil;
  protected SQLUtil sqlUtil;
  protected StringUtil stringUtil;
  protected ThreadUtil threadUtil;
  protected SiteChatUtil siteChatUtil;
  
  protected void init() {
    setDateUtil(DateUtil.get());
    setMiscUtil(MiscUtil.get());
    setQueryUtil(QueryUtil.get());
    setSQLUtil(SQLUtil.get());
    setStringUtil(StringUtil.get());
    setThreadUtil(ThreadUtil.get());
    setSiteChatUtil(SiteChatUtil.get());
  }
  
  public DateUtil getDateUtil() {
    return dateUtil;
  }
  public void setDateUtil(DateUtil dateUtil) {
    this.dateUtil = dateUtil;
  }
  public MiscUtil getMiscUtil() {
    return miscUtil;
  }
  public void setMiscUtil(MiscUtil miscUtil) {
    this.miscUtil = miscUtil;
  }
  public QueryUtil getQueryUtil() {
    return queryUtil;
  }
  public void setQueryUtil(QueryUtil queryUtil) {
    this.queryUtil = queryUtil;
  }
  public SQLUtil getSQLUtil() {
    return sqlUtil;
  }
  public void setSQLUtil(SQLUtil sqlUtil) {
    this.sqlUtil = sqlUtil;
  }
  public StringUtil getStringUtil() {
    return stringUtil;
  }
  public void setStringUtil(StringUtil stringUtil) {
    this.stringUtil = stringUtil;
  }
  public ThreadUtil getThreadUtil() {
    return threadUtil;
  }
  public void setThreadUtil(ThreadUtil threadUtil) {
    this.threadUtil = threadUtil;
  }
  public SiteChatUtil getSiteChatUtil() {
    return siteChatUtil;
  }
  public void setSiteChatUtil(SiteChatUtil siteChatUtil) {
    this.siteChatUtil = siteChatUtil;
  }
}
