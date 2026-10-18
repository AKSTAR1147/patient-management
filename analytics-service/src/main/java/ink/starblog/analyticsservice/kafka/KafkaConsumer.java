package ink.starblog.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Slf4j
@Service
public class KafkaConsumer {

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consuemEvent(byte[] event){
        try {
            PatientEvent patientEvent =  PatientEvent.parseFrom(event);
            //add any business logic here as per the requirement
            log.info("patient received for analytics {}",event);
        } catch (InvalidProtocolBufferException e) {
            log.error("error deserializing event {}", e.getMessage());
        }

    }
}
