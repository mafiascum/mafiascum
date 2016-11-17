package net.mafiascum.web.sitechat.server.user;

import java.time.LocalDateTime;
import java.time.ZoneId;

import net.mafiascum.web.sitechat.server.SiteChatUser;

public class UserPacket {

  public int id;
  public String name;
  public String avatarUrl;
  public String userColor;
  public long lastActivityDatetime;
  
  public UserPacket(SiteChatUser user, LocalDateTime lastActivityDatetime) {
    this.id = user.getId();
    this.name = user.getName();
    this.avatarUrl = user.getAvatarUrl();
    this.userColor = user.getUserColor();
    this.lastActivityDatetime = lastActivityDatetime == null ? 0 : lastActivityDatetime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
  }
}
