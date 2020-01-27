package com.CSYE6225.shubham.CloudComputing.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CSYE6225.shubham.CloudComputing.model.User;
import com.CSYE6225.shubham.CloudComputing.model.UserReturn;
import com.CSYE6225.shubham.CloudComputing.repository.UserRepository;


@RestController
@RequestMapping("/v1")
public class UserController {

	@Autowired
	UserRepository repository;
	
	@Autowired
    PasswordEncoder encoder;
	
	

	@GetMapping("/user/self")
	public ResponseEntity<UserReturn> getUser(@RequestHeader HttpHeaders headers) {
		String username = "";
		String password = "";
		User _user = null;
		final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
		    String base64Credentials = authorization.substring("Basic".length()).trim();
		    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
		    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
		    final String[] values = credentials.split(":", 2);
		    username = values[0];
		    password = values[1];
		}
		_user = repository.findByEmail(username);
		if(repository.existsByEmail(username) && encoder.matches(password, _user.getPassword())) {
			UserReturn returnUser = new UserReturn(_user.getId(),_user.getEmail(),_user.getFirst_name(), _user.getLast_name(), _user.getAccount_created(), _user.getAccount_updated());
			return ResponseEntity.ok().body(returnUser);
			
		}
		else {
			return ResponseEntity.badRequest().body(null);
		}
		
	}

	@PostMapping(value = "/user")
	public ResponseEntity<UserReturn> postUser(@RequestBody User user) {
		System.out.println("inside"+user);
		String regex = "^(.+)@(.+)$";
		 
		Pattern pattern = Pattern.compile(regex);
		 
		    
	    if (!user.getEmail().matches(regex))
        {
	    	System.out.println("email"+user.getEmail().matches(regex));
            return ResponseEntity.badRequest().body(null);
        }    
		//if(repository.existsByEmail(user.getEmail())) {
		//	System.out.println("email exist"+repository.existsByEmail(user.getEmail()));
            //return ResponseEntity.badRequest().body(null);
       // }
		String specialChars = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
        if (!user.getPassword().matches(specialChars))
        {	
        	System.out.println("PWD");
            return ResponseEntity.badRequest().body(null);
        }
		User _user = repository.save(new User(user.getEmail(), encoder.encode(user.getPassword()), user.getFirst_name(), user.getLast_name(),"",""));
		UserReturn returnUser = new UserReturn(_user.getId(),_user.getEmail(),_user.getFirst_name(), _user.getLast_name(), _user.getAccount_created(), _user.getAccount_updated());
		System.out.println("reached"+returnUser);
		return  ResponseEntity.ok().body(returnUser);
	}
	
	@PutMapping(value = "/user/self")
	public ResponseEntity<String> updateUser(@RequestBody User user, @RequestHeader HttpHeaders headers) {
		String username = "";
		String password = "";
		User _user = null;
		final String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
		    String base64Credentials = authorization.substring("Basic".length()).trim();
		    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
		    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
		    final String[] values = credentials.split(":", 2);
		    System.out.println(values[0]+""+values[1]);
		    username = values[0];
		    password = values[1];
		}
		_user = repository.findByEmail(username);
		if(_user!=null && encoder.matches(password, _user.getPassword())) {
			if(repository.existsByEmail(user.getEmail())) {
				
//	            return ResponseEntity.badRequest().body("Existing Email Address");
	        
			String specialChars = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
	        if (!user.getPassword().matches(specialChars))
	        {
            	 return ResponseEntity.badRequest().body("Please enter Strong password");
             }
				
			
//	        _user.setEmail(user.getEmail());
	        _user.setPassword(encoder.encode(user.getPassword()));
	        _user.setFirst_name(user.getFirst_name());
	        _user.setLast_name(user.getLast_name());
	        _user.setAccount_updated(LocalDateTime.now().toString());
			
			repository.save(_user);
			
			return ResponseEntity.noContent().build();
			}
			else {
				return ResponseEntity.badRequest().body("Email cannot be updated");
			}
		}
		else {
			return ResponseEntity.badRequest().body("Please enter correct credentials");
		}
		
	}
		
	
	
	
}

