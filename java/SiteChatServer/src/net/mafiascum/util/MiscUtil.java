package net.mafiascum.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Contains miscellaneous utility functions. */
public class MiscUtil extends MSUtil {

  private static MiscUtil INSTANCE;
  
  private MiscUtil() {
    super();
  }
  
  public static synchronized MiscUtil get() {
    
    if(INSTANCE == null) {
      INSTANCE = new MiscUtil();
      INSTANCE.init();
    }
    return INSTANCE;
  }
  
  private char[] defaultRandomIDCharacterSet = null;
  private char[] numericRandomIDCharacterSet = null;
  
  public boolean strictParseBoolean (String text, boolean allowMixedCase) throws IllegalArgumentException {
    if (allowMixedCase)
      text = text.toLowerCase();

    if (text.equals("true"))
      return true;
    else if (text.equals("false"))
      return false;
    else
      throw new IllegalArgumentException("Invalid value: " + text);
  }

  public boolean strictParseBooleanInt (String text) throws IllegalArgumentException {
    if (text == null || text.equals(""))
      return false;

    if (text.equals("1"))
      return true;
    else if (text.equals("0"))
      return false;
    else
      throw new IllegalArgumentException("Invalid value: " + text);
  }

  public Boolean strictParseBooleanObjInt (String text) throws IllegalArgumentException {
    if (text == null || text.equals(""))
      return null;

    if (text.equals("1"))
      return true;
    else if (text.equals("0"))
      return false;
    else
      throw new IllegalArgumentException("Invalid value: " + text);
  }

  /** Retrieves the stack trace as a human readable string. */
  public String getPrintableStackTrace (Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    pw.flush();
    return sw.toString();
  }

  public String getPrintableStackTrace(StackTraceElement[] stackTraceElements) {

    StringBuilder stringBuilder = new StringBuilder();
    for(StackTraceElement stackTraceElement : stackTraceElements) {

      stringBuilder.append("\t").append(stackTraceElement.toString()).append("\n");
    }
    return stringBuilder.toString();
  }
  
  /** Generates a Random ID using upper- and lower-case letters and digits. */
  public String createRandomID (int length) {
    if (defaultRandomIDCharacterSet == null) {
      defaultRandomIDCharacterSet = new char[26 + 26 + 10];
      int i = 0;
      for (char ch = '0'; ch <= '9'; ch++)
        defaultRandomIDCharacterSet[i++] = ch;
      for (char ch = 'A'; ch <= 'Z'; ch++)
        defaultRandomIDCharacterSet[i++] = ch;
      for (char ch = 'a'; ch <= 'z'; ch++)
        defaultRandomIDCharacterSet[i++] = ch;
    }

    return createRandomID(length, defaultRandomIDCharacterSet);
  }

  /** Generates a Random ID using upper- and lower-case letters and digits. */
  public String createRandomNumericID (int length) {
    if (numericRandomIDCharacterSet == null) {
      numericRandomIDCharacterSet = new char[10];
      int i = 0;
      for (char ch = '0'; ch <= '9'; ch++)
        numericRandomIDCharacterSet[i++] = ch;
    }

    return createRandomID(length, numericRandomIDCharacterSet);
  }

  /** Generates a Random ID consiting of characters from the specified set. */
  public String createRandomID (int length, char[] charSet) {
    SecureRandom random = new SecureRandom();

    StringBuffer id = new StringBuffer();
    while (id.length() < length) {
      int index = random.nextInt(charSet.length);
      char ch = charSet[index];
      id.append(ch);
    }

    return id.toString();
  }

