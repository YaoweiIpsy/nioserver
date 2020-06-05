package com.ipsy.simpleNIOServer.controller;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.ipsy.simpleNIOServer.application.annotations.Controller;
import com.ipsy.simpleNIOServer.application.annotations.RequestMapping;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Future;

@Controller(uri = "/user_id/{userId}")
public class EntityController {
//
//  STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider =
//      new STSAssumeRoleSessionCredentialsProvider.Builder(
//          "arn:aws:iam::450096215204:role/ReadOnlyAccess",
//          UUID.randomUUID().toString())
//          .withStsClient(AWSSecurityTokenServiceClientBuilder.defaultClient())
//          .withRoleSessionDurationSeconds(3600)
//          .build();
  AmazonDynamoDBAsync client = AmazonDynamoDBAsyncClientBuilder.standard().build();

  @RequestMapping(uri = "/entity/{entityId}", method = "GET")
  public Future<GetItemResult> retrieve(String userId, String entityId) {
    return client.getItemAsync("segment-uas-props-main", new HashMap<String, AttributeValue>() {{
      put("userId", new AttributeValue(userId));
      put("name", new AttributeValue(entityId));
    }});
  }
}
