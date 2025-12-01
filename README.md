BFHL Webhook Automation — Spring Boot Project

This Spring Boot application is built as part of the BFHL Hiring Assignment.
The application runs automatically on startup (no controllers or endpoints) and performs the required API flow:

 Project Overview

This application:

 1. Sends a POST request on startup

To generate a webhook endpoint and JWT access token using:

POST https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA


With body:

{
  "name": "John Doe",
  "regNo": "REG12347",
  "email": "john@example.com"
}

 2. Receives:

webhook → URL where the final answer should be submitted

accessToken → JWT token for Authorization header

 3. Determines which SQL question is assigned

Based on last two digits of regNo:

Condition	Assigned Question
Odd last two digits	Question 1
Even last two digits	Question 2
 The corresponding SQL query must be placed in:
src/main/resources/final-queries/q1.sql   ← for odd
src/main/resources/final-queries/q2.sql   ← for even

 4. SUBMITS the final SQL query

To the webhook URL provided in step 1:

POST <webhook-url>
Authorization: <accessToken>
Content-Type: application/json


With body:

{
  "finalQuery": "THE_FINAL_SQL_QUERY"
}

*()* Tech Stack

Java 17

Spring Boot

Spring Web

RestTemplate

Maven

PDFBox (for optional question text extraction)

GitHub Actions (for automated build)

 Project Structure
bfhl-webhook/
 ├── pom.xml
 ├── README.md
 └── src/
      └── main/
           ├── java/com/example/bfhl/
           │     ├── BfhlWebhookApplication.java
           │     ├── config/RestTemplateConfig.java
           │     ├── service/WebhookRunnerService.java
           │     ├── model/GenerateWebhookRequest.java
           │     ├── model/GenerateWebhookResponse.java
           │     └── util/PdfDownloader.java
           └── resources/
                 ├── application.yml
                 └── final-queries/
                       ├── q1.sql
                       └── q2.sql

⚙️ How the Application Works

When the JAR is executed:

Spring Boot starts

WebhookRunnerService (an ApplicationRunner) triggers automatically

A webhook is generated via API call

The assigned SQL question is detected

The answer is loaded from the appropriate .sql file

The final SQL is POSTed to the webhook using the JWT token

The application exits

No controller or REST endpoints are exposed.

() How to Build the Project

Use Maven:

mvn clean package


This will produce:

target/bfhl-webhook-1.0.0.jar

() How to Run the JAR
java -jar target/bfhl-webhook-1.0.0.jar


The application will automatically execute the entire flow.

() GitHub Actions Build

A GitHub Actions workflow (maven.yml) is included to:

Automatically build the project on every push

Produce the JAR file as a downloadable artifact

This provides a clean, consistent, environment-independent build.
