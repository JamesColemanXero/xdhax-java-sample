package com.xero.app;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.xero.api.Config;
import com.xero.api.OAuthAccessToken;
import com.xero.api.XeroClient;

public class Client extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Config config = JsonConfig.getInstance();
	private Storage storage = new Storage();
	boolean success = false;

	// Pass Custom Config to XeroClient so keys/secrets pulled from  AWS
	OAuthAccessToken refreshToken = new OAuthAccessToken(config);

	public Client() {
        super();
    }

    public XeroClient init(HttpServletRequest request) {
    	
    	//Get ID for currently logged in User from Session
    	HttpSession session = request.getSession();
    	String uId = session.getAttribute("userId").toString();
    	String token = storage.get(uId,"users","token");
		String tokenSecret = storage.get(uId,"users","tokenSecret");
		String tokenTimestamp = storage.get(uId,"users", "tokenTimestamp");
		
		if (!storage.tokenIsNull(token)) {
			success = true;
		}
		
		if(config.getAppType().equals("PARTNER") && refreshToken.isStale(tokenTimestamp)) {
			refreshToken.setToken(storage.get(uId,"users", "token"));
			refreshToken.setTokenSecret(storage.get(uId,"users","tokenSecret"));
			refreshToken.setSessionHandle(storage.get(uId,"users","sessionHandle"));

			try {
				success = refreshToken.build().execute();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			storage.save(uId,"users", refreshToken.getAll());
			token =  refreshToken.getToken();
			tokenSecret = refreshToken.getTokenSecret();
		}
		
		
		XeroClient client = new XeroClient(config);
		client.setOAuthToken(token, tokenSecret);
    	return client;
    }
    
    public boolean publicTokenExpired(String uId) {    	
    	boolean expired = false;
    	String tokenTimestamp = storage.get(uId,"users", "tokenTimestamp");

		if(config.getAppType().equals("PUBLIC") && refreshToken.isStale(tokenTimestamp)) {
			storage.clear(uId);
			storage.delete(uId, "invoices");
			expired = true;			
		}
    	
       	return expired;
    }
    
    public boolean isSuccess() {
    	return success;
    }
}
