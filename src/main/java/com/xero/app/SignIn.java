package com.xero.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.TooManyRequestsException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.api.client.util.Base64;

public class SignIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected AWSCognitoIdentityProviderClient cognitoClient = new AWSCognitoIdentityProviderClient();
	
    public SignIn() {
    	super();    
    	
    	// CREATE the TABLES we'll need in AWS DynamicDB
    	Storage storage = new Storage();
    	storage.createTable("users");
    	storage.createTable("invoices");
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("signin.jsp").forward(request, response);
	}
	
	private boolean isEmpty(String str) {
		boolean empty = false;
		if (str == null || str.isEmpty()) {
			empty = true;
		}
		return empty;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("inputEmail");
        String password = request.getParameter("inputPassword");
        
        // Check that values submitted are not blank
        if (isEmpty(username) || isEmpty(password))
        {
            String encodedMsg = URLEncoder.encode("Sign in confirmation failed - check the email and passwords", "UTF-8");
        	request.getRequestDispatcher("signin.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }

        try
        {
            Map<String,String> authParams = new HashMap<String,String>();
            authParams.put("USERNAME", username);
            authParams.put("PASSWORD", password);

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withAuthParameters(authParams)
                    .withClientId(System.getProperty("CognitoClientId"))
                    .withUserPoolId(System.getProperty("CognitoPoolId"));

            AdminInitiateAuthResult authResponse = cognitoClient.adminInitiateAuth(authRequest);
              
            String token = authResponse.getAuthenticationResult().getAccessToken();
            
            String[] jwtParts = token.split("\\.");
             
            byte[] jwtObject = Base64.decodeBase64(jwtParts[1]);
            String s = new String(jwtObject);
           
            JSONParser parser = new JSONParser();

    		Object obj = null;
            try {
				obj = parser.parse(s);
			} catch (ParseException e) {
				e.printStackTrace();
			}
            
            JSONObject jsonObject = (JSONObject) obj;
            String uId = (String) jsonObject.get("sub");
            
            Storage storage = new Storage();
        	storage.saveUserId(uId);
            
        	// Start NEW Session and add userId
        	HttpSession session = request.getSession(true);
            session.setAttribute("userId", uId); 
                		
    		List<Map<String, AttributeValue>> invoices = storage.getInvoices(uId, "invoices");
        	request.setAttribute("invoices", invoices); 
        	request.setAttribute("success", ""); 
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            
        }
        catch (UserNotFoundException ex)
        {
        	System.out.println("UserNotFound");
        	String encodedMsg = URLEncoder.encode("Sign in failed - user not found", "UTF-8");
        	request.getRequestDispatcher("signin.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
        catch (NotAuthorizedException ex)
        {
        	System.out.println("NotAuthorized");
        	String encodedMsg = URLEncoder.encode("Sign in failed - incorrect username or password", "UTF-8");
        	request.getRequestDispatcher("signin.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
        catch (TooManyRequestsException ex)
        {
        	System.out.println("TooMayRequests");
        	String encodedMsg = URLEncoder.encode("Sign in failed - too many requests", "UTF-8");
        	request.getRequestDispatcher("signin.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
	}
}
