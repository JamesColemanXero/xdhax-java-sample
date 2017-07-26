package com.xero.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xero.api.OAuthAccessToken;
import com.xero.api.XeroApiException;
import com.xero.api.XeroClient;
import com.xero.model.Organisation;
import com.xero.api.Config;

public class CallbackServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private Config config = JsonConfig.getInstance();
	
	public CallbackServlet() 
	{
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		Storage storage = new Storage();
		String verifier = request.getParameter("oauth_verifier");

		HttpSession session = request.getSession();
		String uId = session.getAttribute("userId").toString();
				
		// Swap your temp token for 30 oauth token
		// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
		OAuthAccessToken accessToken = new OAuthAccessToken(config);
		accessToken.build(verifier,storage.get(uId,"users","tempToken"),storage.get(uId,"users","tempTokenSecret")).execute();
		
		if(!accessToken.isSuccess())
		{
			storage.clear(uId);
			request.getRequestDispatcher("index.jsp").forward(request, response);
		}
		else 
		{
			HashMap<String,String> tokenMap = new HashMap<String,String>();
			tokenMap.put("token", accessToken.getAll().get("token").toString());
			tokenMap.put("tokenSecret", accessToken.getAll().get("tokenSecret").toString());
			tokenMap.put("tokenTimestamp", accessToken.getAll().get("tokenTimestamp").toString());
			if(config.getAppType().equals("PARTNER")) {
				tokenMap.put("sessionHandle", accessToken.getAll().get("sessionHandle").toString());
			}
			
			storage.save(uId,"users", tokenMap);	
			
			String token = storage.get(uId,"users","token");
			String tokenSecret = storage.get(uId,"users","tokenSecret");
	     
			if(storage.tokenIsNull(token)) {
				request.getRequestDispatcher("settings.jsp").forward(request, response);
			} 
			
			OAuthAccessToken refreshToken = new OAuthAccessToken(config);
			
			String tokenTimestamp = storage.get(uId,"users", "tokenTimestamp");
			if(config.getAppType().equals("PARTNER") && refreshToken.isStale(tokenTimestamp)) {
				
				refreshToken.setToken(storage.get(uId,"users","token"));
				refreshToken.setTokenSecret(storage.get(uId,"users","tokenSecret"));
				refreshToken.setSessionHandle(storage.get(uId,"users","sessionHandle"));

				boolean success = refreshToken.build().execute();
				if (!success) {
					try {
						request.getRequestDispatcher("index.jsp").forward(request, response);
					} catch (ServletException e) {
						e.printStackTrace();
					}
				}
				
				storage.save(uId,"users", refreshToken.getAll());
				token =  refreshToken.getToken();
				tokenSecret = refreshToken.getTokenSecret();
			}
			
			// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
			XeroClient client = new XeroClient(config);
			client.setOAuthToken(token, tokenSecret);
			
			/*  ORGANISATION */
			try {
				List<Organisation> organisation = client.getOrganisations();
			
				storage.update(uId,"users", "org_name", organisation.get(0).getName());
				storage.update(uId,"users", "org_shortcode", organisation.get(0).getShortCode());
				
				request.setAttribute("connected", "true"); 
				request.setAttribute("hasToken", "true"); 
				request.setAttribute("orgName", organisation.get(0).getName()); 
				request.setAttribute("message", "You've successfully connected to Xero."); 
		        request.getRequestDispatcher("settings.jsp").forward(request, response);	
				
			} catch (XeroApiException e) {
				System.out.println(e.getResponseCode());
				System.out.println(e.getMessage());	
			}
		}
	}	
}