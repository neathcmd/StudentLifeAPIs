package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendInviteEmail(
            String toEmail,
            String inviterName,
            String assignmentTitle,
            Long assignmentId,
            String token
    ) {
        String acceptUrl = frontendUrl + "/invite/accept?token=" + token;
        String declineUrl = frontendUrl + "/invite/decline?token=" + token;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #4F46E5;">StudentLife</h2>
                <p>Hi,</p>
                <p><strong>%s</strong> invited you to collaborate on <strong>"%s"</strong>.</p>
                <p>This invitation expires in <strong>7 days</strong>.</p>
                <div style="margin: 32px 0; text-align: center;">
                    <a href="%s"
                       style="background-color: #4F46E5; color: white; padding: 12px 24px;
                              text-decoration: none; border-radius: 6px; margin-right: 12px;">
                       Accept
                    </a>
                    <a href="%s"
                       style="background-color: #e5e7eb; color: #374151; padding: 12px 24px;
                              text-decoration: none; border-radius: 6px;">
                       Decline
                    </a>
                </div>
                <p style="color: #9ca3af; font-size: 12px;">
                    If you did not expect this invitation, you can safely ignore this email.
                </p>
                <p style="color: #9ca3af; font-size: 12px;">— StudentLife</p>
            </div>
        """.formatted(inviterName, assignmentTitle, acceptUrl, declineUrl);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(inviterName + " invited you to \"" + assignmentTitle + "\"");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send invite email.", e);
        }
    }

    @Override
    public void sendInviteAcceptedEmail(String toEmail, String acceptorName, String assignmentTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Invite accepted");
        message.setText(
                "Hi,\n\n" +
                        acceptorName + " accepted your invitation to \"" + assignmentTitle + "\".\n\n" +
                        "— StudentLife"
        );
        mailSender.send(message);
    }

    @Override
    public void sendInviteDeclinedEmail(String toEmail, String declinerName, String assignmentTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Invite declined");
        message.setText(
                "Hi,\n\n" +
                        declinerName + " declined your invitation to \"" + assignmentTitle + "\".\n\n" +
                        "— StudentLife"
        );
        mailSender.send(message);
    }
}
