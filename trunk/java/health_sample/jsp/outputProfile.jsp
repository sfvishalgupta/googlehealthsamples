<?xml version="1.0" ?> 
<%@ page language="java" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Health Profile</title>
  <link rel="stylesheet" type="text/css" href="css/app.css" />
</head>
<body>
  <h1>Health Sample</h1>
  <h2>Menu</h2>
  <ul>
    <li><a href="?action=logout">Logout</a></li>
  </ul>
  <h2>CCRg Document:</h2>
  <pre>
  <c:out value="${profile}"></c:out>
  </pre>
</body>
</html>
