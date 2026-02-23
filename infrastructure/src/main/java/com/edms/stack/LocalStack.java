package com.edms.stack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.CloudMapNamespaceOptions;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.FargateTaskDefinition;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

public class LocalStack extends Stack {
    final private Vpc vpc;
    final private Cluster ecsCluster;

    public LocalStack(App scope, String id, StackProps props) {
        super(scope, id, props);

        // Create the global VPC
        this.vpc = createVpc();

        // Create database instances
        DatabaseInstance authServiceDatabase = createDatabase("AuthServiceDatabaseId", "auth-service-db");
        DatabaseInstance customerServiceDatabase = createDatabase("CustomerServiceDatabaseId", "customer-service-db");

        // Create Databases Health Checker
        CfnHealthCheck authDbHealthChecker = createDatabaseHealthCheck("AuthServiceDatabaseHealthCheck", authServiceDatabase);
        CfnHealthCheck customerDbHealthChecker = createDatabaseHealthCheck("CustomerServiceDatabaseHealthCheck", customerServiceDatabase);

        // Create Kafka MSK Cluster
        CfnCluster mskCluster = createMskCluster();

        // Create the ECS Cluster
        this.ecsCluster = createEcsCluster();

        // Create the Services
        FargateService authService = createFargateService(
                "AuthService",
                "auth-service",
                List.of(5006),
                authServiceDatabase,
                Map.of("JWT_SECRET", "6657a823e9b04677bb0277a8e3699155d4989ae210c1fbb82dac764421c18f6b")
        );

        authService.getNode().addDependency(authDbHealthChecker);
        authService.getNode().addDependency(authServiceDatabase);

        FargateService billingService = createFargateService(
                "BillingService",
                "billing-service",
                List.of(5002, 9002),
                null,
                null
        );

        FargateService analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-service",
                List.of(5003),
                null,
                null
        );

        analyticsService.getNode().addDependency(mskCluster);

        FargateService customerService = createFargateService(
                "CustomerService",
                "customer-service",
                List.of(5000),
                customerServiceDatabase,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9002"
                )
        );

        customerService.getNode().addDependency(customerServiceDatabase);
        customerService.getNode().addDependency(customerDbHealthChecker);
        customerService.getNode().addDependency(billingService);
        customerService.getNode().addDependency(mskCluster);

        // Create API Gateway with LoadBalancer
        createApiGatewayService();
    }

    private Vpc createVpc() {
        return Vpc.Builder
                .create(this, "CustomerManagementVPC")
                .vpcName("CustomerManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String databaseName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()
                ))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(15)
                .credentials(Credentials.fromGeneratedSecret("admin"))
                .databaseName(databaseName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createDatabaseHealthCheck(String id, DatabaseInstance database) {
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(
                        CfnHealthCheck.HealthCheckConfigProperty.builder()
                                .type("TCP")
                                .ipAddress(database.getDbInstanceEndpointAddress())
                                .port(Token.asNumber(database.getDbInstanceEndpointPort()))
                                .requestInterval(45)
                                .failureThreshold(3)
                                .build())
                .build();
    }


    private CfnCluster createMskCluster() {
        return CfnCluster.Builder
                .create(this, "MSKCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("3.7.x")
                .numberOfBrokerNodes(2)
                .brokerNodeGroupInfo(
                        CfnCluster.BrokerNodeGroupInfoProperty.builder()
                                .instanceType("kafka.m5.large")
                                .clientSubnets(vpc.getPrivateSubnets().stream()
                                        .map(ISubnet::getSubnetId)
                                        .collect(Collectors.toList()))
                                .brokerAzDistribution("DEFAULT")
                                .build()
                )
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder
                .create(this, "CustomerManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(
                        CloudMapNamespaceOptions.builder()
                                .name("customer-management.local")
                                .build()
                )
                .build();
    }

    private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance database, Map<String, String> additionalEnvVars) {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, id + "-Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions.Builder containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(
                        ports.stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList()
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder
                                .create(this, id + "-LogGroup")
                                .logGroupName("/ecs" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build()
                        )
                        .streamPrefix(imageName)
                        .build()));

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        if (database != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    database.getDbInstanceEndpointAddress(),
                    database.getDbInstanceEndpointPort(),
                    imageName
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin");
            envVars.put("SPRING_DATASOURCE_PASSWORD",
                    database.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "70000");
        }

        containerDefinitionOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "-Container", containerDefinitionOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    private void createApiGatewayService() {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, "APIGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("api-gateway"))
                        .environment(Map.of(
                                "SPRING_PROFILES_ACTIVE", "prod",
                                "AUTH_SERVICE_URL", "http://host.docker.internal:5006"
                        ))
                        .portMappings(List.of(5004).stream()
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
                .create(this, "APIGatewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.minutes(1))
                .build();
    }

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "localstack", props);
        app.synth();
        System.out.println("App synthesising in progress...");
    }
}
