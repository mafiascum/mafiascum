package net.mafiascum.jdbc;

import java.sql.Statement;

public interface StatementExecutorNoResult {

  public void execute(Statement statement) throws Exception;
}
