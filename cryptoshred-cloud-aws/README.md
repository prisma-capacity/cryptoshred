# cryptoshred-cloud-aws

This is an AWS DynamoDB backed implementation of a `CryptoKeyRepository`. 

You will need at least the following shared infrastructure in order to be able to use `cryptoshred`. 
```
---
AWSTemplateFormatVersion: '2010-09-09'
Description: Cryptoshred DynamoDB Table

Resources:
  CryptoshredKeyTable:
    Type: AWS::DynamoDB::Table
    Properties:
      TableName: cryptoshred-keys
      BillingMode: PAY_PER_REQUEST
      KeySchema:
        - AttributeName: subjectId
          KeyType: HASH
      AttributeDefinitions:
        - AttributeName: subjectId
          AttributeType: S
      PointInTimeRecoverySpecification:
        PointInTimeRecoveryEnabled: true
        
  CryptoShredUserPolicy:
    Type: AWS::IAM::ManagedPolicy
    Properties:
      ManagedPolicyName: cryptoshred-use-policy
      Description: Allows services to use the cryptoshred library
      PolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Action:
              - dynamodb:UpdateItem
              - dynamodb:GetItem
            Resource:
              Fn::Join:
                - ':'
                - - 'arn:aws:dynamodb'
                  - !Ref AWS::Region
                  - !Ref AWS::AccountId
                  - 'table/cryptoshred-keys'        

```
**Note**: The managed policy `cryptoshred-use-policy` is designed to be used by all users of the library.

### Configuration Property
- `cryptoshred.cloud.aws.dynamo.tablename` 
    - name of AWS DynamoDB table to store the subjectId/ key pairs
    - required parameter


### Retry

In case you're wondering, cryptoshred does not alter the defaults for retry
behavior. That means, if you did not configure it differently, it'll come
with the SDKs defaults of: 100 retries and 500ms base delay / max backOff
20s

See com.amazonaws.retry.PredefinedRetryPolicies#getDynamoDBDefaultRetryPolicy

