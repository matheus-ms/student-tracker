package com.luv2code.jsf.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class StudentController {
	
	
	private List<Student> students;
	private StudentDbUtil studentDbUtil;
	private Logger logger = Logger.getLogger(getClass().getName());	
	private String theSearchName;
	
	
	public StudentController() throws Exception {
		students = new ArrayList<>();		
		studentDbUtil = StudentDbUtil.getInstance();
	}
	

	public String getTheSearchName() {
		return theSearchName;
	}

	public void setTheSearchName(String theSearchName) {
		this.theSearchName = theSearchName;
	}

	public List<Student> getStudents() {
		return students;
	}
	

	public void loadStudents() {

		logger.info("Loading students");
		
		logger.info("theSearchName = " + theSearchName);
		
		try {
			
			if (theSearchName != null && theSearchName.trim().length() > 0) {
				// search for students by name
				students = studentDbUtil.searchStudents(theSearchName);				
			}
			else {
				// get all students from database
				students = studentDbUtil.getStudents();
			}
			
		} catch (Exception exc) {
			// send this to server logs
			logger.log(Level.SEVERE, "Error loading students", exc);
			
			// add error message for JSF page
			addErrorMessage(exc);
		}
		finally {
			// reset the search info
			theSearchName = null;
		}
	}
		
	public String addStudent(Student student) {

		logger.info("Adding student: " + student);

		try {
			
			// add student to the database
			studentDbUtil.addStudent(student);
			
		} catch (Exception exc) {
			// send this to server logs
			logger.log(Level.SEVERE, "Error adding students", exc);
			
			// add error message for JSF page
			addErrorMessage(exc);

			return null;
		}
		
		return "list-students?faces-redirect=true";
	}

	public String loadStudent(int studentId) {
		
		logger.info("loading student: " + studentId);
		
		try {
			// get student from database
			Student theStudent = studentDbUtil.getStudent(studentId);
			
			// put in the request attribute ... so we can use it on the form page
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();		

			Map<String, Object> requestMap = externalContext.getRequestMap();
			requestMap.put("student", theStudent);	
			
		} catch (Exception exc) {
			// send this to server logs
			logger.log(Level.SEVERE, "Error loading student id:" + studentId, exc);
			
			// add error message for JSF page
			addErrorMessage(exc);
			
			return null;
		}
				
		return "update-student-form.xhtml";
	}	
	
	public String updateStudent(Student student) {

		logger.info("updating student: " + student);
		
		try {
			
			// update student in the database
			studentDbUtil.updateStudent(student);
			
		} catch (Exception exc) {
			// send this to server logs
			logger.log(Level.SEVERE, "Error updating student: " + student, exc);
			
			// add error message for JSF page
			addErrorMessage(exc);
			
			return null;
		}
		
		return "list-students?faces-redirect=true";		
	}
	
	public String deleteStudent(int studentId) {

		logger.info("Deleting student id: " + studentId);
		
		try {

			// delete the student from the database
			studentDbUtil.deleteStudent(studentId);
			
		} catch (Exception exc) {
			// send this to server logs
			logger.log(Level.SEVERE, "Error deleting student id: " + studentId, exc);
			
			// add error message for JSF page
			addErrorMessage(exc);
			
			return null;
		}
		
		return "list-students";	
	}	
	
	
	private void addErrorMessage(Exception e) {
		FacesMessage message = new FacesMessage("Error: " + e.getMessage());
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
}
