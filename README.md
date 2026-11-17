# Metric Laundry Management System (Java Swing + MySQL)

A desktop laundry system with three roles: **Admin, Staff and Customer**. The design follows the Metric Laundry mockup. See `docs/ANALYSIS.md` for the full system analysis and `docs/screenshots/` for every screen.

## Requirements
* JDK 17 or newer
* MySQL 8 or MariaDB (XAMPP works)
* NetBeans, IntelliJ IDEA or Eclipse (this is a Maven project)

## Setup (5 minutes)
1. **Database:** start MySQL in XAMPP, open phpMyAdmin → *Import* → choose `database/metric_laundry.sql`.
   (Or run `mysql -u root -p < database/metric_laundry.sql`.)
2. **Connection:** edit `src/main/resources/db.properties` if your MySQL user or password is different
   (the XAMPP default is `root` with an empty password).
3. **Open the project:** NetBeans → *File → Open Project* → select this folder (NetBeans detects the `pom.xml`).
4. **Run:** right-click the project → *Run* (main class `com.metriclaundry.Main`).
   Command line: `mvn package` → `java -jar target/metric-laundry-1.0.0-jar-with-dependencies.jar`

## Default accounts
| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | `staff` | `staff123` |
| Customer | `juan` | `customer123` |
| Customer | `maria` | `customer123` |

Change these passwords after the first login (*Settings → Change Password*).

### Upgrading the older email-login database

If login reports unknown columns such as `id` or `username`, the database uses the
older schema. Back it up first, then import `database/migrate_legacy.sql` **once**
instead of importing the reset script `metric_laundry.sql`.
The migration keeps the original tables under `legacy_*`, preserves accounts,
passwords, services, orders and payments, and stores roles as `ADMIN`, `STAFF`,
and `CUSTOMER`. Existing accounts can still sign in with their email and password.
The original demo admin/staff receive usernames `admin`/`staff`; other migrated
accounts receive `customer<original user ID>`. Missing default accounts are added
without resetting existing passwords. For example, the original customer with
ID 8 can sign in as `customer8` using their existing password.

To check default-account login and dashboard creation against a local database,
compile and run `src/test/java/com/metriclaundry/LoginDatabaseCheck.java` with
the application's classes and dependencies on the classpath.

## What each role can do
* **Customer:** register, place an order, track it live, pay by GCash (QR + reference no.) / cash / card, view history and receipts.
* **Staff:** work queue, walk-in orders, move orders through *Received → Washing → Drying → Ready → Completed*, confirm payments, add customers.
* **Admin:** everything Staff can do, plus the KPI dashboard and sales chart, managing services and prices, managing staff and customer accounts, reports with CSV export, and status override.

## Customising
* **Edit screens visually:** open a screen under `src/main/java/com/metriclaundry/ui/` in NetBeans and select **Design**. Each screen/dialog has a matching `.form` file; the shared `Toolbar` also has its own form. Run **Clean and Build** first so the designer can load the project's custom Swing components.
* **Keep each pair together:** `.form` stores the designer layout and properties; NetBeans generates the guarded `initComponents()` and component declarations in the matching `.java` file. Make layout changes in Design view and keep event handlers, database calls and validation outside the generated blocks.
* **Custom visuals and dynamic content:** painted graphics, reusable composite widgets, navigation entries and database-driven rows/cards still use Java helpers. These appear as components in a screen's form; their internal drawing/content stays in the corresponding helper or component class. Both `.java` and `.form` files are required when moving a screen.
* **Your real GCash QR:** save it as `gcash_qr.png` next to the jar (or in `src/main/resources/images/`).
* **Colours and fonts:** `ui/theme/Theme.java`
* **Shop address and hours:** `ui/customer/AboutPage.java`
