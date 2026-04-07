package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendInviteEmail(String toEmail, String inviterName, String assignmentTitle, Long assignmentId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You've been invited to join an assignment");
        message.setText(
                "Hi,\n\n" +
                        inviterName + " invited you to collaborate on \"" + assignmentTitle + "\".\n\n" +
                        "Open the app to accept or decline the invitation.\n\n" +
                        "Assignment ID: " + assignmentId + "\n\n" +
                        "— StudentLife"
        );
        mailSender.send(message);
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
    public void sendDeclinedEmail(String toEmail, String declinerName, String assignmentTitle) {
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
