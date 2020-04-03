package com.CSYE6225.shubham.CloudComputing.service;

import com.CSYE6225.shubham.CloudComputing.config.AmazonS3Config;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AmazonClient {

    private AmazonS3 s3client;
    private static AmazonSQS sqs;
    private static AmazonSNS amazonSNS;
    static String myQueueUrl = "";
    
    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${sqs.url}")
    private String sqsUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;
    @Value("${amazonProperties.region}")
    private String region;
    @Value("${sns.arn}")
    private String arn;

    private void initializeAmazon() {
//      AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(true))
                .build();
        
    }
    
    private void initializeSQS() {
//      BasicAWSCredentials bAWSc = new BasicAWSCredentials(accessKey, secretKey);
        this.sqs =  AmazonSQSClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(true)).build();
    }
    
    private void initializeSNS() {
//      BasicAWSCredentials bAWSc = new BasicAWSCredentials(accessKey, secretKey);
        this.amazonSNS = AmazonSNSClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(true))
                .build();
    }
    
    public void sendMessage(String email, int noOfDays,UUID user_id) {
        initializeSQS();
        String message = email+":"+String.valueOf(noOfDays)+":"+user_id;
        for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
            if(queueUrl.equals(sqsUrl)) {
                myQueueUrl = queueUrl;
            }
        }
        sqs.sendMessage(new SendMessageRequest(myQueueUrl,
                message));
        

    }
    
    public void publishSNSMessage(String message) {
        initializeSNS();

        System.out.println("Publishing SNS message: " + message);

        PublishResult result = this.amazonSNS.publish(arn, message);

        System.out.println("SNS Message ID: " + result.getMessageId());
    }
    
    public String receiveAndDelete() {
        System.out.println(myQueueUrl);
        String messageRet = "";
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for(Message message : messages) {
            System.out.println(message.getBody());
            messageRet = message.getBody();
            String messageRecieptHandle = message.getReceiptHandle();
            System.out.println("message deleted : " + message.getBody() + "." + message.getReceiptHandle());
            sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
        }
        return messageRet;
    }
  
 
    
    
    public String uploadFile(MultipartFile multipartFile) {
        initializeAmazon();
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            file.delete();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return fileUrl;
    }
    
  

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        try {
            this.s3client.putObject(bucketName, fileName, file);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    public String deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted";
    }

    

}
