package net.mafiascum.jdbc.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.jdbc.StoreSQLBuilder;
import net.mafiascum.web.misc.Dual;

import org.junit.Assert;
import org.junit.Test;

public class StoreSQLBuilderTest {

  @Test
  public void testPutEscapedString() {
    
    StoreSQLBuilder builder = new StoreSQLBuilder("tableName");
    
    List<Dual<String, String>> columns = new ArrayList<Dual<String, String>>();
    
    builder.setColumns(columns);
    
    builder.putEscapedString("columnName", "'escaped'");

    Assert.assertEquals(1, columns.size());
    Assert.assertEquals("columnName", columns.get(0).getObject1());
    Assert.assertEquals("'escaped'", columns.get(0).getObject2());
  }
  
  @Test
  public void testPutString() {
    StoreSQLBuilder builder = new StoreSQLBuilder("tableName");
    
    List<Dual<String, String>> columns = new ArrayList<Dual<String, String>>();
    
    builder.setColumns(columns);
    
    builder.put("columnName", "'notescaped'");
    builder.put("columnName2", "str\\ing");
    builder.put("columnName3", "regular");
    
    Assert.assertEquals(3, columns.size());
    Assert.assertEquals("columnName", columns.get(0).getObject1());
    Assert.assertEquals("'''notescaped'''", columns.get(0).getObject2());

    Assert.assertEquals("columnName2", columns.get(1).getObject1());
    Assert.assertEquals("'str\\\\ing'", columns.get(1).getObject2());

    Assert.assertEquals("columnName3", columns.get(2).getObject1());
    Assert.assertEquals("'regular'", columns.get(2).getObject2());
  }
  
  @Test
  public void testPutInteger() {
    StoreSQLBuilder builder = new StoreSQLBuilder("tableName");
    
    List<Dual<String, String>> columns = new ArrayList<Dual<String, String>>();
    
    builder.setColumns(columns);
    
    builder.put("columnName", 1);
    builder.put("columnName2", (Integer)null);
    builder.put("columnName3", -1);
    
    Assert.assertEquals(3, columns.size());
    Assert.assertEquals("columnName", columns.get(0).getObject1());
    Assert.assertEquals("1", columns.get(0).getObject2());

    Assert.assertEquals("columnName2", columns.get(1).getObject1());
    Assert.assertEquals("NULL", columns.get(1).getObject2());

    Assert.assertEquals("columnName3", columns.get(2).getObject1());
    Assert.assertEquals("-1", columns.get(2).getObject2());
  }
  
  @Test
  public void testPutMoney() {
    StoreSQLBuilder builder = new StoreSQLBuilder("tableName");
    
    List<Dual<String, String>> columns = new ArrayList<Dual<String, String>>();
    
    builder.setColumns(columns);
    
    builder.putMoney("columnName", new BigDecimal("12.95"));
    builder.putMoney("columnName2", (BigDecimal)null);
    builder.putMoney("columnName3", new BigDecimal("0.75"));
    builder.putMoney("columnName4", new BigDecimal("-0.75"));
    
    Assert.assertEquals(4, columns.size());
    Assert.assertEquals("columnName", columns.get(0).getObject1());
    Assert.assertEquals("1295", columns.get(0).getObject2());

    Assert.assertEquals("columnName2", columns.get(1).getObject1());
    Assert.assertEquals("NULL", columns.get(1).getObject2());

    Assert.assertEquals("columnName3", columns.get(2).getObject1());
    Assert.assertEquals("75", columns.get(2).getObject2());

    Assert.assertEquals("columnName4", columns.get(3).getObject1());
    Assert.assertEquals("-75", columns.get(3).getObject2());
  }
}
