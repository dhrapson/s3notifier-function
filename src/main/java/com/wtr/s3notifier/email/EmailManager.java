package com.wtr.s3notifier.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class EmailManager {
	 private static final Logger log = LogManager.getLogger(EmailManager.class);
	private String smtpHost;
	private int smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private String emailFrom;
	 
	 public EmailManager(String smtpHost, int smtpPort, String smtpUsername, String smtpPassword, String emailFrom) {
		 this.smtpHost = smtpHost;
		 this.smtpPort = smtpPort;
		 this.smtpUsername = smtpUsername;
		 this.smtpPassword = smtpPassword;
		 this.emailFrom = emailFrom;
	 }
	 
	 public void sendEmail(String emailTo, String subject, String body ) {
		
    	
    	try {
    			
	        Properties props = System.getProperties();
	    	props.put("mail.transport.protocol", "smtps");
	    	props.put("mail.smtp.port", smtpPort);
	    	props.put("mail.smtp.auth", "true");
	    	props.put("mail.smtp.starttls.enable", "true");
	    	props.put("mail.smtp.starttls.required", "true");
	
	        // Create a Session object to represent a mail session with the specified properties. 
	    	Session session = Session.getDefaultInstance(props);
	
	        // Create a message with the specified information. 
	    	MimeMessage msg = createMessage(session);
	        msg.setFrom(new InternetAddress(emailFrom));
	        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
	        msg.setSubject(subject);
	        msg.setContent(body,"text/plain");
	            
	        // Create a transport.        
	        Transport transport = createTransport(session);
	                    
	        // Send the message.
	        try
	        {
	            log.info("Attempting to send an email through the Amazon SES SMTP interface...");
	            
	            // Connect to Amazon SES using the SMTP username and password you specified above.
	            transport.connect(smtpHost, smtpUsername, smtpPassword);
	        	
	            // Send the email.
	            transport.sendMessage(msg, msg.getAllRecipients());
	            log.info("Email sent!");
	        }
	        catch (Exception ex) {
	            log.error("The email was not sent.");
	            log.error("Error message: " + ex.getMessage());
	        }
	        finally
	        {
	            // Close and terminate the connection.
	            transport.close();        	
	        }
    	} catch (Exception ex) {
    		throw new RuntimeException(ex);
    	}
	}
	 
	 Transport createTransport(Session session) throws NoSuchProviderException {
			return session.getTransport();
		}
	    
	 
	   MimeMessage createMessage(Session session) {
	    	 return new MimeMessage(session);
	    }
}
