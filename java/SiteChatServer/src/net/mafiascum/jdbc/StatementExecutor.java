package net.mafiascum.jdbc;

import java.sql.Statement;

public interface StatementExecutor<T> {

  public T execute(Statement statement) throws Exception;
}
