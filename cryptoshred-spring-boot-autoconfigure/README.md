Module *cryptoshred-spring-boot-autoconfigure*
==============================================

This maven module provides simple integration of the *cryptoshred* library into Spring Boot.


Configuration
-------------

The following configuration properties exists:

| **Property name**                        | **Description**                                                                                                                                                                                                                                                                                                                                                                                                                                  | **Required** | **Default Value** |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|-------------------|
| `cryptoshred.cloud.aws.dynamo.tablename` | name of AWS DynamoDB table to store the subjectId/ key pairs. Only required when module [cryptoshred-cloud-aws](../cryptoshred-cloud-aws) is also used.                                                                                                                                                                                                                                                                                          | yes          | -                 |
| `cryptoshred.initVector`                 | Initialization vector for encryption/decryption if useRandomInitVector is false, otherwise use for decryption only if the data was encrypted with an older version of this library (no IV is stored with the encrypted data). Alternatively, you can provide your own [CryptoEngine](https://github.com/prisma-capacity/cryptoshred/blob/main/cryptoshred-core/src/main/java/eu/prismacapacity/cryptoshred/core/CryptoEngine.java) | no           | -                 |
| `cryptoshred.useRandomInitVector`        | Always use secure random initialization vector for encryption, regardless of the initVector property.                                                                                                                                                                                                                                                                                                                                            | no           | false             |
| `cryptoshred.defaults.algorithm`         | crypto algorithm to be used. Currently only *AES* is supported                                                                                                                                                                                                                                                                                                                                                                                   | no           | AES               |
| `cryptoshred.defaults.keySize`           |                                                                                                                                                                                                                                                                                                                                                                                                                                                  | no           | 256               |

Using random initVectors is a good practice, so for new projects setting useRandomInitVector=true and not providing an initVector is recommended.
In order to stay compatible with existing data, the static configured initVector is still used for decryption, if an initVector is not provided in the data. 

In a next major release, the library will default to the use of a random initVector. 
