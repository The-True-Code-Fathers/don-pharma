package com.codefathers.view;

import com.codefathers.service.EmployeeService;

public class EmployeeView {

    private final EmployeeService employeeService;

    public EmployeeView(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public static void main(String[] args) {
        System.out.println("Cadastro de associados!");
    }

}
