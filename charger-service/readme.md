**The Charger Service:**

-   Manages charger connections via AWS API Gateway WebSockets

-   Maintains a Map<charger_id, connection>

-   Sends and queues OCPP commands (e.g., StartTransaction, StopTransaction) with:

-   ACK/timeout retry logic

-   Per-charger command queues

-   Uses RDS for relational data (e.g., users, permissions)

-   Supports OCPP 1.6 JSON (OCPP 2.0.1 is planned)

-   Enforces load balancing per board with hard current limits

-   Allows real-time telemetry tracking (fully persisted with TTL for audit)

-   Processes incoming WebSocket events through AWS API Gateway + Lambda + Kafka

#### DB
-   RDS (PostgreSQL) for relational data
- Apply the migrations:
```
./gradlew flywayMigrate
```
- Generate jOOQ code
```
./gradlew generateJooq
```
#### steps 
- Run ```docker-compose up -d``` to launch PostgreSQL
- Run ```./gradlew :db-charger:flywayMigrate :db-charger:generateJooq```
- Use generated types from com.paragon.generated.jooq in your project
