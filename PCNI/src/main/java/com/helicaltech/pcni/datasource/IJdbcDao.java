package com.helicaltech.pcni.datasource;

import java.sql.Connection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Created by author on 28-Dec-14.
 *
 * @author Rajasekhar
 */
public interface IJdbcDao {

    public String query(Connection connection, String sql);
    public String createQuery(Connection connection, String sql);
    public String updateQuery(Connection connection, String sql);
    public List<String> searchUserById(@Nullable Connection connection,int id);
    public List<String> searchUserQuery(@Nullable Connection connection);
	String deleteQuery(Connection connection, int id);
}
