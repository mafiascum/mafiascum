package net.mafiascum.jdbc;

import java.sql.SQLException;

public interface BatchInsertable {

  public void setBatchInsertStatementColumns(BatchInsertStatement batchInsertStatement) throws SQLException;
  public void addToBatchInsertStatement(BatchInsertStatement batchInsertStatement) throws SQLException;
}
