package ink.starblog.patientservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class PatientRequestDTO {

    @NotBlank
    @Size(max = 100, message = "name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "email cannot be blank")
    @Email(message = "email is not valid")
    private String email;

    @NotBlank(groups= CreatePatientValidationGroup.class, message = "Address is required")
    private String address;

    @NotNull(message = "DOB is mandatory")
    private LocalDate dateOfBirth;

}
