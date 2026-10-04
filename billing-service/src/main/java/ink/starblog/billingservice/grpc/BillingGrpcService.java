package ink.starblog.billingservice.grpc;

import billing.BillingResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;

@Slf4j
@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseStreamObserver){
        //the stream observer let us accept back and forth communication in real time
        log.info("createBillingAccount request received {}",billingRequest.toString());

        //Business logic
        BillingResponse billingResponse =  BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("Active")
                .build();

        responseStreamObserver.onNext(billingResponse);
        //used the sind the response from gRPC service
        responseStreamObserver.onCompleted();
        //this line is to say the response is completed, as in gRPC we can have multiple responses
    }
}
