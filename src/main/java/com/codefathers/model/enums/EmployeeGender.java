package com.codefathers.model.enums;

public enum EmployeeGender {
    MALE,
    FEMALE,
    NON_BINARY,
    UNDECLARED;

    public String getLabel() {
        switch (this) {
            case MALE:
                return "Male";
            case FEMALE:
                return "Female";
            case NON_BINARY:
                return "Non Binary";
            case UNDECLARED:
                return "Undeclared";
            default:
                return this.name(); // Caso padrão
        }
    }
}
