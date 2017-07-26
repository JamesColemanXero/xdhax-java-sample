<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="com.xero.model.Organisation"%>

<%
Object connected = request.getAttribute("connected");
Object hasToken = request.getAttribute("hasToken");
Object orgName = request.getAttribute("orgName");
Object message = request.getAttribute("message");
%>

<html>
<head>
<title>Xero + AWS Debt Collector</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<link href="./css/dashboard.css" rel="stylesheet">
</head>
<body>


<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Show Me The Money</a>
        </div>
         <div id="navbar" class="navbar-collapse collapse">
          	<ul class="nav navbar-nav navbar-right">
            	<li><a href="./dashboard">Dashboard</a></li>
            	<li><a href="./settings">Settings</a></li>
            	<li><a href="./signout">Signout</a></li>
          	</ul>
		</div>
	</div>
</nav>
    
<div class="container-fluid">	
	<div class="row">
		<% if (connected.toString() == "true") { %>
		<div class="alert alert-success" role="alert">
		  <strong>Well done!</strong> <%=message %>
		</div>
		<% } %>
		
		<% if (connected.toString() == "false") { %>
		<div class="alert alert-warning" role="alert">
			<%=message %>
		</div>
		<% } %>
	
	
	
 		<main class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 pt-3">
        <h1>Settings</h1>
 
		<% if (hasToken.toString() == "false") { %>
			<a href="./RequestTokenServlet"><img src="<%= request.getContextPath()%>/images/connect_xero_button_blue1.png"></a>	
		<% } else { %>	
			You are connected to <strong><%= orgName %></strong> org
			<br>
      		<form class="form-invoices" method="post" action="./invoices">
              <button class="btn btn-md btn-primary" type="submit">Load Invoices</button>
      		</form>
      		
      		<form class="form-disconnect" method="post" action="./settings">
              <button class="btn btn-md btn-primary" type="submit">Disconnect</button>
      		</form>
      		
		<% } %>
	
        </main>
	</div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
</body>
</html>