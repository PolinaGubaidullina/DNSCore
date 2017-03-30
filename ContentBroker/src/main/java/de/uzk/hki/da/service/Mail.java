/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.uzk.hki.da.service;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.model.PendingMail;

/**
 * The Class Mail.
 * 
 * @author Jens Peters
 */
public class Mail {
	private static final Logger logger = LoggerFactory.getLogger(Mail.class);

	/**
	 * Send a mail.
	 *
	 * @param nodeName
	 *            the name of node sending the message
	 * @param fromAdress
	 *            the from address
	 * @param toAdress
	 *            the to address
	 * @param subject
	 *            the subject
	 * @param mailText
	 *            the mail text
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public static void queueMail(String nodeName, String fromAdress, String toAdress, String subject, String mailText, Boolean pooled) throws MessagingException {
		logger.debug("from: " + fromAdress);
		logger.debug("to: " + toAdress);
		logger.debug("subject: " + subject);
		logger.debug("body: " + mailText);
		if (toAdress.toLowerCase().startsWith("noreply")) {
			logger.debug("was not sent due to noreply");
			return;
		}

		org.hibernate.Session session = HibernateUtil.openSession();

		PendingMail pMail = new PendingMail();

		pMail.setCreated(new Date());
		pMail.setRetries(0);
		pMail.setPooled(Boolean.TRUE == pooled);

		pMail.setFromAddress(fromAdress);
		pMail.setToAddress(toAdress);
		pMail.setSubject(subject);
		pMail.setMessage(mailText);
		pMail.setNodeName(nodeName);
		
		Transaction transi = session.beginTransaction();
		session.save(pMail);
		transi.commit();

		session.close();
	}

	public static void sendMail(String smtpHost, String fromAdress, String toAdress, String subject, String mailText) throws MessagingException {
		logger.debug("from: " + fromAdress);
		logger.debug("to: " + toAdress);
		logger.debug("subject: " + subject);
		logger.debug("body: " + mailText);
		if (toAdress.toLowerCase().startsWith("noreply")) {
			logger.debug("was not sent due to noreply");
			return;
		}

		Properties props = new Properties();
		// props.setProperty("mail.smtp.host", "mail.lvrintern.lvr.de");
		if (smtpHost != null) {
			props.setProperty("mail.smtp.host", smtpHost);
		}
		
		Session session = Session.getInstance(props, null);
		MimeMessage message = new MimeMessage(session);

		message.setContent(mailText, "text/plain; charset=utf-8");
		message.setSubject(subject);

		Address toAddress = new InternetAddress(toAdress);
		Address fromAddress = new InternetAddress(fromAdress);
		message.setFrom(fromAddress);
		message.setRecipient(Message.RecipientType.TO, toAddress);

		Transport.send(message);
		logger.info(subject + " was sent!");
	}
}
