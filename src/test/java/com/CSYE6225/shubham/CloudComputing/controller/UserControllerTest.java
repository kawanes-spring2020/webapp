package com.CSYE6225.shubham.CloudComputing.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.CSYE6225.shubham.CloudComputing.model.User;
import com.CSYE6225.shubham.CloudComputing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
    private MockMvc mvc;
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	private WebApplicationContext wac;
	
	 @Before
	    public void setup () {
	        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
	        this.mvc = builder.build();
	    }
	 
	@Test
	public void testGetUser()  {
		String token;
		try {
			token = TokenAuthenticationService.createToken("puneettanwar@gmail.com", "lastName!@24");
			System.out.println(token);
			mvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/v1/user/self")
					.header("Authorization", "Basic " + token))
			.andExpect(status().is(200));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void creatUserTest() throws Exception 
	{
		
	  mvc.perform( MockMvcRequestBuilders
	      .post("http://localhost:8080/v1/user")
	      .content(asJsonString(new User("puneettanwar@gmail.com", "lastName!@24", "testuser","testing","bhdbsad","dfnsdf")))
	      .contentType(MediaType.APPLICATION_JSON)
	      .accept(MediaType.APPLICATION_JSON))
	      .andExpect(status().is(200));
	}
	 
	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
	
	@Test
	public void updateUserTest() throws Exception 
	{
		String token;
		token = TokenAuthenticationService.createToken("puneettanwar@gmail.com", "lastName!@24");
		mvc.perform(MockMvcRequestBuilders.put("http://localhost:8080/v1/user/self")
				.header("Authorization", "Basic " + token)
	      .content(asJsonString1(new User("puneettanwar@gmail.com", "lastName!@24", "ni761234ce","nice","bhdbsad","dfnsdf")))
	      .contentType(MediaType.APPLICATION_JSON)
	      .accept(MediaType.APPLICATION_JSON))
	      .andExpect(status().is(204));
	}
	 
	public static String asJsonString1(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}

