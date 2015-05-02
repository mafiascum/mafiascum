package net.mafiascum.testcase;

import java.util.Set;

import net.mafiascum.phpbb.usergroup.UserGroup;

import org.junit.Assert;

public class BannedUserIDSetTestCase extends TestCase {
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoReturn(provider, connection -> {
      
      final int USER_ID = 5932;
      
      siteChatUtil.deleteUserGroup(connection, USER_ID, siteChatUtil.BANNED_USERS_GROUP_ID);
      
      Set<Integer> bannedUserIdSet = siteChatUtil.getBannedUserIdSet(connection);
      
      Assert.assertFalse(bannedUserIdSet.contains(USER_ID));
      
      siteChatUtil.putUserGroup(connection, new UserGroup(true, siteChatUtil.BANNED_USERS_GROUP_ID, USER_ID, false, false, 0));
      
      bannedUserIdSet = siteChatUtil.getBannedUserIdSet(connection);
      
      Assert.assertTrue(bannedUserIdSet.contains(USER_ID));
    });
  }
}
