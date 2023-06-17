package com.example.IBTim19.DTO;

import com.example.IBTim19.model.Role;

public class TokenDTO {
    private String token;

    private Integer id;
    private String role;

    public TokenDTO() {}

    public TokenDTO(String token, Integer id, String role) {
        this.token = token;
        this.id = id;
        this.role = role;
    }

    public TokenDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public TokenDTO(String token) {
        this.token = token;

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRole(Role role) {
        this.role = role.name();
    }

    public String getRole() {
        return role;
    }
}
