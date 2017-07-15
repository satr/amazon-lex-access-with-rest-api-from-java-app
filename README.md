# amazon-lex-access-with-rest-api-from-java-app
Example of the connection to the Amazon Lex bot with REST API from Java application

Video:
https://youtu.be/EAxiratt5_k

Amazon Lex Documentation:
https://aws.amazon.com/documentation/lex/

Documentation for AWS SDK for Java:
http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html

IAM AWS Console: users:
https://console.aws.amazon.com/iam/home?region=us-east-1#/users

Required permissions for the user in IAM
```
AmazonLexReadOnly
AmazonLexRunBotsOnly
```

Lex AWS Console:
https://console.aws.amazon.com/lex/home?region=us-east-1#

Java web-client and json libraries
```
cxf-bundle-jaxrs
jackson-jaxrs-json-provider
```

Authentication and Access Control for Amazon Lex:
http://docs.aws.amazon.com/lex/latest/dg/auth-and-access-control.html

Signature Version 4 Signing Process:
http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html

Signing AWS Requests with Signature Version 4:
http://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html#sigv4_signing-steps-overview

Examples of the Complete Version 4 Signing Process of accessing DynamoDb (Python):
http://docs.aws.amazon.com/general/latest/gr/sigv4-signed-request-examples.html
Example of the Complete Version 4 Signing Process of accessing Lex (Python):
https://github.com/satr/amazon-lex-access-with-rest-api-from-java-app/blob/master/docs/amazon-lex-access-with-rest-api.py

Examples of How to Derive a Signing Key for Signature Version 4:
http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html

Best Practices for Managing AWS Access Keys (not recommended to keep them in the code):
http://docs.aws.amazon.com/general/latest/gr/aws-access-keys-best-practices.html

Troubleshooting AWS Signature Version 4 Errors:
http://docs.aws.amazon.com/general/latest/gr/signature-v4-troubleshooting.html

Amazon Lex API Reference (endpoints):
http://docs.aws.amazon.com/lex/latest/dg/API_Reference.html


