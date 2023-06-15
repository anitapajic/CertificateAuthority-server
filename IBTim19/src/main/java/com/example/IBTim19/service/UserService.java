package com.example.IBTim19.service;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.ResetDTO;
import com.example.IBTim19.DTO.ResetType;
import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.*;
import com.example.IBTim19.repository.PasswordHistoryRepository;
import com.example.IBTim19.repository.ResetCodeRepository;
import com.example.IBTim19.repository.UserRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public AuthDTO login(String username, String password){

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findOneUserByUsername(username);

        if(user.getLastChanged().isBefore(LocalDateTime.now().minusMonths(3))){
            String res = "x";
            return new AuthDTO(res);
        }
        if(user.getIsActive() == 0){
            return null;
        }
        String jwtToken = jwtService.generateToken(user);



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

}
