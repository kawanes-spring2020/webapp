package com.CSYE6225.shubham.CloudComputing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
@Configuration
public class AmazonS3Config 
{
    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Bean(name = "accessKey")
    public String getAWSKeyId() {
        return accessKey;
    }

    @Bean(name = "secretKey")
    public String getAWSKeySecret() {
        return secretKey;
    }

    @Bean(name = "endpointUrl")
    public String getAWSPollyRegion() {
        return endpointUrl;
    }

    

    @Bean(name = "bucketName")
    public String getAWSS3AudioBucket() {
        return bucketName;
    }
}