  /** Performs a deep copy using serialization (slow). */
  public Object serializationDeepCopy (Object object) throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(object);
    objectOutputStream.close();
    byte[] serializedData = byteArrayOutputStream.toByteArray();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedData);
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    return objectInputStream.readObject();
  }

  /** Builds a list containing the single object specified. */
  public <T> List<T> makeSingleObjectList (T object) {
    List<T> list = new ArrayList<T>();
    list.add(object);
    return list;
  }

  public <Type> Set<Type> makeSingleTypedSet (Type object) {
    Set<Type> set = new HashSet<Type>();
    set.add(object);
    return set;
  }

  public <Type> Set<Type> makeHashSet(@SuppressWarnings("unchecked") Type ... args) {

    Set<Type> hashSet = new HashSet<Type>();

    for(Type type : args) {

      hashSet.add(type);
    }

    return hashSet;
  }

  /** Builds a list containing the single typed object specified. */
  public <Type> List<Type> makeSingleTypedList (Type object) {
    List<Type> list = new LinkedList<Type>();
    list.add(object);
    return list;
  }

  public <ElementType> ElementType[] mergeArrays (Class<ElementType[]> arrayClass, ElementType[] array1, ElementType[] array2) {
    ElementType[] mergedArray = arrayClass.cast(Array.newInstance(arrayClass.getComponentType(), array1.length + array2.length));
    System.arraycopy(array1, 0, mergedArray, 0, array1.length);
    System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
    return mergedArray;
  }
  
  //Used to create a UniqueID, not for the request feedback email
  public String getUniqueID(String firstName, String lastName)
  {
    int total=0;
    int lastNameTotal = 0;
    for(int i=0;i<lastName.length();i++)
    {
      lastNameTotal+=(int)lastName.charAt(i);
    }                 
    int firstNameTotal = 0;
    for(int i=0;i<firstName.length();i++)
    {
      firstNameTotal+=(int)firstName.charAt(i);
    }   
    total=Math.abs(lastNameTotal-firstNameTotal);
    total = total + 10000;
    total = total ^ 8;
    total = total ^ 256;
    total = total ^ 1024;
    return String.valueOf(total);
  }

  /** An compare method that handles either object being null. */
  public <Type extends Comparable<Type>> int compare (Type object1, Type object2, boolean nullIsLessThan) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      }
      else {
        return nullIsLessThan ? -1 : +1;
      }
    }
    else {
      if (object2 == null) {
        return nullIsLessThan ? +1 : -1;
      }
      else {
        return object1.compareTo(object2);
      }
    }
  }

  /** An equals comparison that handles either object being null. */
  public boolean equals (Object object1, Object object2) {
    return (object1 == null)
        ? object2 == null
        : object1.equals(object2);
  }

  /** A hashcode producer that handles null objects. */
  public int hashCode (Object object) {
    return (object != null) ? object.hashCode() : 0;
  }

  /** Compares the trimmed values of two Strings, treating null String as empty Strings. */
  public boolean removeNullTrimEquals (String value1, String value2) {
    value1 = stringUtil.removeNull(value1).trim();
    value2 = stringUtil.removeNull(value2).trim();
    return value1.equals(value2);
  }
  
  /**
   * Locates and opens the specified file as an InputStream.  If the specified
   * file is not found or fails to open, an exception is thrown.
   * 
   * @param url A path specifying the resource location within the classpath.
   */
  public InputStream getResourceStream (String url) throws Exception {

    if (url.startsWith("/")) {
      InputStream ins = MiscUtil.class.getResourceAsStream(url);
      if (ins == null)
        throw new FileNotFoundException("Failed to find resource \"" + url + "\"");
      return ins;
    }

    URL urlObject = new URL(url);
    return urlObject.openStream();
  }
  
  /**
   * Locates and loads the specified property files.  If the specified file
   * is not found or fails to load, an exception is thrown.
   * 
   * @param url A path specifying the resource location within the classpath.
   */
  public Properties loadPropertiesResource (String url) throws Exception {
    InputStream ins = getResourceStream(url);
    Properties properties = new Properties();
    properties.load(ins);
    ins.close();
      
    return properties;
  }
  
  public Short[] parseIPAddress (String ipAddress, boolean returnNullWhenNoComponents) {

    Short[] components = new Short[4];

    if (ipAddress != null) {
      String[] componentStrings = ipAddress.split("\\.");
      int numComponents = Math.min(componentStrings.length, 4);
      try {
        for (int i = 0; i < numComponents; i++) {
          components[i] = new Short(componentStrings[i]);
        }
      }
      catch (NumberFormatException nfe) {
        // Ignore - leave any other components unparsed
      }
    }

    if (returnNullWhenNoComponents && components[0] == null)
      return null;

    return components;
  }
  
  @SuppressWarnings("unchecked")
  public <Type> Type[] toArray (List<Type> list, Class<Type> typeClass) {

    Type[] array = (Type[]) Array.newInstance(typeClass, list.size());
    list.toArray(array);
    return array;
  }
  
  public <Type> ArrayList<Type> makeArrayList(Type[] array) {

    ArrayList<Type> arrayList = new ArrayList<Type>(array.length);

    for(Type type : array) {

      arrayList.add(type);
    }

    return arrayList;
  }

  public <Type> ArrayList<Type> makeArrayList(Collection<Type> collection) {

    ArrayList<Type> arrayList = new ArrayList<Type>(collection.size());
    arrayList.addAll(collection);
    return arrayList;
  }

  public <Key, Value> List<Value> convertMapToList(Map<Key, Value> map) {

    return convertMapToList(map, null);
  }

  public <Key, Value> List<Value> convertMapToList(Map<Key, Value> map, Comparator<Value> comparator) {

    List<Value> list = new ArrayList<Value>();

    list.addAll(map.values());

    if(comparator != null) {

      Collections.sort(list, comparator);
    }

    return list;
  }
  
  public <KeyType, PassingType> Map<KeyType, PassingType> map(Collection<PassingType> list, Function<PassingType, KeyType> getKeyFunction) {
    
    Map<KeyType, PassingType> map = new HashMap<KeyType, PassingType>();
    
    for(PassingType listObject : list) {
      map.put(getKeyFunction.apply(listObject), listObject);
    }
    
    return map;
  }
  
  public <KeyType, PassingType> Map<KeyType, List<PassingType>> createChildMap(Map<KeyType, PassingType> map, Function<PassingType, KeyType> getParentKeyFunction) {
    
    Map<KeyType, List<PassingType>> childMap = new HashMap<KeyType, List<PassingType>>();
    
    for(KeyType key : map.keySet()) {
      
      PassingType mapEntry = map.get(key);
      KeyType parentKey = getParentKeyFunction.apply(mapEntry);
      
      List<PassingType> childList = childMap.get(parentKey);
      
      if(childList == null) {
        
        childList = new ArrayList<PassingType>();
        childMap.put(parentKey, childList);
      }
      
      childList.add(mapEntry);
      
      //Create empty list for this current node.
      childList = childMap.get(key);
      
      if(childList == null) {
        childMap.put(key, new ArrayList<PassingType>());
      }
    }
    
    return childMap;
  }
  
  public <ObjectType, KeyType, ValueType> Map<KeyType, List<ValueType>> createGroupedMap(Collection<ObjectType> collection, Function<ObjectType, KeyType> keyRetrievalFunction, Function<ObjectType, ValueType> valueRetrievalFunction) {
    
    Map<KeyType, List<ValueType>> map = new HashMap<KeyType, List<ValueType>>();
    
    for(ObjectType objectInCollection : collection) {
      
      KeyType key = keyRetrievalFunction.apply(objectInCollection);
      ValueType value = valueRetrievalFunction.apply(objectInCollection);
      
      List<ValueType> list = map.get(key);
      
      if(list == null) {
        list = new ArrayList<ValueType>();
        map.put(key, list);
      }
      
      list.add(value);
    }
    
    return map;
  }
  
  public <ObjectType> List<List<ObjectType>> createGrid(List<ObjectType> list, int entriesPerRow) {
    
    List<List<ObjectType>> grid = new ArrayList<List<ObjectType>>();
    List<ObjectType> currentRow = null;
    
    for(ObjectType object : list) {
      
      if(currentRow == null || currentRow.size() >= entriesPerRow) {
        
        currentRow = new ArrayList<ObjectType>();
        grid.add(currentRow);
      }
      
      currentRow.add(object);
    }
    
    return grid;
  }
  
  public <ObjectType> List<List<ObjectType>> createGrid(List<ObjectType> list, int entriesPerRow, boolean fillEmptyEntries) {
    
    List<List<ObjectType>> grid = createGrid(list, entriesPerRow);
    
    if(fillEmptyEntries && !grid.isEmpty()) {
      List<ObjectType> lastRow = grid.get(grid.size() - 1);
      
      while(lastRow.size() < entriesPerRow) {
        
        lastRow.add(null);
      }
    }
    
    return grid;
  }
  
  public <PassingType, TransformedType> List<TransformedType> transformToList(Collection<PassingType> collection, Function<PassingType, TransformedType> transformationFunction) {
    
    return collection.stream().map(transformationFunction).collect(Collectors.toList());
  }
  
  public <PassingType, TransformedType> Set<TransformedType> transformToSet(Collection<PassingType> collection, Function<PassingType, TransformedType> transformationFunction) {
    
    return collection.stream().map(transformationFunction).collect(Collectors.toSet());
  }
  
  public <KeyType, ValueType> List<ValueType> pushToListMap(KeyType key, Map<KeyType, List<ValueType>> map) {
    
    List<ValueType> list = map.get(key);
    
    if(list == null) {
      list = new ArrayList<ValueType>();
      map.put(key, list);
    }
    
    return list;
  }
  
  public <KeyType, ValueType> void pushToListMap(KeyType key, ValueType value, Map<KeyType, List<ValueType>> map) {
    
    List<ValueType> list = map.get(key);
    
    if(list == null) {
      list = new ArrayList<ValueType>();
      map.put(key, list);
    }
    
    list.add(value);
  }
  
  public <T> int findMaxInt(Collection<T> collection, Function<T, Integer> function) {
    
    int max = Integer.MIN_VALUE;
    for(T t : collection) {
      
      int value = function.apply(t);
      
      if(value > max)
         max = value;
    }
    return max;
  }
 }