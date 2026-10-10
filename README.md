# Rule-Based Fraud Detection Website

## About the Project

The Rule-Based Fraud Detection Website is a Java web application designed to identify potentially fraudulent transactions using predefined rules. It analyzes transaction details, calculates a risk score, and displays a result to help users identify suspicious activity.

## Features

- **User Registration and Login:** Allows users to access the application.
- **Transaction Analysis:** Enter transaction details such as amount, location, device, time, and transaction frequency.
- **Fraud Risk Scoring:** Calculates a risk score using predefined fraud detection rules.
- **Transaction Status:** Classifies transactions as Approved, Flagged, or Blocked based on their risk score.
- **Transaction History:** Stores and displays previous transactions.
- **Dashboard:** Provides a simple interface to use the application's features.

## Technologies Used

- **Backend:** Java, Spring Boot
- **Frontend:** HTML, CSS, JavaScript
- **Database:** MySQL
- **Build Tool:** Maven

## How It Works

1. The user enters transaction details.
2. The application checks the details against predefined fraud detection rules.
3. A risk score is calculated based on the rules triggered.
4. The transaction is classified as Approved, Flagged, or Blocked.
5. The transaction result is stored in the database for future reference.

## Fraud Detection Rules

The application can use rules such as:

- High transaction amount
- Unusually frequent transactions
- Suspicious location changes
- Unrecognized devices
- Unusual transaction timings

The final decision depends on the risk-scoring logic implemented in the application.

## Installation and Setup

### Prerequisites

- Java Development Kit (JDK)
- Maven
- MySQL Server

### Steps

1. Download or clone the project source code.
2. Create a MySQL database for the application.
3. Configure the database connection settings in the application's configuration file.
4. Install the required dependencies.
5. Run the application using your IDE or Maven.

For a Maven-based Spring Boot project, use:

```bash
mvn spring-boot:run
```

6. Open the local URL shown in the application console in your web browser.

**Note:** The exact database configuration and startup commands may vary depending on the project setup.

## Project Objective

The objective of this project is to demonstrate how a rule-based fraud detection system can analyze transaction data, assign risk scores, and identify suspicious transactions through a simple web interface.

## Future Improvements

- Email or in-app fraud alerts
- More configurable fraud detection rules
- Advanced transaction analytics
- Improved dashboard and reporting features
- Additional security enhancements

## Disclaimer

This project is intended for educational purposes. Its rule-based decisions are demonstrations and should not be treated as a complete, production-ready financial fraud prevention system.
