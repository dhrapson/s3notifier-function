# s3notifier-function

An AWS Lambda function written in Java, that receives an S3 event notification and processes the file.
At first versions this looks like sending the file to dropbox for manual processing and notifying the person responsible via email.

## Development

The code can be developed and tested from the desktop without deployment to AWS

### Prerequisites

* Maven 3
* Java 8
* AWS SES service must be initiated and the SMTP credentials retrieved
* Domain must be setup in AWS SES. If SES has not yet verified you domain, both the EMAIL_FROM and EMAIL_TO addresses must be verified 
* A dropbox app must have been created, with a valid access token
* An AWS account with credentials
* An S3 bucket into which the account has access



## Running Unit Tests

```
mvn clean test
```

### Running Integration Tests

First store your AWS credentials in the standard .aws/credentials file or as the standard environment variables: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`.
Then run

```
SMTP_USERNAME='insert_aws_ses_smtp_username' \
SMTP_PASSWORD='insert_aws_ses_smtp_password' \
EMAIL_FROM='insert_email_address' \
EMAIL_TO='insert_email_address' \
DROPBOX_ACCESS_TOKEN='insert_dropbox_access_token' \
DROPBOX_PARENT_FOLDER='/test' \
mvn clean verify
```

## Deployment to AWS

### Prerequisites

* Maven 3
* Java 8
* Install Terraform

### Deployment Commands

First store your AWS credentials in the standard .aws/credentials file or as the standard environment variables: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
Then run

```
mvn package && \
TF_VAR_access_key='your_aws_access_key' \
TF_VAR_secret_key='your_aws_secret_key' \
TF_VAR_smtp_username='your_aws_smtp_username' \
TF_VAR_smtp_password='your_aws_smtp_password' \
TF_VAR_email_from='insert_email_address' \
TF_VAR_email_to='insert_email_address' \
TF_VAR_dropbox_access_token='your_dropbox_access_token'\
 TF_VAR_dropbox_parent_folder="/the top-level dir" \
 terraform apply
 ```
 
