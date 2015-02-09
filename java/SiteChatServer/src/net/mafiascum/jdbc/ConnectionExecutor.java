package net.mafiascum.jdbc;

import java.sql.Connection;

public interface ConnectionExecutor<T> {

  public T execute(Connection connection) throws Exception;
}
