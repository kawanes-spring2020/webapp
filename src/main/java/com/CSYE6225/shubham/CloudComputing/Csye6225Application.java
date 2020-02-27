package com.CSYE6225.shubham.CloudComputing;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
//code check
@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class Csye6225Application {

	public static void main(String[] args) {
		try {
			File input = new File("/home/ubuntu/application.properties");
			File input1 = new File("/home/ubuntu/S3Bucket.properties");
			Scanner sc = new Scanner(input);
			Scanner sc1 = new Scanner(input1);
			PrintWriter writer = new PrintWriter("/home/ubuntu/webapp/src/main/resources/application.properties", "UTF-8");
			PrintWriter writer1 = new PrintWriter("/home/ubuntu/webapp/target/classes/application.properties", "UTF-8");

			while(sc.hasNextLine()) {
			    String s = sc.nextLine();
			    writer.println(s);
			    writer1.println(s);
			}
			while(sc1.hasNextLine()) {
				String s1 = sc1.nextLine();
				writer.println(s1);
				writer1.println(s1);
			}
			
			writer.close();
			writer1.close();
			SpringApplication.run(Csye6225Application.class, args);

		}catch(Exception e) {
			e.printStackTrace();
		}
			}

}
