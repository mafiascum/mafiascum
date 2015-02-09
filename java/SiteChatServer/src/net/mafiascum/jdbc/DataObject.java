package net.mafiascum.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataObject {

  public void loadFromResultSet(ResultSet resultSet) throws SQLException;
  public void store(Connection connection) throws SQLException;
}
