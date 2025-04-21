provider "aws" {
  region = var.region
}

resource "aws_sqs_queue" "charger_connected" {
  name = "charger-connected-queue"
}

resource "aws_sqs_queue" "charger_disconnected" {
  name = "charger-disconnected-queue"
}

resource "aws_sqs_queue" "charger_telemetry" {
  name = "charger-telemetry-queue"
}

resource "aws_iam_role" "lambda_role" {
  name = "charger-service-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_policy" "lambda_sqs_access" {
  name = "charger-service-sqs-access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "sqs:SendMessage"
      ]
      Resource = [
        aws_sqs_queue.charger_connected.arn,
        aws_sqs_queue.charger_disconnected.arn,
        aws_sqs_queue.charger_telemetry.arn
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_sqs" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_sqs_access.arn
}

resource "aws_lambda_function" "charger_service_lambda" {
  function_name = "charger-service"

  role    = aws_iam_role.lambda_role.arn
  handler = "WebSocketRouterHandler::handleRequest"
  runtime = "java17"
  timeout = 10

  filename         = "${path.module}/lambda/charger-service.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/charger-service.zip")

  environment {
    variables = {
      CHARGER_CONNECTED_QUEUE_URL    = aws_sqs_queue.charger_connected.id
      CHARGER_DISCONNECTED_QUEUE_URL = aws_sqs_queue.charger_disconnected.id
      CHARGER_TELEMETRY_QUEUE_URL    = aws_sqs_queue.charger_telemetry.id
    }
  }
}

resource "aws_apigatewayv2_api" "websocket_api" {
  name                       = "charger-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_integration" "lambda_integration" {
  api_id                 = aws_apigatewayv2_api.websocket_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.charger_service_lambda.invoke_arn
  integration_method     = "POST"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "connect" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "\$connect"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

resource "aws_apigatewayv2_route" "disconnect" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "\$disconnect"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

resource "aws_apigatewayv2_route" "default" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "\$default"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

resource "aws_lambda_permission" "allow_apigw" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.charger_service_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.websocket_api.execution_arn}/*/*"
}
