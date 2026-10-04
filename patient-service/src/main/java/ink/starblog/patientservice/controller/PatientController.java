package ink.starblog.patientservice.controller;

import ink.starblog.patientservice.dto.PatientRequestDTO;
import ink.starblog.patientservice.dto.PatientResponseDTO;
import ink.starblog.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name="patient", description = "API for managing patients")
public class PatientController {
    private final PatientService patientService;


    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }


    @GetMapping
    @Operation(summary = "get the list of all patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients(){
        return ResponseEntity.ok().body(patientService.getPatients());
    }

    @PostMapping
    @Operation(summary = "Add a new patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO){
        return ResponseEntity.ok().body(patientService.createPatient(patientRequestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a patient by using the ID")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Valid @RequestBody PatientRequestDTO patientRequestDTO){
        return ResponseEntity.ok().body(patientService.updatePatient(id, patientRequestDTO));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient by using the ID")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
