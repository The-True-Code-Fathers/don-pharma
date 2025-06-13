package com.codefathers.model.dto;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UpdateEmployeeDTO {

    @NotBlank(message = "O nome completo é obrigatório.")
    private String fullName;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento tem que estar no passado.")
    private LocalDate birthDate;

    @NotNull(message = "O gênero é obrigatório.")
    private EmployeeGender gender;

    @NotNull(message = "O cargo é obrigatório")
    private EmployeeRole role;

    @NotNull
    private boolean active;
}
