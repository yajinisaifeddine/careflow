package com.careflow.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;


    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("My Application <noreply@example.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send an HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("no-reply@careflow.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email with attachment
     */
    public void sendEmailWithAttachment(String to, String subject, String text,
                                        String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("My Application <your-email@gmail.com>");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("Email with attachment sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email with multiple recipients
     */
    public void sendEmailToMultipleRecipients(String[] to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("My Application <your-email@gmail.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to {} recipients", to.length);
        } catch (Exception e) {
            log.error("Failed to send email to multiple recipients", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email with CC and BCC
     */
    public void sendEmailWithCcAndBcc(String to, String subject, String text,
                                      String[] cc, String[] bcc) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@example.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }
            if (bcc != null && bcc.length > 0) {
                helper.setBcc(bcc);
            }

            mailSender.send(message);
            log.info("Email with CC/BCC sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with CC/BCC to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
