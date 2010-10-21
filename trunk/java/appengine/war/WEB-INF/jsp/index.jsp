<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ page import="java.util.Set" %>
<%@ page import="com.google.health.examples.appengine.gdata.TestResult" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Google Health App Engine Example</title>
  </head>
  
  <body>
    <h3>Google Health App Engine Example</h3>
    
    <form method="post">
      <table>
<%
  for (TestResult test : (Set<TestResult>) request.getAttribute("tests")) {
%>
        <tr>
          <td><%= test.getName() %></td>
          <td><%= test.getValue() %></td>
          <td><%= test.getUnits() %></td>
          <td><%= test.getDate() %></td>
        </tr>
<% } %>
        <tr>
          <td><input name="name"/></td>
          <td><input name="value"/></td>
          <td><input name="units"/></td>
          <td><input name="date"/></td>
          <td><input type="submit"/></td>
        </tr>
      </table>
    </form>
    
    <a href="<%= request.getAttribute("logoutLink") %>">Logout</a>
    <a href="?unlink">Unlink</a>
  </body>
</html>
