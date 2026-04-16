package com.studentlife.StudentLifeAPIs.Service;

public interface EmailService {

    void sendInviteEmail(
            String toEmail,
            String inviterName,
            String assignmentTitle,
            Long assignmentId,
            String token
    );

    void sendInviteAcceptedEmail(String toEmail, String acceptorName, String assignmentTitle);

    void sendInviteDeclinedEmail(String toEmail, String declinerName, String assignmentTitle);
}
