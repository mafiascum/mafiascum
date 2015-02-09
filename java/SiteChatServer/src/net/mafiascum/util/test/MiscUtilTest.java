package net.mafiascum.util.test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.web.misc.Dual;

import org.junit.Assert;
import org.junit.Test;

public class MiscUtilTest {

  protected MiscUtil miscUtil = MiscUtil.get();
  
  @Test
  public void testConvertMapToList() {

    Map<Integer, String> map = new HashMap<Integer, String>();
    
    map.put(5, "5");
    map.put(10, "10");
    map.put(2, "2");
    map.put(20, "20");
    map.put(15, "15");
    
    Comparator<String> comparator = (String value1, String value2) -> {
      return Integer.valueOf(value1).compareTo(Integer.valueOf(value2));
    };
    
    List<String> list = miscUtil.convertMapToList(map, comparator);
    
    Assert.assertEquals(map.size(), list.size());
    
    Integer last = null;
    
    for(String value : list) {
      
      int valueInt = Integer.valueOf(value);
      
      Assert.assertTrue(last == null || last <= valueInt);
      Assert.assertEquals(map.get(valueInt), value);
      
      last = valueInt;
    }
  }
  
  @Test
  public void testMakeArrayList() {
    
    List<Integer> list = miscUtil.makeArrayList(new Integer[] { 1, 2, 5, 0 });
    
    Assert.assertEquals(4, list.size());
    Assert.assertEquals(1, list.get(0).intValue());
    Assert.assertEquals(2, list.get(1).intValue());
    Assert.assertEquals(5, list.get(2).intValue());
    Assert.assertEquals(0, list.get(3).intValue());
  }
  
  @Test
  public void testToArray() {

    List<String> list = Arrays.asList("ABC", "FFF", "123", "daskdasldk", "Sparky Rules!!");
    String[] stringArray = miscUtil.toArray(list, String.class);
    
    Assert.assertEquals(list.size(), stringArray.length);
    
    for(int index = 0;index < list.size();++index) {
      
      Assert.assertEquals(list.get(index), stringArray[index]);
    }
  }
  
  @Test
  public void testParseIPAddress() {
    
    Assert.assertArrayEquals(new Short[] {192, 168, 1, 1}, miscUtil.parseIPAddress("192.168.1.1", true));
    Assert.assertArrayEquals(new Short[] {192, null, null, null}, miscUtil.parseIPAddress("192.*.*.*", true));
    Assert.assertArrayEquals(new Short[] {192, null, null, null}, miscUtil.parseIPAddress("192.*", true));
    Assert.assertArrayEquals(null, miscUtil.parseIPAddress("", true));
    Assert.assertArrayEquals(new Short[] {null, null, null, null}, miscUtil.parseIPAddress("", false));
  }
  
  @Test
  public void testMap() {
    
    final class Entry {
      
      int id;
      String name;
      
      public Entry(int id, String name) {
        this.id = id;
        this.name = name;
      }
      
      public int getId() {
        return id;
      }
    }
    
    List<Entry> entries = Arrays.asList(
        new Entry(1, "Entry 1"),
        new Entry(2, "Entry 2"),
        new Entry(3, "Entry 3")
    );
    
    Map<Integer, Entry> entryMap = miscUtil.map(entries, Entry::getId);
    
    Assert.assertEquals(3, entryMap.size());
    Assert.assertEquals("Entry 1", entryMap.get(1).name);
    Assert.assertEquals("Entry 2", entryMap.get(2).name);
    Assert.assertEquals("Entry 3", entryMap.get(3).name);
  }
  
  @Test
  public void testCreateChildMap() {
    
    final class Node {
      
      int id;
      Integer parentId;
      
      public Node(int id, Integer parentId) {
        this.id = id;
        this.parentId = parentId;
      }
      
      public Integer getParentId() {
        return parentId;
      }
      
      public int getId() {
        return id;
      }
    };
    
    List<Node> nodes = Arrays.asList(
        new Node(1, null),
          new Node(2, 1),
          new Node(3, 1),
          new Node(4, 1),
            new Node(5, 4),
        new Node(6, null),
        new Node(7, null),
          new Node(8, 7)
    );
    
    Map<Integer, Node> nodeMap = miscUtil.map(nodes, Node::getId);
    Map<Integer, List<Node>> childMap = miscUtil.createChildMap(nodeMap, Node::getParentId);
    
    Assert.assertEquals(3, childMap.get(1).size());
    Assert.assertEquals(0, childMap.get(2).size());
    Assert.assertEquals(0, childMap.get(3).size());
    Assert.assertEquals(1, childMap.get(4).size());
    Assert.assertEquals(0, childMap.get(5).size());
    Assert.assertEquals(0, childMap.get(6).size());
    Assert.assertEquals(1, childMap.get(7).size());
    Assert.assertEquals(0, childMap.get(8).size());
    Assert.assertEquals(3, childMap.get(null).size());
    
    Assert.assertTrue(childMap.get(1).stream().anyMatch(node -> node.getId() == 2));
    Assert.assertTrue(childMap.get(1).stream().anyMatch(node -> node.getId() == 3));
    Assert.assertTrue(childMap.get(1).stream().anyMatch(node -> node.getId() == 4));
    
    Assert.assertTrue(childMap.get(4).stream().anyMatch(node -> node.getId() == 5));
    
    Assert.assertTrue(childMap.get(7).stream().anyMatch(node -> node.getId() == 8));
  }
  
