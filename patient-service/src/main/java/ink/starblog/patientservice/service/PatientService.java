package ink.starblog.patientservice.service;


import ink.starblog.patientservice.dto.PatientRequestDTO;
import ink.starblog.patientservice.dto.PatientResponseDTO;
import ink.starblog.patientservice.grpc.BillingServiceGrpcClient;
import ink.starblog.patientservice.mapper.PatientMapper;
import ink.starblog.patientservice.model.Patient;
import ink.starblog.patientservice.repository.PatientRepository;
import ink.starblog.patientservice.exception.PatientNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient){
        this.patientRepository=patientRepository;
        this.billingServiceGrpcClient=billingServiceGrpcClient;
    }


    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO)); //add the Entity model in the db
        log.info("Patient added with id {}", patient.getId());
        billingServiceGrpcClient.createBillingAccount(patient.getId().toString(), patient.getName(), patient.getEmail());
        log.info("Create Billing Account request sent for name {}",patient.getName());
        return PatientMapper.toDTO(patient); //response back with the responseDTO
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id)
                .orElseThrow(()-> new PatientNotFoundException("Patient not found"));

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(patientRequestDTO.getDateOfBirth());

        log.info("Patient updated with id {}", patient.getId());
        return PatientMapper.toDTO(patientRepository.save(patient));
    }

    public void  deletePatient(UUID id){
        Patient patient = patientRepository.findById(id).
                orElseThrow(()-> new PatientNotFoundException("Patient not found"));
        patientRepository.delete(patient);
    }


}
