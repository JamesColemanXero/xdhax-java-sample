package com.xero.app;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.TooManyRequestsException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

public class SignUpConfirmation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("deprecation")
	protected AWSCognitoIdentityProviderClient cognitoClient = new AWSCognitoIdentityProviderClient();
       
    public SignUpConfirmation() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher("signupconfirmation.jsp").forward(request, response);
	}
	
	private boolean isEmpty(String str) {
		boolean empty = false;
		if (str == null || str.isEmpty()) {
			empty = true;
		}
		return empty;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String email = request.getParameter("inputEmail").trim();
		String encodedEmail = URLEncoder.encode(email, "UTF-8");
		String tempPassword = request.getParameter("inputTempPassword").trim();
		String finalPassword = request.getParameter("inputPassword").trim();
		
		// Check if values have been submitted
		if ( isEmpty(email) || isEmpty(tempPassword) || isEmpty(finalPassword) )
        {
            String encodedMsg = URLEncoder.encode("Sign up confirmation failed - check the email and passwords", "UTF-8");
        	request.getRequestDispatcher("signupconfirmation.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
		
        try
        {
            Map<String,String> initialParams = new HashMap<String,String>();
            initialParams.put("USERNAME", email);
            initialParams.put("PASSWORD", tempPassword);

            AdminInitiateAuthRequest initialRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withAuthParameters(initialParams)
                    .withClientId(System.getProperty("CognitoClientId"))
                    .withUserPoolId(System.getProperty("CognitoPoolId"));

            AdminInitiateAuthResult initialResponse = cognitoClient.adminInitiateAuth(initialRequest);
            if (! ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(initialResponse.getChallengeName()))
            {
                throw new RuntimeException("unexpected challenge: " + initialResponse.getChallengeName());
            }
   
            Map<String,String> challengeResponses = new HashMap<String,String>();
            challengeResponses.put("USERNAME", email);
            challengeResponses.put("PASSWORD", tempPassword);
            challengeResponses.put("NEW_PASSWORD", finalPassword);

            AdminRespondToAuthChallengeRequest finalRequest = new AdminRespondToAuthChallengeRequest()
                    .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                    .withChallengeResponses(challengeResponses)
                    .withClientId(System.getProperty("CognitoClientId"))
                    .withUserPoolId(System.getProperty("CognitoPoolId"))
                    .withSession(initialResponse.getSession());

            AdminRespondToAuthChallengeResult challengeResponse = cognitoClient.adminRespondToAuthChallenge(finalRequest);
            
            // Challenge should be empty after successfully verifying your account
            if ( isEmpty(challengeResponse.getChallengeName()) )
            {
    	        request.getRequestDispatcher("signin.jsp?success=true&email=" + encodedEmail).forward(request, response);
    	        return;
            } else {
            	 throw new RuntimeException("unexpected challenge: " + challengeResponse.getChallengeName());
            }
          
        }
        catch (InvalidPasswordException ex)
        {
        	String encodedMsg = URLEncoder.encode("Sign up confirmation failed - invalid password must be 8 character long, with at least 1 upper case and 1 special character", "UTF-8");
         	request.getRequestDispatcher("signupconfirmation.jsp?success=false&message=" + encodedMsg + "&email=" + encodedEmail).forward(request, response);
        }
        catch (UserNotFoundException ex)
        {
        	String encodedMsg = URLEncoder.encode("Sign up confirmation failed - user not found", "UTF-8");
         	request.getRequestDispatcher("signupconfirmation.jsp?success=false&message=" + encodedMsg + "&email=" + encodedEmail).forward(request, response);
        }
        catch (NotAuthorizedException ex)
        {
        	String encodedMsg = URLEncoder.encode("Sign up confirmation failed - not authorized", "UTF-8");
         	request.getRequestDispatcher("signupconfirmation.jsp?success=false&message=" + encodedMsg + "&email=" + encodedEmail).forward(request, response);
        }
        catch (TooManyRequestsException ex)
        {
        	String encodedMsg = URLEncoder.encode("Sign up confirmation failed - too many requests", "UTF-8");
         	request.getRequestDispatcher("signupconfirmation.jsp?success=false&message=" + encodedMsg + "&email=" + encodedEmail).forward(request, response);

        }   
	}
}
