<%-- 
    Document   : Print
    Created on : 21.10.2008, 19:19:36
    Author     : monty
--%>
<%@ include file="KiezAtlas.jsp" %>

<%@page contentType="text/html" pageEncoding="iso-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%  String text = (String) session.getAttribute("formLetter");
    out.println(text);
%>
