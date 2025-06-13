package com.codefathers.model.enums;

public enum EmployeeRole {
    LOCAL_MANAGER,
    SAC,
    HR,
    FINANCIAL,
    SALES,
    STORAGE,
    SHIPPING;

    public String getLabel() {
        switch (this) {
            case LOCAL_MANAGER:
                return "Local Manager";
            case SAC:
                return "Customer Service";
            case HR:
                return "Human Resources";
            case FINANCIAL:
                return "Finalcial";
            case SALES:
                return "Seller";
            case STORAGE:
                return "Storage";
            case SHIPPING:
                return "Shipping";
            default:
                return this.name(); // Caso padrão
        }
    }

}
