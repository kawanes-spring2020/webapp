package com.CSYE6225.shubham.CloudComputing.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class TokenAuthenticationService {
	public static String createToken(String username, String password) throws UnsupportedEncodingException {
		String token = Base64.getEncoder().encodeToString((username+":"+password).getBytes("UTF-8"));

		
		return token;
    }
}


