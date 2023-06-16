package com.example.IBTim19.service;

import com.example.IBTim19.DTO.*;
import com.example.IBTim19.model.*;
import com.example.IBTim19.repository.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ActivationService activationService;
    @Autowired
    private ResetCodeRepository resetCodeRepository;
    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;
    @Autowired
    private TwoFactorRepository twoFactorRepository;

    @Value("${app.sendgrid.key}")
    private String appKey;
    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;
    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;

    public User findOneById(Integer id){
        if (userRepository.findOneById(id) == null){
            return null;
        }
        return userRepository.findOneById(id);
    }

    public User findOneByUsername(String username){
        return userRepository.findOneByUsername(username).orElse(null);
    }
    public User findOneByTelephone(String telephone){
        return userRepository.findOneUserByTelephone(telephone).orElse(null);
    }
    public User findIdByUsername(String username){
        return userRepository.findOneByUsername(username).orElse(null);

    }
    public User findOneUserByUsername(String username){
        return this.userRepository.findOneUserByUsername(username);
    }

    public User findOneLogin(String email, String password){return userRepository.findOneByUsernameAndPassword(email,password);}

    public List<User> findAll(){return userRepository.findAll();}

    public Page<User> findAll(Pageable page){
        return userRepository.findAll(page);

    }

    public User save(User user){return userRepository.save(user);}

    public void remove(Integer id){userRepository.deleteById(id);}

    public User createNewUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setLastname(userDTO.getLastname());
        user.setTelephone(userDTO.getTelephone());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(0);

        user = userRepository.save(user);
        activationService.createNewActivation(user, ActivationType.EMAIL, null);


        return user;
    }

    public AuthDTO login(String username, String password, String code){

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        }catch (Exception e){
            String res = "e";
            return new AuthDTO(res);
        }

        User user = userRepository.findOneUserByUsername(username);
        TwoFactor twoFactor = twoFactorRepository.findOneByUserId(user.getId()).orElse(null);

        if(user.getLastChanged().isBefore(LocalDateTime.now().minusMonths(3))){
            String res = "x";
            return new AuthDTO(res);
        }

        if (twoFactor == null || !twoFactor.getCode().equals(code)) {
            return null;
        }
        if(user.getIsActive() == 0){
            return null;
        }
        String jwtToken = jwtService.generateToken(user);

        twoFactorRepository.deleteById(twoFactor.getId());


        return new AuthDTO(jwtToken);
    }

    public Integer resetPassword(ResetDTO resetDTO) {

        User user;
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


        if(resetDTO.getType().equals(ResetType.MAIL)){
            user = userRepository.findOneUserByUsername(resetDTO.getUsername());
        }else{
            user = userRepository.findOneUserByTelephone(resetDTO.getTelephone()).get();
        }

        List<PasswordHistory> passwords = passwordHistoryRepository.findAllByUsername(user.getUsername());

        for( PasswordHistory ph : passwords){
            if (passwordEncoder.matches(resetDTO.getNewPassword(), ph.getPassword())){
                return 3; //old password
            }
        }

        ResetCode resetCode = resetCodeRepository.findOneByUsername(user.getUsername()).orElse(null);
        if(!resetCode.getCode().equals(resetDTO.getCode())){

            return 1; //do not match
        }
        if(resetCode.getDate().isBefore(LocalDateTime.now())){
            resetCodeRepository.deleteById(resetCode.getId());
            return 2; //expired
        }else{
            user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
            user.setLastChanged(LocalDateTime.now());
            userRepository.save(user);

            resetCodeRepository.deleteById(resetCode.getId());

            PasswordHistory ph = new PasswordHistory();
            ph.setUsername(user.getUsername());
            ph.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
            passwordHistoryRepository.save(ph);


            return 0;
        }
    }


    public void sentdTwoFactoreCode(LoginDTO loginDTO){
        User user = userRepository.findOneUserByUsername(loginDTO.getUsername());
        Integer code = randInt();

        TwoFactor twoFactor = new TwoFactor();
        twoFactor.setUser(user);
        twoFactor.setCode(code.toString());
        twoFactor.setType(loginDTO.getType());

        TwoFactor t = twoFactorRepository.findOneByUserId(user.getId()).orElse(null);
        if(t != null){
            twoFactorRepository.deleteById(t.getId());
        }
        twoFactorRepository.save(twoFactor);

        if(loginDTO.getType().equals(ActivationType.PHONE)){
            //TODO: posalji kod na sms
            sendResetCodeToTelephone(user.getTelephone(), code);
        }
            //TODO: posalji kod na mail
        try {
            sendCodeToMail(user.getUsername(),code);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendResetCodeToTelephone(String telephone, Integer code) {

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+381645638986"),  //to
                new com.twilio.type.PhoneNumber("+16205071936"),   //from
                "Your two factor code is: " + code).create();    //content

        System.out.println(message.getSid());

    }


    public void sendCodeToMail(String username, Integer code) throws MessagingException, UnsupportedEncodingException {
        Email from = new Email("UberAppTim19@gmail.com");
        from.setName("Tim19");
        String subject = "Reset password";

        Email to = new Email("tamara_dzambic@hotmail.com"); //ovde ide username

        String mailContent = "<p>Dear, user </p>";
        mailContent +="<p>This is Your two factor code:" + code +"</p>";
        mailContent +="<p>Thank you<br>Team 19</p>";

        Content content = new Content("text/html", mailContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(appKey);
        com.sendgrid.Request request = new Request();
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
