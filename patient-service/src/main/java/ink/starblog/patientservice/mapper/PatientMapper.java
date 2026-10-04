package ink.starblog.patientservice.mapper;

import ink.starblog.patientservice.dto.PatientRequestDTO;
import ink.starblog.patientservice.dto.PatientResponseDTO;
import ink.starblog.patientservice.model.Patient;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO responseDTO = new PatientResponseDTO();
        responseDTO.setId(patient.getId().toString());
        responseDTO.setName(patient.getName());
        responseDTO.setEmail(patient.getEmail());
        responseDTO.setAddress(patient.getAddress());
        responseDTO.setDateOfBirth(patient.getDateOfBirth().toString());

        return responseDTO;
    }

    public static Patient toPatient(PatientRequestDTO  patientRequestDTO){
        Patient patient = new Patient();

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(patientRequestDTO.getDateOfBirth());

        return patient;

    }

}
