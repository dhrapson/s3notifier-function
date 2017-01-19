package com.wtr.s3notifier.email;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.mockito.Mockito;

public class EmailManagerTest {

    @Test
    public void testS3BucketHandler() throws Exception {

        EmailManager handlerReal = new EmailManager("email-smtp.eu-west-1.amazonaws.com", 25, "sometestuser", "sometestpwd", "test@example.com");
        EmailManager handlerSpy = Mockito.spy(handlerReal);

        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        Session session = Session.getDefaultInstance(props);
        Transport mockTransport = Mockito.mock(Transport.class);
        MimeMessage message = Mockito.spy(new MimeMessage(session));

        when(handlerSpy.createMessage(session)).thenReturn(message);
        when(handlerSpy.createTransport(session)).thenReturn(mockTransport);

        handlerSpy.sendEmail("mailto@example.com", "the subject", "the body");

        verify(mockTransport).connect("email-smtp.eu-west-1.amazonaws.com", "sometestuser", "sometestpwd");
        verify(mockTransport).sendMessage(message, message.getAllRecipients());
        verify(mockTransport).close();
        verify(message).setFrom(new InternetAddress("test@example.com"));
        verify(message).setRecipient(Message.RecipientType.TO, new InternetAddress("mailto@example.com"));
        verify(message).setSubject("the subject");
        verify(message).setContent("the body", "text/plain");
    }
}
