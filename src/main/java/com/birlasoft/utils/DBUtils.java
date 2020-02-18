package com.birlasoft.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class DBUtils {
	static Logger LOGGER = Logger.getLogger(DBUtils.class);
	
	private static DBUtils dbUtils = new DBUtils();

	private DBUtils() {
	}

	public static DBUtils getInstance() {
		return dbUtils;
	}

	private Connection establishConnection(String driver, String connString)
	        throws SQLException, ClassNotFoundException {
		Class.forName(driver);
		return DriverManager.getConnection(connString);
	}

	// Execute Query - if varargs not present then Normal statement otherwise
	// Prepared Statement
	public HashMap<Integer, String> executeQuery(String driver, String connString, String query,
	        boolean includeColumnName, String... arguments) throws ClassNotFoundException, SQLException {
		Connection dbConn = establishConnection(driver, connString);
		HashMap<Integer, String> queryResult = new HashMap<Integer, String>();

		Statement statement = null;
		ResultSet result = null;
		int rowCounter = 1;
		String data = "";
		String columnName = "";

		try {
			if (arguments == null) {
				statement = dbConn.createStatement();
				result = statement.executeQuery(query);
			} else {
				statement = dbConn.prepareStatement(query);

				for (int ctr = 0; ctr < arguments.length; ctr++) {
					((PreparedStatement) statement).setString(ctr + 1, arguments[ctr]);
				}

				result = statement.executeQuery(query);
			}

			while (result.next()) {
				int columnsCount = result.getMetaData().getColumnCount();

				for (int i = 0; i <= columnsCount; i++) {
					if (includeColumnName) {
						columnName = result.getMetaData().getColumnName(i) + Constants.COLUMN_SEPARATOR;
					}

					if (result.getObject(i) == null) {
						data += columnName + " " + Constants.COLUMNVALUE_SEPARATOR;
					} else {
						data += columnName + result.getObject(i).toString() + Constants.COLUMNVALUE_SEPARATOR;
					}
				}

				data = data.substring(0, data.length() - Constants.COLUMNVALUE_SEPARATOR.length());
				queryResult.put(rowCounter, data);
				rowCounter++;
			}
		} catch (SQLException e) {
			LOGGER.error("Error in querying '" + query + "' from database", e);
			result.close();
			statement.close();
			dbConn.close();			
		} finally {
			result.close();
			statement.close();
			dbConn.close();
		}

		return queryResult;
	}

	// Update Query - if varargs not present then Normal statement otherwise
	// Prepared Statement
	public boolean updateQuery(String driver, String connString, String query, String... arguments)
	        throws ClassNotFoundException, SQLException {
		Connection dbConn = establishConnection(driver, connString);

		Statement statement = null;
		boolean blnResult = false;

		try {
			if (arguments == null) {
				statement = dbConn.createStatement();
				statement.executeUpdate(query);
			} else {
				statement = dbConn.prepareStatement(query);

				for (int ctr = 0; ctr < arguments.length; ctr++) {
					((PreparedStatement) statement).setString(ctr + 1, arguments[ctr]);
				}
				((PreparedStatement) statement).executeUpdate();
			}

			blnResult = true;
		} catch (SQLException e) {
			LOGGER.error("Error in posting '" + query + "' from database", e);
			statement.close();
			dbConn.close();
		} finally {
			statement.close();
			dbConn.close();
		}

		return blnResult;
	}

	// Execute Proc - Callable Statement
	public HashMap<Integer, String> executeProc(String driver, String connString, String query, String... arguments)
	        throws ClassNotFoundException, SQLException {
		Connection dbConn = establishConnection(driver, connString);
		HashMap<Integer, String> queryResult = new HashMap<Integer, String>();

		CallableStatement statement = null;
		ResultSet result = null;
		boolean blnResult = false;
		int rowCounter = 1;
		String data = "";

		try {
			statement = dbConn.prepareCall(query);

			if (arguments != null) {
				for (int ctr = 0; ctr < arguments.length; ctr++) {
					statement.setString(ctr + 1, arguments[ctr]);
				}
			}

			blnResult = statement.execute();

			if (blnResult) {
				result = statement.getResultSet();

				while (result.next()) {
					int columnsCount = result.getMetaData().getColumnCount();

					for (int i = 0; i <= columnsCount; i++) {
						if (result.getObject(i) == null) {
							data += " " + Constants.COLUMNVALUE_SEPARATOR;
						} else {
							data += result.getObject(i).toString() + Constants.COLUMNVALUE_SEPARATOR;
						}
					}

					data = data.substring(0, data.length() - Constants.COLUMNVALUE_SEPARATOR.length());
					queryResult.put(rowCounter, data);
					rowCounter++;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Error in querying '" + query + "' from database", e);
			result.close();
			statement.close();
			dbConn.close();
		} finally {
			result.close();
			statement.close();
			dbConn.close();
		}
		return queryResult;
	}
}