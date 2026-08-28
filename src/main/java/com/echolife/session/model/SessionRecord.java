package com.echolife.session.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.time.Instant;

@DynamoDbBean
public class SessionRecord {
    private String sessionId,userId,personaId,mode,inputChannel,outputChannel,clientType,status;
    private Instant createdAt;
    private Long expiresAtEpoch;
    private Integer policyVersion;

    @DynamoDbPartitionKey
    public String getSessionId(){return sessionId;} public void setSessionId(String v){sessionId=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getPersonaId(){return personaId;} public void setPersonaId(String v){personaId=v;}
    public String getMode(){return mode;} public void setMode(String v){mode=v;}
    public String getInputChannel(){return inputChannel;} public void setInputChannel(String v){inputChannel=v;}
    public String getOutputChannel(){return outputChannel;} public void setOutputChannel(String v){outputChannel=v;}
    public String getClientType(){return clientType;} public void setClientType(String v){clientType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Long getExpiresAtEpoch(){return expiresAtEpoch;} public void setExpiresAtEpoch(Long v){expiresAtEpoch=v;}
    public Integer getPolicyVersion(){return policyVersion;} public void setPolicyVersion(Integer v){policyVersion=v;}
}
