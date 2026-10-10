# Rule-Based Fraud Detection Website

A small Spring Boot and MySQL project with a plain HTML, CSS, and JavaScript dashboard. Create an account, check a transaction, see its decision and reasons, and review your saved transaction history.

## Run in Replit

The app starts a private, local MySQL server for the development preview, then starts Spring Boot. Use the app's Run button. Its database files stay outside the project source.

## Run on your computer

You need Java 17 or newer, Maven 3.8 or newer, and MySQL 8 or newer.

1. Create a MySQL database and a local app user:

   ```sh
   mysql -u root -p < database/create_database.sql
   ```

   Before running that command, change `choose-a-password` in `database/create_database.sql` to a password of your choice. The script creates the `fraud_detection` database and a user limited to `127.0.0.1`.

2. Set the database connection in your terminal. Keep the password out of source control:

   ```sh
   export MYSQL_URL='jdbc:mysql://127.0.0.1:3306/fraud_detection?serverTimezone=UTC'
   export MYSQL_USER='fraud_app'
   export MYSQL_PASSWORD='your-chosen-password'
   ```

3. From this folder, start the website:

   ```sh
   mvn spring-boot:run
   ```

4. Open `http://localhost:8080`.

Spring Boot creates the `app_users` and `transactions` tables the first time it connects. The connection settings can also be supplied as environment variables on Windows or in your IDE's run configuration.

## Risk rules

Each triggered rule adds points, and the total is capped at 100:

| Rule | Points |
| --- | ---: |
| Amount is $1,000–$4,999.99 | 15 |
| Amount is $5,000 or more | 35 |
| 5–9 payments in the last hour | 20 |
| 10 or more payments in the last hour | 35 |
| Payment is between 11 PM and 5 AM | 20 |
| Device is “Other” | 10 |
| Location contains “unknown” | 20 |
| Location contains “international” | 15 |

- **Approved:** score 0–29
- **Flagged:** score 30–59
- **Blocked:** score 60–100

This is a learning example, not a real payment authorization or production fraud system.
