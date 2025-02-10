package com.example.thuan.ultis;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// Cái này để gửi mã otp khi xác thực email
@Service
public class EmailSenderUtil {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtUtil JwtUtil;

    public void sendEmail(String toEmail, String otp) {

        // String jwtToken = JwtUtil.generateToken(toEmail, otp);
        String subject = "Email Verification - ReadHasha Book";
        String body = "Hello,\n\n"
                + "Your OTP code for email verification is: <b>" + otp + "</b>\n\n"
                + "Please enter this code in the application to verify your account.\n\n"
                + "Best regards,\n ReadHasha Team";

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("thuanntce181024@fpt.edu.vn");
            helper.setTo(toEmail);
            helper.setText(body, true);
            helper.setSubject(subject);

            mailSender.send(message);
            System.out.printf("Mail Sent Successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
