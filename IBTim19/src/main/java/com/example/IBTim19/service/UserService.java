package com.example.IBTim19.service;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.Activation;
import com.example.IBTim19.model.Role;
import com.example.IBTim19.model.User;
import com.example.IBTim19.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private JavaMailSender mailSender;
    @Autowired
    private ActivationService activationService;

    public User findOneById(Integer id){
        if (userRepository.findOneById(id) == null){
            return null;
        }
        return userRepository.findOneById(id);
    }

    public User findOneByUsername(String username){
        return userRepository.findOneByUsername(username).orElse(null);
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
        user.setActive(false);

        user = userRepository.save(user);
        Activation activation = new Activation();
        activation.setId(user.getId());
        activation.setUser(user);
        activation.setCreationDate(LocalDateTime.now());
        activation.setExpirationDate(LocalDateTime.now().plusYears(5));

        activationService.save(activation);

        return user;
    }

    public AuthDTO login(String username, String password){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRepository.findOneUserByUsername(username);
        String jwtToken = jwtService.generateToken(user);

        return new AuthDTO(jwtToken);
    }

    public void sendVerificationMail(String username, Integer id) throws MessagingException, UnsupportedEncodingException {
        String subject = "Please verify your account";
        String senderName = "Tim19";

        String mailContent = "<p>Dear, user </p>";
        mailContent +="<p>Please click the link below to verify your registration:</p>";
        mailContent +="<h3><a href=\"" + "http://localhost:8085/api/user/activate/" + id + "\">VERIFY</a></h3>";
        mailContent +="<p>Thank you<br>Team 19</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("UberAppTim19@gmail.com", senderName);
        helper.setTo("tamara_dzambic@hotmail.com"); //ovde treba mail korisnika
        helper.setSubject(subject);
        helper.setText(mailContent, true);

        mailSender.send(message);
    }
    public void sendResetCodeMail(String username, Integer id) throws MessagingException, UnsupportedEncodingException {
        String subject = "Please verify your account";
        String senderName = "Tim19";

        String mailContent = "<p>Dear, user </p>";
        mailContent +="<p>Please click the link below to verify your registration:</p>";
        mailContent +="<h3><a href=\"" + "http://localhost:8085/api/user/activate/" + id + "\">VERIFY</a></h3>";
        mailContent +="<p>Thank you<br>Team 19</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("UberAppTim19@gmail.com", senderName);
        helper.setTo("tamara_dzambic@hotmail.com"); //ovde treba mail korisnika
        helper.setSubject(subject);
        helper.setText(mailContent, true);

        mailSender.send(message);
    }

    private Integer randInt(){
        double r = Math.random();
        int randomNum = (int)(r * (9999 - 1000)) + 1000;
        return randomNum;
    }
}
