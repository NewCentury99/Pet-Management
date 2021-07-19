package com.sju18.petmanagement.global.util.email;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final Logger logger = LogManager.getLogger();
    public static final String authCode = createAuthCode();

    // 메일인증 안내메일의 제목 및 내용 생성
    private MimeMessage createVerificationMessage(String to)throws Exception{
        logger.info("보내는 대상 : "+ to);
        logger.info("인증 번호 : " + authCode);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to); //보내는 대상
        message.setSubject("Pet Management 서비스 회원가입 인증 코드: " + authCode); //제목

        String verificationMessage="";
        verificationMessage += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">이메일 주소 확인</h1>";
        verificationMessage += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 회원가입 인증코드란에 입력하세요.</p>";
        verificationMessage += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        verificationMessage += authCode;
        verificationMessage += "</td></tr></tbody></table></div>";

        message.setText(verificationMessage, "utf-8", "html"); //내용
        message.setFrom(new InternetAddress("smyun99@gmail.com","pet-management")); //보내는 사람

        return message;
    }

    // 인증코드 만들기
    public static String createAuthCode() {
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) { // 인증코드 6자리
            code.append((rnd.nextInt(10)));
        }
        return code.toString();
    }

    // 인증 메시지 발송
    public void sendVerificationMessage(String to)throws Exception {
        MimeMessage message = createVerificationMessage(to);
        try{
            //예외처리
            emailSender.send(message);
        } catch(MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
}
