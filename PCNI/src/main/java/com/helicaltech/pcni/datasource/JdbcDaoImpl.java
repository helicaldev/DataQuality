package com.helicaltech.pcni.datasource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.helicaltech.pcni.exceptions.JdbcConnectionException;

import org.apache.commons.dbutils.DbUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;

/**
 * Created by author on 28-Dec-14.
 *
 * @author Rajasekhar
 */
@Component
class JdbcDaoImpl implements IJdbcDao {

	private static final Logger logger = LoggerFactory.getLogger(JdbcDaoImpl.class);
	
	public String query(@Nullable Connection connection, String sql) {
		if (connection == null) 
		{
			throw new IllegalArgumentException("Nopes! The connection object is null");
		}
		Statement statement = null;
		JsonObject queryResult;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultSet = statement.executeQuery(sql);
			int rows = getRowCount(resultSet);
			ResultSetMetaData metaData = resultSet.getMetaData();

			queryResult = new JsonObject();
			queryResult.add("data", obtainData(resultSet, metaData));
			queryResult.add("metadata", obtainMetaData(metaData, rows));
		} catch (SQLException e) 
		{
			throw new JdbcConnectionException("Failed to query the database.", e);
		} 
		finally 
		{
			//DbUtils.closeQuietly(connection, statement, resultSet);
		}
		return queryResult.toString();
	}

	private int getRowCount(@Nullable ResultSet resultSet) {
		if (resultSet == null) {
			return 0;
		}
		try {
			resultSet.last();
			return resultSet.getRow();
		} catch (SQLException exp) {
			throw new JdbcConnectionException("Failed to query the database.", exp);
		} finally {
			try {
				resultSet.beforeFirst();
			} catch (SQLException ignore) {
			}
		}
	}

	@NotNull
	private JsonArray obtainData(@NotNull ResultSet resultSet, @NotNull ResultSetMetaData metaData) throws SQLException {
		JsonArray dataArray = new JsonArray();
		while (resultSet.next()) {
			JsonObject row = new JsonObject();
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				int columnType = metaData.getColumnType(i);
				Object object = resultSet.getObject(i);
				if ((columnType == Types.DATE) || (columnType == Types.TIMESTAMP) || (columnType == Types.TIME)) {
					row.addProperty(metaData.getColumnName(i), object.toString());
				} else {
					String columnLabel = metaData.getColumnLabel(i);
					if (object instanceof Number) {
						row.addProperty(columnLabel, (Number) (object));
					} else if (object instanceof Character) {
						row.addProperty(columnLabel, (Character) (object));
					} else if (object instanceof Boolean) {
						row.addProperty(columnLabel, (Boolean) (object));
					} else {
						row.addProperty(columnLabel, (object == null ? "Null" : (String) object));
					}
				}
			}
			dataArray.add(row);
		}
		return dataArray;
	}

	@NotNull
	private JsonArray obtainMetaData(@NotNull ResultSetMetaData metaData, int rows) throws SQLException {
		JsonObject columnNameAndType = new JsonObject();
		for (int index = 1; index <= metaData.getColumnCount(); index++) {
			JsonObject object = new JsonObject();
			object.addProperty("name", metaData.getColumnLabel(index));
			object.addProperty("type", metaData.getColumnTypeName(index));
			columnNameAndType.add(Integer.toString(index), object);
		}
		JsonObject rowsJson = new JsonObject();
		rowsJson.addProperty("rows", rows);
		JsonArray metaDataArray = new JsonArray();
		metaDataArray.add(columnNameAndType);
		metaDataArray.add(rowsJson);
		return metaDataArray;
	}
	
	@SuppressWarnings("null")
	public String createQuery(@Nullable Connection connection, String sql) {
		if (connection == null) 
		{
			throw new IllegalArgumentException("Nopes! The connection object is null");
		}
		JsonArray dataArray = new JsonArray();
		JsonObject row = new JsonObject();
		JsonArray dataArrays = new JsonArray();
		JsonObject rows = new JsonObject();
		Statement statement = null;
		JsonObject queryResult = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.executeUpdate(sql);
			row.addProperty("Create Query", "");
			dataArray.add(row);
			queryResult.add("data", dataArray);
			row.addProperty("", "0");
			dataArrays.add(rows);
			queryResult.add("status", dataArrays);
		} catch (SQLException e) 
		{
			throw new JdbcConnectionException("Failed to query the database.", e);
		} 
		finally 
		{
			DbUtils.closeQuietly(connection, statement, resultSet);
		}
		return queryResult.toString();
	}

	public String updateQuery(@Nullable Connection connection, String sql) {
		if (connection == null) 
		{
			throw new IllegalArgumentException("Nopes! The connection object is null");
		}
		String message="failure";
		Statement statement = null;
		
		try {
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.executeUpdate(sql);
			message= "success";
		} catch (SQLException e) 
		{
			message="Error occured";
			if(logger.isDebugEnabled())
			{
				logger.debug("Failed to insert query ",e);
			}
			throw new JdbcConnectionException("Failed to query the database.", e);
		} 
		finally
		{
			DbUtils.closeQuietly(statement);
			//DbUtils.closeQuietly();
		}
		return message;
	}
	
	@SuppressWarnings("null")
	@Override
	public List<String> searchUserById(@Nullable Connection connection,int id)
	{
		List<String> userList = null;
		//String searchUserQuery = "SELECT id,username from t_pcni_users";
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultSet = statement.executeQuery("SELECT username FROM t_pcni_users");
				while(resultSet.next())
				{
					userList.add(resultSet.getString("username"));
				}
		}
		catch(SQLException e)
		{
			throw new JdbcConnectionException("Failed to query the database.", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(resultSet);
		}
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public List<String> searchUserQuery(@Nullable Connection connection) {
		if (connection == null) 
		{
			throw new IllegalArgumentException("Nopes! The connection object is null");
		}
		List<String> searchedUserList = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			String searchUserQry = "SELECT username FROM t_pcni_users;";
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultSet = statement.executeQuery(searchUserQry);
			while (resultSet.next()) {
				searchedUserList.add(resultSet.getString(0));
			}
		} catch (SQLException e) 
		{
			throw new JdbcConnectionException("Failed to query the database.", e);
		} 
		finally 
		{
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(resultSet);
		}
		return searchedUserList;
	}
	
	
	@Override
	public String deleteQuery(@Nullable Connection connection,int id) {
		if (connection == null) 
		{
			throw new IllegalArgumentException("Nopes! The connection object is null");
		}

		String message="Error Occured!!";
		Statement statement = null;
		ResultSet resultSet = null;
		String deleteRoleMappingQuery = "DELETE FROM user_role WHERE user_id="+id+";";
		String deleteUserQuery = "DELETE FROM t_pcni_users WHERE id="+id+";";
		try {
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.executeUpdate(deleteRoleMappingQuery);
			statement.executeUpdate(deleteUserQuery);
			message= "success";
		} catch (SQLException e) 
		{
			message="Error occured";
			if(logger.isDebugEnabled())
			{
				logger.debug("Failed to delete user ",e);
			}
			throw new JdbcConnectionException("Failed to query the database.", e);
		} 
		finally
		{
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(resultSet);
		}
		return message;
	}
	
}
