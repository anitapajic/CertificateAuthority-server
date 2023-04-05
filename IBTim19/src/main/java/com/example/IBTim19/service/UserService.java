package com.example.IBTim19.service;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.Role;
import com.example.IBTim19.model.User;
import com.example.IBTim19.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;


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

        return userRepository.save(user);
    }

    public AuthDTO login(String username, String password){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRepository.findOneUserByUsername(username);
        String jwtToken = jwtService.generateToken(user);

        return new AuthDTO(jwtToken);
    }
}
