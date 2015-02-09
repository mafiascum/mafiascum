package net.mafiascum.util.test;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.mafiascum.util.StringUtil;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

  protected StringUtil stringUtil = StringUtil.get();
  
  @Test
  public void testGetSHA1() throws NoSuchAlgorithmException {
    
    Assert.assertEquals("8b45e4bd1c6acb88bebf6407d16205f567e62a3e", stringUtil.getSHA1("some string"));
  }
  
  @Test
  public void testBuildListFromString() {
    
    String str = "Dave Bateman, Michael Mason, Bobby Brannigan";
    
    Assert.assertEquals(Arrays.asList("Dave Bateman", "Michael Mason", "Bobby Brannigan"), stringUtil.buildListFromString(str, ", "));
    Assert.assertEquals(Arrays.asList("Dave Bateman"), stringUtil.buildListFromString("Dave Bateman", ", "));
  }
  
  @Test
  public void testReplaceExpression() {
    
    Assert.assertEquals(null, stringUtil.replaceExpression(null, "find", "replace"));
    Assert.assertEquals("replace replace replace replace", stringUtil.replaceExpression("find replace find replace", "find", "replace"));
  }
  
  @Test
  public void testReplaceSpaceWithNBSP() {
    
    Assert.assertEquals("This&nbsp;is&nbsp;a&nbsp;&nbsp;string.", stringUtil.replaceSpaceWithNBSP("This is a  string."));
  }
  
  @Test
  public void testEscapeHTMLCharacters() {
    
    Assert.assertEquals("This is &amp;&amp;&lt;&lt;&gt;&gt;&quot;&quot;&apos;&apos;a string", stringUtil.escapeHTMLCharacters("This is &&<<>>\"\"''a string"));
    Assert.assertEquals("Do not touch me!!", stringUtil.escapeHTMLCharacters("Do not touch me!!"));
  }
  
  @Test
  public void testMakeStringOf() {
    
    Assert.assertEquals("xxxxxxxxxx", stringUtil.makeStringOf('x', 10));
    Assert.assertEquals("", stringUtil.makeStringOf('x', 0));
  }
  
  @Test
  public void testAppendChar() {
    
    Assert.assertEquals("xxxmy string", stringUtil.appendChar("my string", 'x', 12, true));
    Assert.assertEquals("my stringxxx", stringUtil.appendChar("my string", 'x', 12, false));
    Assert.assertEquals("my stringxxxxxx", stringUtil.appendChar("my string", 'x', 15));
  }
  
  @Test
  public void testAppendSpace() {
    
    Assert.assertEquals("my string     ", stringUtil.appendSpace("my string", 14));
    Assert.assertEquals("my string", stringUtil.appendSpace("my string", 0));
  }
  
  @Test
  public void testRemoveNonPrintableChars() {
    
    Assert.assertEquals("Yo", stringUtil.removeNonPrintableChars("Yo\r"));
    Assert.assertEquals("Hello\tthere .\n", stringUtil.removeNonPrintableChars("Hello\tthere .\n"));
  }
  
  @Test
  public void testConvertHTMLCharsToAscii() {
    
    Assert.assertEquals("&\"!yp?", stringUtil.convertHTMLCharsToAscii("&amp;&quot;&#161;&#162;&#170;&#176;&#175;&#174;&#173;&#172;&#171;&#190;&#255;&#254;&#191;"));
  }
  
  @Test
  public void testRemoveNullString() {
    
    Assert.assertEquals("", stringUtil.removeNull((String)null));
    Assert.assertEquals("", stringUtil.removeNull(""));
    Assert.assertEquals("test", stringUtil.removeNull("test"));
  }
  
  @Test
  public void testRemoveNullStringArray() {
    
    Assert.assertArrayEquals(new String[] {"", "", "test", ""}, stringUtil.removeNull(new String[] {null, "", "test", null}));
    Assert.assertArrayEquals(new String[] {"test1", "test2", "test3", "test4"}, stringUtil.removeNull(new String[] {"test1", "test2", "test3", "test4"}));
  }
  
  @Test
  public void testIsNullOrEmptyTrimmedString() {
    
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString(""));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString(" "));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString("  "));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString("\n"));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString("\r"));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString("\t"));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString("  \r\n\t  "));
    Assert.assertTrue(stringUtil.isNullOrEmptyTrimmedString(null));
    Assert.assertFalse(stringUtil.isNullOrEmptyTrimmedString("."));
    Assert.assertFalse(stringUtil.isNullOrEmptyTrimmedString(" . "));
    Assert.assertFalse(stringUtil.isNullOrEmptyTrimmedString(" ."));
  }
  
  @Test
  public void testTruncateText() {
    
    Assert.assertEquals("test", stringUtil.truncateText("test", 4, "a"));
    Assert.assertEquals("tea", stringUtil.truncateText("test", 3, "a"));
    Assert.assertEquals("tes", stringUtil.truncateText("test", 3, null));
  }
  
  @Test
  public void testTruncateTextWithEllipses() {
    
    Assert.assertEquals("test", stringUtil.truncateTextWithEllipses("test", 4));
    Assert.assertEquals("t...", stringUtil.truncateTextWithEllipses("testt", 4));
  }
  
  @Test
  public void testNullIfEmpty() {
    
    Assert.assertEquals(null, stringUtil.nullIfEmpty(""));
    Assert.assertEquals(null, stringUtil.nullIfEmpty(null));
    Assert.assertEquals("test", stringUtil.nullIfEmpty("test"));
    Assert.assertEquals(" ", stringUtil.nullIfEmpty(" "));
  }
  
  @Test
  public void testNullIfEmptyTrimmed() {
    
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(""));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(" "));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed("\t"));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed("\n"));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed("\r"));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(" \r\n\t"));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(null));
    Assert.assertEquals(" .", stringUtil.nullIfEmptyTrimmed(" ."));
    Assert.assertEquals("test", stringUtil.nullIfEmptyTrimmed("test"));
    
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed("", true));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(" ", true));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed("\r\n\t ", true));
    Assert.assertEquals(null, stringUtil.nullIfEmptyTrimmed(null, true));
    Assert.assertEquals("test", stringUtil.nullIfEmptyTrimmed("test", true));
    Assert.assertEquals("test", stringUtil.nullIfEmptyTrimmed(" test ", true));
    Assert.assertEquals(" test ", stringUtil.nullIfEmptyTrimmed(" test ", false));
  }
  
  @Test
  public void testProperString() {
    
    Assert.assertEquals("Test", stringUtil.properString("test"));
    Assert.assertEquals("This is a sentence.", stringUtil.properString("this is a sentence."));
    Assert.assertEquals("This is A Sentence.", stringUtil.properString("this is A Sentence."));
  }
  
  @Test
  public void testRemoveCharFromString() {
    
    Assert.assertEquals("Ths s a strng.", stringUtil.removeCharFromString("This is a string.", 'i'));
    Assert.assertEquals("ThisIsAString", stringUtil.removeCharFromString("This.Is.A.String.", '.'));
    Assert.assertEquals("This is a string.", stringUtil.removeCharFromString("This is a string.", 'x'));
  }
  
  @Test
  public void testToLowerSubString() {
    
    Assert.assertEquals("This is A String.", stringUtil.toLowerSubString("This Is A String.", "Is"));
    Assert.assertEquals("hi there. hi THERE. hi ThErE.", stringUtil.toLowerSubString("HI there. HI THERE. HI ThErE.", "HI"));
    Assert.assertEquals("hi there. Hi THERE. hI ThErE.", stringUtil.toLowerSubString("HI there. Hi THERE. hI ThErE.", "HI"));
  }
  
  @Test
  public void testToUpperSubString() {
    
    Assert.assertEquals("This IS A String.", stringUtil.toUpperSubString("This Is A String.", "Is"));
    Assert.assertEquals("HI there. HI THERE. HI ThErE.", stringUtil.toUpperSubString("hi there. hi THERE. hi ThErE.", "hi"));
    Assert.assertEquals("HI there. Hi THERE. hI ThErE.", stringUtil.toUpperSubString("hi there. Hi THERE. hI ThErE.", "hi"));
  }
  
  @Test
  public void testRemoveIllegalChars() {
    
    Assert.assertEquals("test", stringUtil.removeIllegalChars("test"));
    Assert.assertEquals("test   test", stringUtil.removeIllegalChars("test\t\n|test"));
  }
  
  @Test
  public void testCreateLine() {
    
    Assert.assertEquals("Field1,Field2,Field3,Field4", stringUtil.createLine(new String[] {"Field1", "Field2", "Field3", "Field4" }, ','));
    Assert.assertEquals("Field1,Fie,ld2", stringUtil.createLine(new String[] {"Field1", "Fie,ld2"}, ','));
  }
  
  @Test
  public void testEncodeHTMLCharacters() {
    
    Assert.assertEquals("This is a string.", stringUtil.encodeHTMLCharacters("This is a string."));
    Assert.assertEquals("&amp; &lt; &gt; &#x22; &#x29; &#x28;", stringUtil.encodeHTMLCharacters("& < > \" ) ("));
    Assert.assertEquals(null, stringUtil.encodeHTMLCharacters(null));
  }
}
