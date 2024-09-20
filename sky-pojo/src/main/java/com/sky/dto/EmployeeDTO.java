package com.sky.dto;

import com.sky.entity.Employee;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO extends Employee implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;



}
