package com.studentlife.StudentLifeAPIs.Service;

public interface EmailService {

    void sendInviteEmail(String toEmail, String inviterName, String assignmentTitle, Long assignmentId);

    void sendInviteAcceptedEmail(String toEmail, String acceptorName, String assignmentTitle);

    void sendDeclinedEmail(String toEmail, String declinerName, String assignmentTitle);
}
