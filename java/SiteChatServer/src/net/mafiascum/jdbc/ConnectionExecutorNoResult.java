package net.mafiascum.jdbc;

import java.sql.Connection;

public interface ConnectionExecutorNoResult {

  public void execute(Connection connection) throws Exception;
}
