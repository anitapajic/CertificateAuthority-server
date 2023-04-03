package com.example.IBTim19.DTO;

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

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
