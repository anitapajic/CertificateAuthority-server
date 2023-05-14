package com.example.IBTim19.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetDTO {
    private String username;
    private String telephone;
    private ResetType type;

    private Integer code;
    private String newPassword;
    private String newConfirmed;

}
