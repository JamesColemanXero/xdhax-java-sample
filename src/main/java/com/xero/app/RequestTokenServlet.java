package com.xero.app;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xero.api.Config;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;


public class RequestTokenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Config config = JsonConfig.getInstance(); 

	public RequestTokenServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
		OAuthRequestToken requestToken = new OAuthRequestToken(config);
		requestToken.execute();

		//Get ID for currently logged in User from Session
		HttpSession session = request.getSession(true);
		String uId = session.getAttribute("userId").toString();
		
		// DEMONSTRATION ONLY - Store in Cookie - you can extend Storage
		// and implement the save() method for your database
		Storage storage = new Storage();
		storage.save(uId,"users", requestToken.getAll());

		//Build the Authorization URL and redirect User
		OAuthAuthorizeToken authToken = new OAuthAuthorizeToken(config,requestToken.getTempToken());
		response.sendRedirect(authToken.getAuthUrl());	

	}
}
