package com.vf.sb_criteria_pager.user;

import lombok.Data;

import java.util.Date;
@Data
public class UserResponseDTO {
    private Integer userId;
    private String name;
    private String lastName;
    private Double salary;
    private Float score;
    private Date birthDate;
}
