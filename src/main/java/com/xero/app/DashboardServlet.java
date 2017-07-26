package com.xero.app;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;;

public class DashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;	
	
	private Storage storage = new Storage();
	   	
   	public DashboardServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	String uId = session.getAttribute("userId").toString();
		
    	List<Map<String, AttributeValue>> invoices = storage.getInvoices(uId, "invoices");
    	request.setAttribute("invoices", invoices); 
    	request.setAttribute("success", "");
    	request.getRequestDispatcher("dashboard.jsp").forward(request, response);
	}
}