  @Test
  public void testGroupedByMap() {
    
    List<Dual<Integer, String>> duals = Arrays.asList(
        new Dual<Integer, String>(1, "A"),
        new Dual<Integer, String>(1, "B"),
        new Dual<Integer, String>(1, "C"),
        new Dual<Integer, String>(2, "D"),
        new Dual<Integer, String>(3, "E"),
        new Dual<Integer, String>(3, "F")
    );
    
    Map<Integer, List<String>> groupedMap = miscUtil.createGroupedMap(
        duals,
        Dual<Integer, String>::getObject1,
        Dual<Integer, String>::getObject2
    );
    
    Assert.assertEquals(3, groupedMap.size());
    Assert.assertEquals(3, groupedMap.get(1).size());
    Assert.assertEquals(1, groupedMap.get(2).size());
    Assert.assertEquals(2, groupedMap.get(3).size());
    Assert.assertEquals("A", groupedMap.get(1).stream().filter(str -> str.equals("A")).findFirst().get());
    Assert.assertEquals("B", groupedMap.get(1).stream().filter(str -> str.equals("B")).findFirst().get());
    Assert.assertEquals("C", groupedMap.get(1).stream().filter(str -> str.equals("C")).findFirst().get());
    Assert.assertEquals("D", groupedMap.get(2).stream().filter(str -> str.equals("D")).findFirst().get());
    Assert.assertEquals("E", groupedMap.get(3).stream().filter(str -> str.equals("E")).findFirst().get());
    Assert.assertEquals("F", groupedMap.get(3).stream().filter(str -> str.equals("F")).findFirst().get());
  }
  
  @Test
  public void testCreateGrid() {
    
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    List<List<Integer>> grid = miscUtil.createGrid(list, 3);
    
    Assert.assertEquals(3, grid.size());
    Assert.assertEquals(3, grid.get(0).size());
    Assert.assertEquals(3, grid.get(1).size());
    Assert.assertEquals(1, grid.get(2).size());
    
    Assert.assertEquals(1, grid.get(0).get(0).intValue());
    Assert.assertEquals(2, grid.get(0).get(1).intValue());
    Assert.assertEquals(3, grid.get(0).get(2).intValue());
    Assert.assertEquals(4, grid.get(1).get(0).intValue());
    Assert.assertEquals(5, grid.get(1).get(1).intValue());
    Assert.assertEquals(6, grid.get(1).get(2).intValue());
    Assert.assertEquals(7, grid.get(2).get(0).intValue());
  }
  
  @Test
  public void testCreateGridEqualSizedRows() {
    
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    List<List<Integer>> grid = miscUtil.createGrid(list, 3, true);
    
    Assert.assertEquals(3, grid.get(2).size());
    Assert.assertEquals(7, grid.get(2).get(0).intValue());
    Assert.assertEquals(null, grid.get(2).get(1));
    Assert.assertEquals(null, grid.get(2).get(2));
  }
  
  @Test
  public void testPushToListMap_ExistingKey() {
    
    Map<Integer, List<String>> listMap = new HashMap<Integer, List<String>>();
    
    listMap.put(1, Arrays.asList("1", "2"));
    listMap.put(2, Arrays.asList("3"));
    
    miscUtil.pushToListMap(2, listMap);
    
    Assert.assertEquals(2, listMap.size());
  }
  
  @Test
  public void testPushToListMap_NewKey() {
    Map<Integer, List<String>> listMap = new HashMap<Integer, List<String>>();
    
    listMap.put(1, Arrays.asList("1", "2"));
    listMap.put(2, Arrays.asList("3"));
    
    miscUtil.pushToListMap(3, listMap);
    
    Assert.assertEquals(3, listMap.size());
    Assert.assertEquals(true, listMap.containsKey(3));
  }
  
  @Test
  public void testFindMaxInt() {
    
    List<String> strings = Arrays.asList("abc", "abcde", "a", "abcdefg", "ab");
    
    Assert.assertEquals(7, miscUtil.findMaxInt(strings, String::length));
  }
}
