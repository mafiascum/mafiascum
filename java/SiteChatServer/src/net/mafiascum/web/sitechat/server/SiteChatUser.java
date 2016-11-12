package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.Table;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="phpbb_users")
public class SiteChatUser extends DataObjectWithIntID implements DataObject {
  
  protected String name;
  protected String avatarUrl;
  protected String userColor;
  
  public SiteChatUser() {
    
    id = NEW;
  }
  
  public SiteChatUser(SiteChatUser siteChatUser) {
    setName(siteChatUser.getName());
    setAvatarUrl(siteChatUser.getAvatarUrl());
    setId(siteChatUser.getId());
    setUserColor(siteChatUser.getUserColor());
  }
  
  public String getName() {
    
    return name;
  }
  
  public void setName(String name) {
    
    this.name = name;
  }
  
  public String getAvatarUrl() {
    
    return avatarUrl;
  }
  
  public void setAvatarUrl(String avatarUrl) {
    
    this.avatarUrl = avatarUrl;
  }
  
  public String getUserColor() {
    return userColor;
  }
  
  public void setUserColor(String userColor) {
    this.userColor = userColor;
  }

  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    setId(resultSet.getInt("user_id"));
    setName(resultSet.getString("username"));
    setAvatarUrl(resultSet.getString("user_avatar"));
    setUserColor(resultSet.getString("user_colour"));
  }
  
  public void store(Connection connection) throws SQLException {
    //Not implemented.
  }
}
