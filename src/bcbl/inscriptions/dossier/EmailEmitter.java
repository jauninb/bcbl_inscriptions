package bcbl.inscriptions.dossier;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailEmitter {

	private Session session;
	private String userName;
	private String titleTemplate;
	private String messageTemplate;

	public EmailEmitter(String host, int port, String userName, String password, String title, String message) {
		this.userName = userName;
		// sets SMTP server properties
		Properties properties = new Properties();
		properties.put("mail.debug", true);
		properties.put("mail.user", userName);
		properties.put("mail.password", password);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		};
		this.session = Session.getInstance(properties, auth);
		this.titleTemplate = title;
		this.messageTemplate = message;
	}

	public void sendEmail(Licencie fbi, Licencie bcbl, File[] attachments) throws AddressException, MessagingException {
		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(this.userName));
		ArrayList<InternetAddress> toAddresses = new ArrayList<InternetAddress>(2);
		if (bcbl.email1 != null) {
			toAddresses.add(new InternetAddress(bcbl.email1));
		}
		if (bcbl.email2 != null) {
			toAddresses.add(new InternetAddress(bcbl.email2));
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses.toArray(new InternetAddress[toAddresses.size()]));
		msg.setRecipients(Message.RecipientType.CC, new InternetAddress[] {new InternetAddress(this.userName)});
		msg.setSubject(MessageFormat.format(titleTemplate, bcbl.nom, bcbl.prenom));
		msg.setSentDate(new Date());

		// creates message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(MessageFormat.format(messageTemplate, bcbl.nom, bcbl.prenom));

		// creates multi-part
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// adds attachments
		if (attachments != null && attachments.length > 0) {
			for (File file : attachments) {
				MimeBodyPart attachPart = new MimeBodyPart();

				try {
					attachPart.attachFile(file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				multipart.addBodyPart(attachPart);
			}
		}

		// sets the multi-part as e-mail's content
		msg.setContent(multipart);

		// sends the e-mail
		Transport.send(msg);

	}

}
