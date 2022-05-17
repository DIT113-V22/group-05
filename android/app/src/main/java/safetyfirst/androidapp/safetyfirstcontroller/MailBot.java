package safetyfirst.androidapp.safetyfirstcontroller;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.Properties;
import java.io.IOException;
import java.util.Properties;

public class MailBot {

    Session newSession = null;
    MimeMessage mimeMessage = null;

    //Ossian
    public void sendEmail() throws MessagingException {
        String fromUser = "safetyfirst.emergencyservices@gmail.com";  //Enter sender email id (MUST BE GMAIL)
        String fromUserPassword = "Safetyfirst123";  //Enter sender gmail password , this will be authenticated by gmail smtp server
        String emailHost = "smtp.gmail.com";
        Transport transport = newSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserPassword);
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();
    }

    //Created by Ossian
    public MimeMessage draftEmail(String emailRecipient,String emailSubject ,String emailBody) throws MessagingException, IOException {
        mimeMessage = new MimeMessage(newSession);
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailRecipient));
        mimeMessage.setSubject(emailSubject);
        mimeMessage.setText(emailBody);
        return mimeMessage;
    }

    //Ossian
    public void setupServerProperties() {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.3");
        newSession = Session.getDefaultInstance(properties,null);
    }
}