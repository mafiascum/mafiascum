package net.mafiascum.web.sitechat.server.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DebugManager {

  Map<String, DebugEntry> debugEntryMap = new HashMap<>();
  
  public DebugManager() {
    
  }
  
  public DebugEntry submitDebugEntry(int initiatingUserId, int targetUserId, String code) {
    DebugEntry entry = new DebugEntry();
    
    entry.setId(UUID.randomUUID().toString());
    entry.setCode(code);
    entry.setInitiatingUserId(initiatingUserId);
    entry.setTargetUserId(targetUserId);
    
    debugEntryMap.put(entry.getId(), entry);
    
    return entry;
  }
  
  public DebugEntry getEntry(String id) {
    return debugEntryMap.get(id);
  }
  
  public void removeEntry(String id) {
    debugEntryMap.remove(id);
  }
}
