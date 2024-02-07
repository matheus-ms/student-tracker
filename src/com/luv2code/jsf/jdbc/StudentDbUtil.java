package com.luv2code.jsf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class StudentDbUtil {

	private static StudentDbUtil instance;
	private DataSource dataSource;
	private String jndiName = "java:comp/env/jdbc/web_student_tracker";
	

	public static StudentDbUtil getInstance() throws Exception {
		if (instance == null) {
			instance = new StudentDbUtil();
		}

		return instance;
	}

	private StudentDbUtil() throws Exception {
		dataSource = getDataSource();
	}

	private DataSource getDataSource() throws Exception {
		Context context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup(jndiName);

		return dataSource;
	}

	public List<Student> getStudents() throws Exception {

		List<Student> students = new ArrayList<>();

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {

			connection = dataSource.getConnection();

			String sql = "select * from student order by last_name";

			statement = connection.createStatement();

			resultSet = statement.executeQuery(sql);

			// process result set
			while (resultSet.next()) {

				// retrieve data from result set row
				int id = resultSet.getInt("id");
				String firstName = resultSet.getString("first_name");
				String lastName = resultSet.getString("last_name");
				String email = resultSet.getString("email");

				Student student = new Student(id, firstName, lastName, email);
				students.add(student);
			}

			return students;

		} finally {
			// close JDBC objects
			close(connection, statement, resultSet);
		}

	}

	public void addStudent(Student student) throws Exception {

		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = dataSource.getConnection();
			
			String sql = "insert into student (first_name, last_name, email) values (?, ?, ?)";
			
			statement = connection.prepareStatement(sql);
			
			statement.setString(1, student.getFirstName());
			statement.setString(2, student.getLastName());
			statement.setString(3, student.getEmail());
			
			statement.execute();
			
		} finally {
			close(connection, statement);
		}
		
	}

	public Student getStudent(int studentId) throws Exception {
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = dataSource.getConnection();
			
			String sql = "select * from student where id=?";
			
			statement = connection.prepareStatement(sql);
			
			statement.setInt(1, studentId);
			
			resultSet = statement.executeQuery();
			
			Student studentDb = null;
			
			// retrieve data from result set row
			if(resultSet.next()) {
				int id = resultSet.getInt("id");
				String firstName = resultSet.getString("first_name");
				String lastName = resultSet.getString("last_name");
				String email = resultSet.getString("email");
				
				studentDb = new Student(id, firstName, lastName, email);
				
			} else {
				throw new Exception("Could not find student id: " +studentId);
			}
			
			return studentDb;
			
		} finally {
			close(connection, statement, resultSet);
		}		
	}

	public void updateStudent(Student theStudent) throws Exception {

		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = dataSource.getConnection();
			
			String sql = "update student "
						+ "set first_name=?, last_name=?, email=? "
						+ "where id=?";
			
			statement = connection.prepareStatement(sql);
			
			statement.setString(1, theStudent.getFirstName());
			statement.setString(2, theStudent.getLastName());
			statement.setString(3, theStudent.getEmail());
			statement.setInt(4, theStudent.getId());
			
			statement.execute();
			
		} finally {
			close(connection, statement);
		}
		
	}
	
	public void deleteStudent(int studentId) throws Exception {
		
		Connection connection = null;
		PreparedStatement myStmt = null;
		
		try {
			connection = dataSource.getConnection();
			
			String sql = "delete from student where id=?";
			
			myStmt = connection.prepareStatement(sql);
			
			myStmt.setInt(1, studentId);
			
			myStmt.execute();
			
		} finally {
			close(connection, myStmt);
		}
		
	}
	
	public List<Student> searchStudents(String theSearchName)  throws Exception {

		List<Student> students = new ArrayList<>();
		
		Connection myConn = null;
		PreparedStatement myStmt = null;
		ResultSet myRs = null;
		
		try {
			
			// get connection to database
			myConn = dataSource.getConnection();
			
	        //
	        // only search by name if theSearchName is not empty
	        //
			if (theSearchName != null && theSearchName.trim().length() > 0) {

				// create sql to search for students by name
				String sql = "select * from student where lower(first_name) like ? or lower(last_name) like ?";

				// create prepared statement
				myStmt = myConn.prepareStatement(sql);

				// set params
				String theSearchNameLike = "%" + theSearchName.toLowerCase() + "%";
				myStmt.setString(1, theSearchNameLike);
				myStmt.setString(2, theSearchNameLike);
				
			} else {
				// create sql to get all students
				String sql = "select * from student order by last_name";

				// create prepared statement
				myStmt = myConn.prepareStatement(sql);
			}
	        
			// execute statement
			myRs = myStmt.executeQuery();
			
			// retrieve data from result set row
			while (myRs.next()) {
				
				// retrieve data from result set row
				int id = myRs.getInt("id");
				String firstName = myRs.getString("first_name");
				String lastName = myRs.getString("last_name");
				String email = myRs.getString("email");
				
				// create new student object
				Student tempStudent = new Student(id, firstName, lastName, email);
				
				// add it to the list of students
				students.add(tempStudent);			
			}
			
			return students;
		}
		finally {
			// clean up JDBC objects
			close(myConn, myStmt, myRs);
		}
	}
	
	
	private void close(Connection theConn, Statement theStmt) {
		close(theConn, theStmt, null);
	}
	
	private void close(Connection connection, Statement statement, ResultSet resultSet) {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
			
			if(statement != null) {
				statement.close();
			}
			
			if(connection != null) {
				connection.close(); // doesn't really close it ... just puts back in connection pool.
			}			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
