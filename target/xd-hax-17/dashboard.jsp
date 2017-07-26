<%@page import="java.util.*"%>
<%@page import="com.xero.model.Invoice"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.amazonaws.services.dynamodbv2.model.AttributeValue"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
Object success = request.getAttribute("success");
String message = (String) request.getAttribute("message");
List<Map<String, AttributeValue>> invoices = (List<Map<String, AttributeValue>>) request.getAttribute("invoices"); 
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Xero + AWS Debt Collector - Dashboard</title>
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
	<% if (success.toString() == "true") { %>
	<div class="alert alert-success" role="alert">
	  <%= message %>
	</div>
	<% } %>
	
	<% if (success.toString() == "false") { %>
	<div class="alert alert-warning" role="alert">
	  <%= message %>
	</div>
	<% } %>
	
	
        <main class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 pt-3">
		<h1>Dashboard</h1>
			<h2 class="sub-header">Customers</h2>
          	<div class="table-responsive">
            	<table class="table table-striped">
	              	<thead>
	                	<tr>
	                  		<th>&nbsp;</th>
	                  		<th>Invoice Number</th>
	                  		<th>Due Date</th>
	                  		<th>Days Overdue</th>
	                  		<th>Amount</th>
	                  		<th>Name</th>
	                	</tr>
					</thead>
					<tbody>
	              	<form name="EmailCustomers" method="POST" action="./email" >
	              	<input type="submit" value="Email Customer"  class="btn btn-default">
						<% 	String dateInString = "";
						
						for (Map<String, AttributeValue> item : invoices) {
						   
		    				SimpleDateFormat format = new SimpleDateFormat();
		    				dateInString = item.get("duedate").getS().toString();
		    				
		    				System.out.println(dateInString);
		    				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
		    				Date parsedDate = formatter.parse(dateInString);
		    				
		    				
		    				SimpleDateFormat formatter2 = new SimpleDateFormat("MM/dd/yyyy");
		    				System.out.println(formatter2.format(parsedDate));
		    				System.out.println(parsedDate.getTime());
		    				
		    				Date today = new Date();
		    				
		    				System.out.println(today.getTime());
		    				
		    				
		    				long diff = today.getTime() - parsedDate.getTime();
		    				long daysOverDue = diff / (24 * 60 * 60 * 1000);
							String styleOverDue = "";
		    				if(daysOverDue > 30) {
		    					styleOverDue = "style='color: red;'";
		    				}
		    				
		    				System.out.println(daysOverDue);
		    				
		    			%>
						<tr>
		                  <td><input type="checkbox" name="InvoiceId" value="<%=item.get("id").getS().toString()%>"></td>
		                  <td><%=item.get("inv_num").getS().toString()%></td>
		                  <td><%=formatter2.format(parsedDate) %></td>
		                  <td <%=styleOverDue%> ><%=daysOverDue %></td>
		                  <td><%=item.get("amount").getS().toString() %></td>
		                  <td><%=item.get("cname").getS().toString()  %></td>
		                </tr>
						<% } %>
					</form>
	            	</tbody>
            	</table>
        	</div>

        </main>
	</div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
</body>
</html>