package com.xero.app;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xero.api.Config;
import com.xero.api.XeroApiException;
import com.xero.api.XeroClient;
import com.xero.model.Contact;
import com.xero.model.Invoice;

public class Invoices extends HttpServlet {
	private static final long serialVersionUID = 1L;  
	private Storage storage = new Storage();
	private Config config = JsonConfig.getInstance();

    public Invoices() {
        super();
    }
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String uId = session.getAttribute("userId").toString();
		String token = storage.get(uId,"users","token");
		String tokenSecret = storage.get(uId,"users","tokenSecret");
		
		Client c = new Client();
		
		if (c.publicTokenExpired(uId)) {
			request.setAttribute("connected", false); 
			request.setAttribute("hasToken", false); 
			request.setAttribute("message", "Your token has expired"); 
			request.setAttribute("orgName", ""); 
	        request.getRequestDispatcher("settings.jsp").forward(request, response);	
		}

		// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
		XeroClient client = new XeroClient(config);
		client.setOAuthToken(token, tokenSecret);
		
		try {
			List<Contact> contacts = client.getContacts(null,"ContactStatus==\"ACTIVE\"",null);
			
			// Filter by Date with Where Clause
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    cal.add(Calendar.MONTH, -1);
		    SimpleDateFormat format = new SimpleDateFormat("y,M,d");
			String monthAgoStr = format.format(cal.getTime());
		    
			List<Invoice> invoices = client.getInvoices(null,"Type==\"ACCREC\" && Status==\"AUTHORISED\" &&  DueDate < DateTime(" + monthAgoStr + ") ","DueDate");
			
			// Loop over our invoices
	        for (Iterator<Invoice> iter = invoices.iterator(); iter.hasNext(); ) {
				Invoice inv = iter.next();
			
				SimpleDateFormat format1 = new SimpleDateFormat();
				String DateToStr = format1.format(inv.getDueDate().getTime());
				String email = containsContact(contacts, inv.getContact().getContactID());
				if(email.length() > 0) {
			        
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("uId", uId);
					map.put("id", inv.getInvoiceID());
					map.put("inv_num", inv.getInvoiceNumber());
			        map.put("amount", inv.getAmountDue().toString());
			        map.put("duedate", DateToStr);
			        map.put("email", email);
			        map.put("cname", inv.getContact().getName());
			        map.put("cId", inv.getContact().getContactID());
			        
					storage.save(uId,"invoices", map);
				}
			}
			
			request.setAttribute("connected", true); 
			request.setAttribute("hasToken", true); 
			request.setAttribute("message", "You've successfully retrieved outstanding invoices."); 
			request.setAttribute("orgName", storage.get(uId,"users", "org_name")); 
	        request.getRequestDispatcher("settings.jsp").forward(request, response);	
			
		} catch (XeroApiException e) {
			request.setAttribute("connected", false); 
			request.setAttribute("hasToken", false); 
			request.setAttribute("message", "Your token has expired"); 
			request.setAttribute("orgName", ""); 
	        request.getRequestDispatcher("settings.jsp").forward(request, response);	
		}
	}
	
	public static String containsContact(List<Contact> c, String id) {
		String email = "";
	    for(Contact o : c) {
	        if(o != null && o.getContactID().equals(id)) {
	        	if(o.getEmailAddress() != null) {
	        		email =  o.getEmailAddress();
	        	}
	        }
	    }
	    return email;
	}
}
