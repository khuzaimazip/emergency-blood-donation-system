# Emergency Blood Donation System

A Java Swing desktop application for managing emergency blood donation operations — donor registration, real-time blood stock tracking, emergency request creation and fulfilment, distribution history, and an admin dashboard — backed by a MySQL database.

## Features

- **Donor Registration** — register donors with blood group, contact info, and optional immediate donation (auto-adds to stock)
- **Blood Stock Management** — track units available per blood group with a live animated stock graph
- **Emergency Requests** — create requests with priority levels (Critical / High / Normal)
- **Emergency Fulfilment** — match requests to available stock and distribute units
- **Distribution History** — full transaction log (stock in, distribution, admin actions)
- **Reports** — area-wise pending need and hospital-wise distribution summaries
- **Multi-user Auth** — each user gets their own database on registration; passwords are hashed with PBKDF2 (salted)
- **Admin Panel** — manage requests, donors, stock, high-priority areas, and system announcements

## Tech Stack

- Java (Swing / AWT for UI)
- MySQL (via JDBC — `mysql-connector-j`)
- PBKDF2WithHmacSHA256 for password hashing

## Prerequisites

- JDK 8 or later
- MySQL Server running locally (default: `localhost:3306`)
- MySQL JDBC Driver (`mysql-connector-j`) on the classpath

## Setup

1. Install and start MySQL Server locally.
2. Make sure a MySQL user with permission to create databases is available. By default the app connects as:
   - **User:** `root`
   - **Password:** *(empty)*

   > ⚠️ These are placeholder local-dev credentials in the source code (`MYSQL_USER`, `MYSQL_PASSWORD` constants). **Change them** before running against any real/shared database, and never commit real credentials.

3. The app automatically creates its own databases and tables on first run — no manual SQL setup needed.
4. Compile and run:

   ```bash
   javac -cp .:mysql-connector-j-x.x.x.jar EmergencyBloodDonation/EmergencyBloodDonation.java
   java -cp .:mysql-connector-j-x.x.x.jar EmergencyBloodDonation.EmergencyBloodDonation
   ```

   (On Windows, replace `:` with `;` in the classpath.)

5. On first launch, use **"CREATE ACCOUNT"** to register a new user, then log in.

   > Note: if a legacy `bloodbank` database already exists locally, the app will auto-register a default admin account (`Khuzaima` / `Khuzaima@123`) pointing to it. This is a migration convenience for the original developer's local setup — **not intended as a default login for other users.**

## Project Structure

```
EmergencyBloodDonation/
└── EmergencyBloodDonation.java   # Full application: UI, auth, DB layer, models
```

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
