package com.example.java_mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/*
 * @author ZZQ
 * @Date 2022/4/2 11:11 上午
 */
public class JavaMailSendDemo {
    public static void main(String[] args) {

        // 设置邮件发送内容
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 发件人: setFrom处必须填写自己的邮箱地址，否则会报553错误
        mailMessage.setFrom("support@yoycol.com");
        // 收件人
        mailMessage.setTo("416432541@qq.com");
        // 抄送收件人:网易邮箱要指定抄送收件人，不然会报 554（发送内容错误）
        // 主题
        mailMessage.setSubject("主题测试");
        // 内容
        mailMessage.setText("抄送收件人:网易邮箱要指定抄送收件人，不然会报 554（发送内容错误）");
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.sendgrid.net");
            mailSender.setUsername("send_key_0223");
            mailSender.setPassword("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");
            mailSender.setDefaultEncoding("UTF-8");
            Properties properties = new Properties();
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.socketFactory.port", "465");
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mailSender.setJavaMailProperties(properties);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessage.setFrom("support@yoycol.com");
            mimeMessage.setText("抄送收件人:网易邮箱要指定抄送收件人，不然会报 554（发送内容错误@yoycol.com");
            mimeMessage.setSentDate(new Date());
            InternetAddress[] parse = InternetAddress.parse("416432541@qq.com");
            mimeMessage.setRecipient(Message.RecipientType.TO, parse[0]);
            mailSender.send(mimeMessage);
            System.out.println("sucess");
        } catch (Exception e) {
            System.out.println("-----发送简单文本邮件失败!-------" + e.toString());
            e.printStackTrace();
        }
    }
}
