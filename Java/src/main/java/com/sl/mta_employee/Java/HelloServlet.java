package com.sl.mta_employee.Java;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		Connection conn = null;
		try {
			conn = getConnection();
		} catch (SQLException e) {
			throw new ServletException(e.getMessage(), e);
		}
		try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8")) {
			writer.write("First MTA App!!");
			writer.write("<br>JDBC connection available: ");
			if (conn != null) {
				writer.write("yes");
				writer.write("<br>Current Hana DB user:");
				String userName = getCurrentUser(conn);
				writer.write(userName);
				writer.write("<br>Current Hana schema:");
				writer.write(getCurrentSchema(conn));
				writer.write("<br>Total Number of records in the table by using SQL query : ");
				writer.write(getTotalNumberOfRecords(conn));
				writer.write("<br>Employees in the Table by using calculation view : ");
				writer.write(getEmployees(conn));
			} else {
				writer.write("no");
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private String getCurrentUser(Connection conn) throws SQLException {
		String currentUser = "";
		PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_USER \"current_user\" FROM DUMMY;");
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("current_user");
		while (resultSet.next()) {
			currentUser += resultSet.getString(column);
		}
		return currentUser;
	}

	private String getCurrentSchema(Connection conn) throws SQLException {
		String currentSchema = "";
		PreparedStatement prepareStatement = conn
				.prepareStatement("SELECT CURRENT_SCHEMA \"current_schema\" FROM DUMMY;");
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("current_schema");
		while (resultSet.next()) {
			currentSchema += resultSet.getString(column);
		}
		return currentSchema;
	}

	private Connection getConnection() throws SQLException {
		try {
			Context ctx = new InitialContext();
			Context xmlContext = (Context) ctx.lookup("java:comp/env");
			DataSource ds = (DataSource) xmlContext.lookup("jdbc/DefaultDB");
			Connection conn = ds.getConnection();
			System.out.println("Connected to database");
			return conn;
		} catch (NamingException ignorred) {
			// could happen if HDB support is not enabled
			return null;
		}
	}

	private String getTotalNumberOfRecords(Connection conn) throws SQLException {
		String count = "";
		String sql = "SELECT count(*) \"count\" FROM " + getCurrentSchema(conn)
				+ ".\"MTA_Employee.DB::Organization.employee\";";
		PreparedStatement prepareStatement = conn.prepareStatement(sql);
		ResultSet resultSet = prepareStatement.executeQuery();
		int column = resultSet.findColumn("count");
		while (resultSet.next()) {
			count = resultSet.getString(column);
		}
		return count;
	}

	private String getEmployees(Connection conn) throws SQLException {
		String result = "<table border=\"1\">";
		String sql = "SELECT * FROM " + getCurrentSchema(conn) + ".\"MTA_Employee.DB::GetEmployees\";";
		PreparedStatement prepareStatement = conn.prepareStatement(sql);
		ResultSet resultSet = prepareStatement.executeQuery();
		int column1 = resultSet.findColumn("empId");
		int column2 = resultSet.findColumn("name");
		while (resultSet.next()) {
			result = result + "<tr><td>";
			result = result + resultSet.getString(column1) + "</td><td>";
			result = result + resultSet.getString(column2) + "</td></tr>";
		}
		result = result + "</table>";
		return result;
	}
}
