<?xml version="1.0" ?>
<%@ page language="java" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Google Health</title>
  <link rel="stylesheet" type="text/css" href="css/app.css" />
</head>
<body>
  <h1>Google Health</h1>
  <p>
     This page demonstrates basic functionality of Google Health.
  </p>
  <p><a href="<c:out value="${authUrl}"/>">Sign in to your Google Account</a></p>
</body>
</html>
