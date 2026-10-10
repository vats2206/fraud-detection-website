# Rule-Based Fraud Detection

A beginner-friendly transaction review website. The dashboard uses plain HTML, CSS, and JavaScript; a Spring Boot API scores transactions and stores user accounts and transaction history in MySQL.

## Run & Operate

- Run the `artifacts/fraud-detection: web` workflow for the Replit preview. It starts a local development MySQL instance and the Spring Boot app.
- For a local computer setup, follow `artifacts/fraud-detection/README.md`.
- `pnpm --filter @workspace/fraud-detection run build` — package the Java application.
- `pnpm --filter @workspace/fraud-detection run typecheck` — run the Java Maven checks.

## Stack

- Java 17+, Spring Boot, Spring Data JPA, and MySQL
- Plain HTML, CSS, and vanilla JavaScript
- Maven for Java dependencies and builds

## Product scope

- Register and sign in to a private workspace.
- Enter transaction details and receive an Approved, Flagged, or Blocked decision with an explainable score.
- Store and show each user's own transaction history and dashboard totals.
- Keep this project focused on the working website. Do not add team details, presentation materials, or unrelated features.
