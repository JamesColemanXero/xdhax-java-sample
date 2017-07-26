<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>    
<%
String isSuccess = request.getParameter("success");
String message = request.getParameter("message");
String email = request.getParameter("email");
if (email == null || email.isEmpty()) {
	email = "";
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Xero + AWS Debt Collector - SignIn</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css" integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r" crossorigin="anonymous">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
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
          <a class="navbar-brand" href="./index.jsp">Show Me The Money</a>
        </div>
         <div id="navbar" class="navbar-collapse collapse">
          	<ul class="nav navbar-nav navbar-right">
            	<li><a href="./signup">Sign-up</a></li>
          	</ul>
		</div>
	</div>
</nav>
    
<div class="container-fluid">
	<p>&nbsp;</p>
   	
	<div class="row">
 		<main class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 pt-3">
		<% if (isSuccess != null && isSuccess.equals("true")) { %>
		<div class="alert alert-success" role="alert">
		  <strong>Well done!</strong> You successfully set your password - Login to get started.
		</div>
		<% } %>
		<% if (isSuccess != null && isSuccess.equals("false")) { %>
		<div class="alert alert-success" role="alert">
		  <strong>Error!</strong> <%= message %>
		</div>
		<% } %>
		<div class="container">
		      <form class="form-signin" method="post" action="./signin">
		        <h2 class="form-signin-heading">Please Sign In</h2>
		        <label for="inputEmail" class="sr-only">Email address</label>
		        <input type="input" name="inputEmail" class="form-control" placeholder="Email Address" required="yes" value="<%= email %>">
		        <label for="inputEmail" class="sr-only">Password</label>
		        <input type="password" name="inputPassword" class="form-control" placeholder="Password" required="yes">
		        <div class="checkbox"></div>
		        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign In</button>
		      </form>
		</div>
        </main>
	</div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>

</body>
</html>