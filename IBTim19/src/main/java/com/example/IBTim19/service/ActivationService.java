package com.example.IBTim19.service;

import com.example.IBTim19.model.Activation;
import com.example.IBTim19.model.ActivationType;
import com.example.IBTim19.model.ResetCode;
import com.example.IBTim19.model.User;
import com.example.IBTim19.repository.ActivationRepository;
import com.example.IBTim19.repository.ResetCodeRepository;
import com.example.IBTim19.repository.UserRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Service
public class ActivationService {

    @Autowired
    private ActivationRepository activationRepository;
    @Autowired
    private ResetCodeRepository resetCodeRepository;
    @Autowired
    private UserRepository userRepository;
    @Value("${app.sendgrid.key}")
    private String appKey;

    public Activation save(Activation activation){return activationRepository.save(activation);}

    public Activation findOneByUserId(Integer id){return activationRepository.findOneByUserId(id).orElse(null);}

    public void remove(Integer id){activationRepository.deleteById(id);}

    public void createNewActivation(User user, ActivationType type, Integer code) {
        Activation activation = new Activation();
        activation.setType(type);
        activation.setId(user.getId());
        activation.setUser(user);
        activation.setCreationDate(LocalDateTime.now());
        activation.setExpirationDate(LocalDateTime.now().plusYears(5));

        if(type.equals(ActivationType.PHONE)){
            activation.setCode(code);
        }
        activationRepository.save(activation);
    }

    public void sendVerificationMail(String username, Integer id) throws MessagingException, UnsupportedEncodingException {
        Email from = new Email("UberAppTim19@gmail.com");
        from.setName("Tim19");
        String subject = "Please verify your account";

        Email to = new Email("tamara_dzambic@hotmail.com"); //ovde ide username

        String mailContent = "<p>Dear, user </p>";
        mailContent +="<p>Please click the link below to verify your registration:</p>";
        mailContent +="<h3><a href=\"" + "http://localhost:8085/api/user/activate/" + id + "\">VERIFY</a></h3>";
        mailContent +="<p>Thank you<br>Team 19</p>";

        Content content = new Content("text/html", mailContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(appKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void sendVerificationCode(String phoneNumber){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        user.setTelephone(phoneNumber);

        Integer code = randInt();

        String text = "Your verification code is" + code;
        //TODO: send verification code to phone number


        createNewActivation(user, ActivationType.PHONE, code);
    }

    public Integer verifyPhoneNumber(Integer code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Activation activation = activationRepository.findOneByUserId(user.getId()).orElse(null);
        if (activation == null) {
            return 1;  //not found
        }
        if(!activation.getCode().equals(code)){
            return 2; //do not match
        }
        activationRepository.deleteById(activation.getId());
        if(activation.getExpirationDate().isBefore(LocalDateTime.now())){
            return 3;  //expired
        }

        return 0;
    }


    public Integer sendResetCode(Integer type, String userr) {
        User user;

        if(type == 0){
            user = userRepository.findOneUserByTelephone(userr);

        }else{
            user = userRepository.findOneUserByUsername(userr);
        }

        Integer code = randInt();

        ResetCode resetCode = resetCodeRepository.findOneByUsername(user.getUsername()).orElse(new ResetCode());
        resetCode.setUsername(user.getUsername());
        resetCode.setCode(code);
        resetCode.setDate(LocalDateTime.now().plusMinutes(15));

        resetCodeRepository.save(resetCode);


        if(type==0){
            sendResetCodeToTelephone(user.getTelephone(), code);
            return 0;
        }
        try {
            sendCodeToMail(user.getUsername(),code);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private void sendResetCodeToTelephone(String telephone, Integer code) {
        String text = "Your reset code is" + code;
        //TODO: send reset code to phone number

    }


    public void sendCodeToMail(String username, Integer code) throws MessagingException, UnsupportedEncodingException {
        Email from = new Email("UberAppTim19@gmail.com");
        from.setName("Tim19");
        String subject = "Reset password";

        Email to = new Email("tamara_dzambic@hotmail.com"); //ovde ide username

        String mailContent = "<p>Dear, user </p>";
        mailContent +="<p>This is Your reset code:" + code +"</p>";
        mailContent +="<p>Thank you<br>Team 19</p>";

        Content content = new Content("text/html", mailContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(appKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    private Integer randInt(){
        double r = Math.random();
        int randomNum = (int)(r * (9999 - 1000)) + 1000;
        return randomNum;
    }

}
