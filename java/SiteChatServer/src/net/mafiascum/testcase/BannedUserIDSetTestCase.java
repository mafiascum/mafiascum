package net.mafiascum.testcase;

import java.util.List;

import net.mafiascum.phpbb.usergroup.UserGroup;

import org.junit.Assert;

public class BannedUserIDSetTestCase extends TestCase {
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {
      
      final int USER_ID = 5932;
      
      siteChatUtil.deleteUserGroup(connection, USER_ID, siteChatUtil.BANNED_USERS_GROUP_ID);
      
      List<UserGroup> bannedUserIdSet = siteChatUtil.getBanUserGroups(connection);
      
      Assert.assertFalse(bannedUserIdSet.stream().filter(entry -> entry.getUserId() == USER_ID).findFirst().isPresent());
      
      siteChatUtil.putUserGroup(connection, new UserGroup(true, siteChatUtil.BANNED_USERS_GROUP_ID, USER_ID, false, false, 0));
      
      bannedUserIdSet = siteChatUtil.getBanUserGroups(connection);
      
      Assert.assertTrue(bannedUserIdSet.stream().filter(entry -> entry.getUserId() == USER_ID).findFirst().isPresent());
    });
  }
}
