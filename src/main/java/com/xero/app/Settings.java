package com.xero.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Settings extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Storage storage = new Storage();
	
    public Settings() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		HttpSession session = request.getSession();
		String uId = session.getAttribute("userId").toString();
		
		String token = storage.get(uId, "users", "token");
		boolean hasToken = !storage.tokenIsNull(token);
		
		String orgName = storage.get(uId,"users","org_name");
		request.setAttribute("connected", ""); 
		request.setAttribute("hasToken", hasToken); 
		request.setAttribute("orgName", orgName); 
		request.setAttribute("message", ""); 
	    request.getRequestDispatcher("settings.jsp").forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		HttpSession session = request.getSession();
		String uId = session.getAttribute("userId").toString();
				
		storage.clear(uId);	
		storage.delete(uId,"invoices");
		request.setAttribute("connected", false); 
		request.setAttribute("hasToken", false); 
		request.setAttribute("message", "You are now disconnected from Xero."); 
		request.getRequestDispatcher("settings.jsp").forward(request, response);
	}
}
