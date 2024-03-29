package com.example.IBTim19.service;

import com.example.IBTim19.DTO.ResetType;
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
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

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
    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;
    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;

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

        if(user.getTelephone() == null)   //ako nema sacuvaj
            user.setTelephone(phoneNumber);
        if(!user.getTelephone().equals(phoneNumber)) //ako ima proveri da li je to taj broj
            return;

        Integer code = randInt();

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+381645638986"),  //to
                new com.twilio.type.PhoneNumber("+16205071936"),   //from
                "Your verification code is: " + code).create();    //content


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

        user.setIsActive(2);
        userRepository.save(user);

        return 0;
    }


    public Integer sendResetCode(ResetType type, String userr) {
        Optional<User> userO;
        User user;
        if(type.equals(ResetType.TELEPHONE)){
            userO = userRepository.findOneUserByTelephone(userr);

        }else{
            userO = userRepository.findUserByUsername(userr);
        }
        if(userO.isPresent()){
            user = userO.get();
        }
        else {
            return 2;
        }

        Integer code = randInt();

        ResetCode resetCode = resetCodeRepository.findOneByUsername(user.getUsername()).orElse(new ResetCode());
        resetCode.setUsername(user.getUsername());
        resetCode.setCode(code);
        resetCode.setDate(LocalDateTime.now().plusMinutes(15));

        resetCodeRepository.save(resetCode);


        if(type.equals(ResetType.TELEPHONE)){
            sendResetCodeToTelephone(user.getTelephone(), code);
            return 0;
        }
        try {
            sendCodeToMail(user.getUsername(),code);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private void sendResetCodeToTelephone(String telephone, Integer code) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+381645638986"),  //to
                new com.twilio.type.PhoneNumber("+16205071936"),   //from
                "Your reset code is: " + code).create();    //content


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
