<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Google Health App Engine Example</title>
  </head>
  
  <body>
    <h3>Google Health App Engine Example</h3>
    <p>Do you want to allow this application to access to your profile?</p>
    <a href="<%= request.getAttribute("link") + "&permission=1" %>">Link to Google Health</a>
  </body>
</html>
