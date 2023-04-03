package com.example.IBTim19.DTO;


import com.example.IBTim19.model.User;
import lombok.Data;

@Data
public class UserDTO {
    private Integer id;

    private String username;
    private String name;
    private String lastname;
    private String telephone;
    private String password;

    public UserDTO(Integer id, String username, String firstname, String lastname, String telephone, String password) {
        this.id = id;
        this.username = username;
        this.name = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
        this.password = password;

    }

    public UserDTO() {
    }

    public UserDTO(User users) {
        this(users.getId(), users.getUsername(), users.getName(), users.getLastname(), users.getTelephone(), users.getPassword());
    }
}


