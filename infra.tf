provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region     = "${var.region}"
}


resource "aws_iam_role_policy" "s3notifier_lambda_iam_role_policy" {
    name = "s3notifier_lambda_iam_role_policy"
    role = "${aws_iam_role.s3notifier_lambda_iam_role.id}"
    policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:*"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    },
     {
      "Effect": "Allow",
      "Action": [
        "s3:Get*",
        "s3:List*"
      ],
      "Resource": "arn:aws:s3:::*"
    }
  ]
}
EOF
}

resource "aws_iam_role" "s3notifier_lambda_iam_role" {
    name = "s3notifier_lambda_iam_role"
    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_lambda_function" "s3notifier" {
    filename = "target/s3notifier-function.jar"
    function_name = "s3notifier"
    role = "${aws_iam_role.s3notifier_lambda_iam_role.arn}"
    runtime = "java8"
    handler = "com.wtr.s3notifier.S3EventHandler"
    source_code_hash = "${base64sha256(file("target/s3notifier-function.jar"))}"
    environment {
        variables = {
            SMTP_HOST = "${var.smtp_host}"
            SMTP_PORT = "${var.smtp_port}"
            SMTP_USERNAME = "${var.smtp_username}"
            SMTP_PASSWORD = "${var.smtp_password}"
            EMAIL_FROM = "${var.email_from}"
            EMAIL_TO = "${var.email_to}"
            DROPBOX_ACCESS_TOKEN  ="${var.dropbox_access_token}"
        }
    }
    timeout = 20
    memory_size = 256
}