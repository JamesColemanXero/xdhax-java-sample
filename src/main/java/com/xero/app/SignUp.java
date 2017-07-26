package com.xero.app;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.*;

public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("deprecation")
	protected AWSCognitoIdentityProviderClient cognitoClient = new AWSCognitoIdentityProviderClient();

    public SignUp() {
        super();
    }

    private boolean isEmpty(String str) {
		boolean empty = false;
		if (str == null || str.isEmpty()) {
			empty = true;
		}
		return empty;
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    request.getRequestDispatcher("signup.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String email = request.getParameter("inputEmail");
		
		if ( isEmpty(email))
        {
            String encodedMsg = URLEncoder.encode("Sign up failed - email address blank", "UTF-8");
        	request.getRequestDispatcher("signup.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
		
        try
        {
            AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
                    .withUserPoolId(System.getProperty("CognitoPoolId"))
                    .withUsername(email)
                    .withUserAttributes(
                            new AttributeType()
                                .withName("email")
                                .withValue(email),
                            new AttributeType()
                                .withName("email_verified")
                                .withValue("true"))
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withForceAliasCreation(Boolean.FALSE);

            cognitoClient.adminCreateUser(cognitoRequest);

            String encodedEmail = URLEncoder.encode(email, "UTF-8");
            request.getRequestDispatcher("signupconfirmation.jsp?success=true&email=" + encodedEmail).forward(request, response);
           
        }
        catch (UsernameExistsException ex)
        {
        	System.out.println("UsernameExistsException");
            String encodedMsg = URLEncoder.encode("Sign up failed - account with this email address already exists", "UTF-8");
        	request.getRequestDispatcher("signup.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }

        catch (InvalidParameterException ex)
        {
        	System.out.println("InvalidParameterException");
        	String encodedMsg = URLEncoder.encode("Sign up failed - not a valid email format", "UTF-8");
         	request.getRequestDispatcher("signup.jsp?success=false&message=" + encodedMsg).forward(request, response);
         	return;
        }
        
        catch (TooManyRequestsException ex)
        {
        	System.out.println("TooManyRequestsException");
            String encodedMsg = URLEncoder.encode("Sign up failed - too many requests", "UTF-8");
        	request.getRequestDispatcher("signup.jsp?success=false&message=" + encodedMsg).forward(request, response);
        	return;
        }
	}
}
