package com.example.IBTim19.DTO;

import com.example.IBTim19.model.ActivationType;
import com.example.IBTim19.model.ResetCode;
import com.example.IBTim19.model.User;

public class LoginDTO {
    private String username;
    private String password;
    private String code;

    private ActivationType type;


    public LoginDTO(String email, String password, String code, ActivationType type) {
        this.username = email;
        this.password = password;
        this.code = code;
        this.type = type;
    }

    public LoginDTO(String email, String password) {
        this.username = email;
        this.password = password;
    }

    public LoginDTO(){}

    public LoginDTO(User user){
        this(user.getUsername(), user.getPassword());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCode() {return code;}

    public ActivationType getType() {return type;}
}
