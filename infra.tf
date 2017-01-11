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
}