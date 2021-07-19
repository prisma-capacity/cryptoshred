Module *cryptoshred-spring-boot-autoconfigure*
==============================================

This maven module provides simple integration of the *cryptoshred* library into Spring Boot.


Configuration
-------------

The following configuration properties exists:

| **Property name**                | **Description** | **Required** | **Default Value** |
|----------------------------------|-----------------|---------------|----|
| `cryptoshred.cloud.aws.dynamo.tablename`| name of AWS DynamoDB table to store the subjectId/ key pairs. Only required when module [cryptoshred-cloud-aws](../cryptoshred-cloud-aws) is also used. | yes | - |
| `cryptoshred.initVector` | initialization vector for encryption/ decryption. Should be a random value. Only required for the default [JDKCryptoEngine](https://github.com/prisma-capacity/cryptoshred/blob/master/cryptoshred-core/src/main/java/eu/prismacapacity/cryptoshred/core/JDKCryptoEngine.java). Alternatively, you can provide your own [CryptoEngine](https://github.com/prisma-capacity/cryptoshred/blob/master/cryptoshred-core/src/main/java/eu/prismacapacity/cryptoshred/core/CryptoEngine.java)| yes (if JDKCryptoEngine is used) | - |
| `cryptoshred.defaults.algorithm` | crypto algorithm to be used. Currently only *AES* is supported | no | AES |
| `cryptoshred.defaults.keySize` | | no | 256 |
