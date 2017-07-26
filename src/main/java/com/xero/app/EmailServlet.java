package com.xero.app;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.xero.api.Config;
import com.xero.api.XeroApiException;
import com.xero.api.XeroClient;
import com.xero.model.OnlineInvoice;

public class EmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Config config = JsonConfig.getInstance();
	
	private String FROM = "integrations@xero.com";  // Replace with your "From" address. This address must be verified.
    private String TO = "integrations@xero.com"; // Replace with a "To" address. If you have not yet requested
	private String BODY = "You owe me some cheddar ...";
    private String SUBJECT = "Show me the Money!";
    private Storage storage = new Storage();
    
    public EmailServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String onlineInvoiceUrl = ""; 
		String[] idArray = null;
		HttpSession session = request.getSession();
		String uId = session.getAttribute("userId").toString();
		String token = storage.get(uId,"users","token");
		String tokenSecret = storage.get(uId,"users","tokenSecret");
		
		if (null != System.getProperty("FromEmail")) {
			FROM = System.getProperty("FromEmail");
		}
			
		if (request.getParameterValues("InvoiceId") == null) {
			
			try {
				List<Map<String, AttributeValue>> invoices = storage.getInvoices(uId, "invoices");
		    	request.setAttribute("invoices", invoices); 
				request.setAttribute("success", "false");
				request.setAttribute("message", "Please select at least one invoice to send");
		        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
			} catch (XeroApiException e) {
				storage.clear(uId);	
				storage.delete(uId,"invoices");
				request.setAttribute("connected", false); 
				request.setAttribute("hasToken", false); 
				request.setAttribute("message", "Your token has expired"); 
				request.setAttribute("orgName", ""); 
		        request.getRequestDispatcher("settings.jsp").forward(request, response);
			}
		} else {
			idArray=request.getParameterValues("InvoiceId");			
		}
		

		// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
		XeroClient client = new XeroClient(config);
		client.setOAuthToken(token, tokenSecret);
		
		for (String id : idArray) {
			
			try {
				OnlineInvoice onlineInvoice = client.getOnlineInvoice(id);
				onlineInvoiceUrl = onlineInvoice.getOnlineInvoiceUrl();
			} catch (XeroApiException e) {	
				storage.clear(uId);	
				storage.delete(uId,"invoices");
				request.setAttribute("connected", false); 
				request.setAttribute("hasToken", false); 
				request.setAttribute("message", "Your token has expired"); 
				request.setAttribute("orgName", ""); 
		        request.getRequestDispatcher("settings.jsp").forward(request, response);
			}	
			
			String cname = storage.get(id,"invoices","cname");
			String email = storage.get(id,"invoices","email");
			String invNum = storage.get(id,"invoices","inv_num");
			String amount = storage.get(id,"invoices","amount");
				
        	TO= email;
        	BODY= "Dear " + cname + "<br><br>We are contacting you, regarding invoice number " + invNum + " for the amount of " + amount 
        			+ " <br><br>This invoice is now past due.  You can easily <a href='" + onlineInvoiceUrl  + "'>pay the invoice online</a>"
        			+ "<br><br>Team ABC Corp";
		
			// Construct an object to contain the recipient address.
	        Destination destination = new Destination().withToAddresses(new String[]{TO});
	
	        // Create the subject and body of the message.
	        Content subject = new Content().withData(SUBJECT);
	        Content textBody = new Content().withData(BODY);
	        Body body = new Body().withHtml(textBody);
	        			        
	        // Create a message with the specified subject and body.
	        Message message = new Message().withSubject(subject).withBody(body);
	        
	        // Assemble the email.
	        SendEmailRequest emailRequest = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);
	
	        try {
	            System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
	            AmazonSimpleEmailServiceClient awsClient = new AmazonSimpleEmailServiceClient();
	               
	            Region REGION = Region.getRegion(Regions.US_EAST_1);
	            awsClient.setRegion(REGION);
	
	            // Send the email.
	            awsClient.sendEmail(emailRequest);
	            System.out.println("Email sent!");
	
	        } catch (Exception ex) {
	            System.out.println("The email was not sent.");
	            System.out.println("Error message: " + ex.getMessage());
	        }
		}
		
		try {
			List<Map<String, AttributeValue>> invoices = storage.getInvoices(uId, "invoices");
	    	request.setAttribute("invoices", invoices); 
			request.setAttribute("success", "true");
			request.setAttribute("message", "Emails successfully sent!");
	        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
		} catch (XeroApiException e) {	
			storage.clear(uId);	
			storage.delete(uId,"invoices");
			request.setAttribute("connected", false); 
			request.setAttribute("hasToken", false); 
			request.setAttribute("message", "Your token has expired"); 
			request.setAttribute("orgName", ""); 
	        request.getRequestDispatcher("settings.jsp").forward(request, response);
		}	
	}
}
