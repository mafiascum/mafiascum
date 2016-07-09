package net.mafiascum.testcase;

import net.mafiascum.phpbb.usergroup.UserGroup;

import org.apache.log4j.Logger;
import org.junit.Assert;

public class UserGroupsTestCase extends TestCase {
  
  private static final Logger logger = Logger.getLogger(UserGroupsTestCase.class.getName());
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {

      logger.info("Testing User Groups.");
      int userId = 5932;
      int groupId = siteChatUtil.BANNED_USERS_GROUP_ID;
      
      logger.info("Deleting User Group.");
      siteChatUtil.deleteUserGroup(connection, userId, groupId);
      
      UserGroup userGroup = new UserGroup(true, groupId, userId, false, false, 0);
      
      logger.info("Storing New User Group.");
      siteChatUtil.putUserGroup(connection, userGroup);
      
      logger.info("Loading User Group.");
      UserGroup loadedUserGroup = siteChatUtil.getUserGroup(connection, userId, groupId);
      
      Assert.assertEquals(userGroup.getUserId(), loadedUserGroup.getUserId());
      Assert.assertEquals(userGroup.getGroupId(), loadedUserGroup.getGroupId());
      Assert.assertEquals(userGroup.getGroupLeader(), loadedUserGroup.getGroupLeader());
      Assert.assertEquals(userGroup.getUserPending(), loadedUserGroup.getUserPending());
      Assert.assertEquals(userGroup.getAutoRemoveTime(), loadedUserGroup.getAutoRemoveTime());
      
      logger.info("Deleting User Group.");
      siteChatUtil.deleteUserGroup(connection, userId, groupId);
      
      logger.info("Loading Deleted User Group.");
      UserGroup deletedUserGroup = siteChatUtil.getUserGroup(connection, userId, groupId);
      
      Assert.assertNull(deletedUserGroup);
    });
  }
}
