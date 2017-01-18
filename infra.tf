provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region     = "${var.region}"
}

resource "aws_sns_topic" "S3NotifierTopic" {
  name = "S3NotifierTopic"
}

resource "aws_sns_topic_policy" "s3notifier_topic_policy" {
    arn = "${aws_sns_topic.S3NotifierTopic.arn}"
    policy = <<EOF
{
  "Version": "2012-10-17",
  "Id": "s3notifier-topic-policy",
  "Statement": [
    {
      "Sid": "AllowS3ToPublishToTopic",
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": "SNS:Publish",
      "Resource": "arn:aws:sns:eu-west-1:609701658665:S3NotifierTopic",
      "Condition": {
        "ArnLike": {
          "aws:SourceArn": "arn:aws:s3:*:*:*"
        }
      }
    },
    {
      "Sid": "AllowOwnerFullRightsOnTopic",
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": [
        "SNS:Publish",
        "SNS:RemovePermission",
        "SNS:SetTopicAttributes",
        "SNS:DeleteTopic",
        "SNS:ListSubscriptionsByTopic",
        "SNS:GetTopicAttributes",
        "SNS:Receive",
        "SNS:AddPermission",
        "SNS:Subscribe"
      ],
      "Resource": "arn:aws:sns:eu-west-1:609701658665:S3NotifierTopic",
      "Condition": {
        "StringEquals": {
          "AWS:SourceOwner": "609701658665"
        }
      }
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

resource "aws_iam_role_policy" "s3notifier_lambda_iam_role_policy" {
    name = "s3notifier_lambda_iam_role_policy"
    role = "${aws_iam_role.s3notifier_lambda_iam_role.id}"
    policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
     {
        "Sid": "AllowFunctionToWriteCloudWatchLogs",
        "Effect": "Allow",
        "Action": [
            "logs:*"
        ],
        "Resource": "arn:aws:logs:*:*:*"
    },
    {
        "Sid": "AllowFunctionToManageS3Objects",
        "Effect": "Allow",
        "Action": [
            "s3:Get*",
            "s3:List*",
            "s3:PutObject*",
            "s3:DeleteObject*"
        ],
        "Resource": "arn:aws:s3:::*"
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
            DROPBOX_PARENT_FOLDER ="${var.dropbox_parent_folder}"
        }
    }	
    timeout = 20
    memory_size = 256
}

resource "aws_sns_topic_subscription" "S3NotifierTopic-s3notifier" {
    topic_arn = "${aws_sns_topic.S3NotifierTopic.arn}"
    protocol = "lambda"
    endpoint = "${aws_lambda_function.s3notifier.arn}"
}

resource "aws_lambda_function" "s3schedulednotifier" {
    filename = "target/s3notifier-function.jar"
    function_name = "s3schedulednotifier"
    role = "${aws_iam_role.s3notifier_lambda_iam_role.arn}"
    runtime = "java8"
    handler = "com.wtr.s3notifier.ScheduleHandler"
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
            DROPBOX_PARENT_FOLDER ="${var.dropbox_parent_folder}"
        }
    }
    timeout = 20
    memory_size = 256
}

resource "aws_cloudwatch_event_target" "s3schedulednotifier" {
  target_id = "Yada"
  rule = "${aws_cloudwatch_event_rule.s3schedulednotifier.name}"
  arn = "${aws_lambda_function.s3schedulednotifier.arn}"
}

resource "aws_cloudwatch_event_rule" "s3schedulednotifier" {
  name = "s3schedulednotifier_dailyevent"
  schedule_expression = "cron(35 21 1/1 * ? *)"
}
