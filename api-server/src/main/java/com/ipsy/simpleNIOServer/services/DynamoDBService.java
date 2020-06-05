package com.ipsy.simpleNIOServer.services;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import io.reactivex.rxjava3.core.Single;
import java.util.HashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class DynamoDBService {

  public static void main(String[] argv) throws Exception {
    STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider =
        new STSAssumeRoleSessionCredentialsProvider.Builder(
            "arn:aws:iam::450096215204:role/ReadOnlyAccess",
            UUID.randomUUID().toString())
            .withStsClient(AWSSecurityTokenServiceClientBuilder.defaultClient())
            .withRoleSessionDurationSeconds(3600)
            .build();
    AmazonDynamoDBAsync client = AmazonDynamoDBAsyncClientBuilder.standard()
        .withCredentials(stsAssumeRoleSessionCredentialsProvider).build();
    val result = client
        .getItemAsync("segment-uas-props-main", new HashMap<String, AttributeValue>() {{
          put("userId", new AttributeValue("u-k7cjysozfb1x129f"));
          put("name", new AttributeValue("membership_original"));
        }});
    log.info("{}", Single.fromFuture(result).blockingGet());
    //    result.
//    log.info("{}", );
  }
}
