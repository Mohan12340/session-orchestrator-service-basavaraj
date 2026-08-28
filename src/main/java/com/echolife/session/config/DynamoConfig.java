package com.echolife.session.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoConfig {
    private static final Logger log = LoggerFactory.getLogger(DynamoConfig.class);

    @Bean
    DynamoDbClient dynamo(@Value("${echolife.dynamodb.endpoint:}") String endpoint, @Value("${AWS_REGION:ap-south-1}") String region) {
        var builder = DynamoDbClient.builder().region(software.amazon.awssdk.regions.Region.of(region));
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                    System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "local"),
                    System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "local"))));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    @Bean
    DynamoDbEnhancedClient enhanced(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Bean
    DynamoDbTable<com.echolife.session.model.SessionRecord> sessionTable(
            DynamoDbEnhancedClient db,
            DynamoDbClient client,
            @Value("${echolife.dynamodb.table-name:echolife-sessions}") String tableName,
            @Value("${echolife.dynamodb.auto-create-table:false}") boolean autoCreate) {
        var table = db.table(tableName, TableSchema.fromBean(com.echolife.session.model.SessionRecord.class));
        if (autoCreate) {
            try {
                table.createTable();
                client.waiter().waitUntilTableExists(r -> r.tableName(tableName));
                log.info("Created DynamoDB table {}", tableName);
            } catch (software.amazon.awssdk.services.dynamodb.model.ResourceInUseException ignored) {
                log.info("DynamoDB table {} already exists", tableName);
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to create DynamoDB table " + tableName, ex);
            }
        }
        return table;
    }
}
