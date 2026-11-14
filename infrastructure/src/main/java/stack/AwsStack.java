package stack;

import com.amazonaws.services.mgn.model.Application;
import software.amazon.awscdk.*;

import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;

import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AwsStack extends Stack {
    //creating vpc
    private final Vpc vpc;
    private final Cluster ecsCluster;
    private CfnCluster mskCluster;
    private SecurityGroup sharedServiceSecurityGroup;

    public AwsStack(final App scope, final String id, final StackProps props){
        super(scope, id, props);

        this.vpc = createVpc();


        this.sharedServiceSecurityGroup = SecurityGroup.Builder.create(
                        this, "SharedServiceSecurityGroup")
                .vpc(vpc)
                .description("Shared security group for ECS services and MSK")
                .allowAllOutbound(true)
                .build();


        sharedServiceSecurityGroup.addIngressRule(
                Peer.securityGroupId(sharedServiceSecurityGroup.getSecurityGroupId()),
                Port.allTraffic(),
                "Allow communication between internal services"
        );

        //creating databases
        DatabaseInstance authServiceDb = createDatabase("AuthServiceDb","auth-service-db");
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDb","patient-service-db");

//        //creating  db health check
//        CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb,"AuthServiceDbHealthCheck");
//        CfnHealthCheck patientServiceDbHealthCheck = createDbHealthCheck(patientServiceDb,"PatientServiceDbHealthCheck");

        //creating kafka  cluster
        this.mskCluster =  creatMskCluster();

        //creating ecs cluseter for individual service of as ecs
        this.ecsCluster=createEcsCluster();

        FargateService authService = createFargateService("AuthService",
                "auth-service",
                List.of(4005),
                authServiceDb,
                Map.of("JWT_SECRET","d43557a0f5511ee94ec6c22200b34a7d452288947ba35fcafe8a29d470c2b857"));


        //analytics service need authServiceDb and it healt chek to be running prior to this
        //so add dependency of both on it
//        authService.getNode().addDependency(authDbHealthCheck);
        authService.getNode().addDependency(authServiceDb);


        FargateService billingService = createFargateService("BillingService",
                "billing-service",
                List.of(4001,9001),
                null,
                null);


        FargateService analyticsService = createFargateService("AnalyticsService",
                "analytics-service",
                List.of(4002),
                null,
                null);

        //analytics service need kafka to be running prior to this
        //so add dependency of kafka on it
        analyticsService.getNode().addDependency(mskCluster);

        FargateService patientService = createFargateService(
                "PatientService",
                "patient-service",
                List.of(4000),
                patientServiceDb,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "billing-service.patient-management.aws",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ));

        //patients service need authServiceDb and it healt chek to be running prior to this
        //so add dependency of both on it
        patientService.getNode().addDependency(patientServiceDb);
//        patientService.getNode().addDependency(patientServiceDbHealthCheck);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        //creating the api gateway
        createApiGatewayService();









    }

    private Vpc createVpc(){
        return Vpc.Builder
                .create(this, "PatientManagementVPC")
                .vpcName("patientmanagementVPC")
                .maxAzs(2)   //number of max availability zones
                .build();
    }

    private DatabaseInstance createDatabase(String id,String dbName  ){
        return DatabaseInstance.Builder.create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps
                                .builder().version(PostgresEngineVersion.VER_17_2)
                                .build()))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("AKSTAR"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id){
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster  creatMskCluster(){
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(2)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId).collect(Collectors.toList()))
                        .securityGroups(List.of(sharedServiceSecurityGroup.getSecurityGroupId()))
                        .brokerAzDistribution("DEFAULT").build())
                .build();
    }

    private Cluster createEcsCluster(){
        return Cluster.Builder.create(this, "PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder().name("patient-management.aws")
                        .build())
                .build();
    }

    private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance db, Map<String, String> additionalEnvVars){


        Role executionRole = Role.Builder.create(this, id + "ExecutionRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName(
                                "service-role/AmazonECSTaskExecutionRolePolicy"
                        )
                ))
                .build();
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .executionRole(executionRole)
                .build();




        IRepository repository = Repository.fromRepositoryName(
                this,
                id + "Repository",
                imageName
        );

        //ECR image pull permission
        repository.grantPull(taskDefinition.getExecutionRole());


        ContainerImage containerImage =
                ContainerImage.fromEcrRepository(repository, "latest");




        ContainerDefinitionOptions.Builder containerOptions = ContainerDefinitionOptions.builder()
                .image(containerImage)
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, id+"LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                        .streamPrefix(imageName)
                        .build()));

        String bootstrapServers = Fn.getAtt(
                mskCluster.getLogicalId(),
                "BootstrapBrokerStringTls"
        ).toString();

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", bootstrapServers);

        if(additionalEnvVars != null){
            envVars.putAll(additionalEnvVars);
        }
        if(db!=null){
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(), db.getDbInstanceEndpointPort(), imageName));

            envVars.put("SPRING_DATASOURCE_USERNAME","AKSTAR");
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_JPA_HIBERNATE_SHOW_SQL", "true");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");

        }

        containerOptions.environment(envVars);
        taskDefinition.addContainer(imageName+"Container", containerOptions.build());


        FargateService service = FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .securityGroups(List.of(sharedServiceSecurityGroup))
                .cloudMapOptions(CloudMapOptions.builder()
                        .name(imageName)
                        .build())
                .build();

        if (db != null) {
            db.getConnections().allowDefaultPortFrom(service);
        }

        return service;
    }

    private void createApiGatewayService(){

        Role executionRole = Role.Builder.create(this, "ApiGatewayExecutionRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName(
                                "service-role/AmazonECSTaskExecutionRolePolicy"
                        )
                ))
                .build();

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, "APIGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .executionRole(executionRole)
                .build();

        IRepository repository = Repository.fromRepositoryName(
                this,
                "ApiGatewayRepository",
                "api-gateway"
        );

        repository.grantPull(taskDefinition.getExecutionRole());

        ContainerImage containerImage =
                ContainerImage.fromEcrRepository(repository, "latest");
        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                .image(containerImage)
                .environment(Map.of(
                "SPRING_PROFILES_ACTIVE", "prod",
                "AUTH_SERVICE_URL", "http://auth-service.patient-management.aws:4005"
            ))
                .portMappings(List.of(4004).stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();


        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService apiGateway = ApplicationLoadBalancedFargateService.Builder
                .create(this, "APIGAtewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .securityGroups(List.of(sharedServiceSecurityGroup))
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }

    public static void main(final String a[]){
        App app = new App(AppProps.builder().outdir("./cdk.out").build());
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();


        new AwsStack(app, "awsStack", props);
        app.synth();
        System.out.println("App synthesis in progress...");
    }

}
