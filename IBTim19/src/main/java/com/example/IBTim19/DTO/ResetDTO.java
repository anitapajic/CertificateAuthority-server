package com.example.IBTim19.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetDTO {
    private Integer code;
    private String oldPassword;
    private String newPassword;
    private String newConfirmed;

}
