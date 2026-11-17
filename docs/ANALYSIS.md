# Metric Laundry Management System — System Analysis

Prepared from the UI mockup (11 screens) · Target: Java 17 desktop application (Swing) + MySQL

---

## 1. What the mockup tells us

| Screen in mockup | Role | Kept / Changed |
|---|---|---|
| Customer Dashboard (services, recent orders, track widget) | Customer | Kept |
| Order Tracking (6-step tracker) | Customer | Kept + auto-refresh every 30 s |
| Payment (GCash QR, COD, Card) | Customer | Kept, but changed how payments are verified (see below) |
| Digital Receipt | Customer / Staff / Admin | Kept + Print and Save-as-image |
| Order History | Customer | Kept + filters |
| About Us | Customer | Kept |
| Admin Login | Admin | **Changed into one login for all roles** |
| Admin Dashboard (KPIs, sales chart, recent orders) | Admin | Kept |
| Manage Services | Admin | Kept |
| *(not in mockup)* Staff panel | Staff | **Added** |
| *(not in mockup)* Customer registration | Customer | **Added** |
| *(not in mockup)* Manage Customers / Staff / Payments / Reports | Admin | **Added** (they appear in the mockup's sidebar but have no screens) |

### Gaps and risks found in the mockup

1. **There's no Staff role.** The mockup has a customer side and an admin side, but nobody who runs the counter day to day. I added a Staff panel. Staff can receive laundry, update statuses and take payments. They cannot change prices, manage staff accounts or see reports.
2. **GCash payments aren't verified.** The receipt shows "Payment Status: Paid" right after the customer clicks Confirm. Anyone could click it without paying. In this design the customer enters the GCash **reference number**, and the payment stays **Pending** until staff check it in the shop's GCash app.
3. **Credit/Debit card entry.** A desktop app must not collect card numbers, because that brings in PCI-DSS rules. Card payments are made on the shop's POS terminal and confirmed by staff. The app never stores card data.
4. **Some mockup data doesn't match.** Order History shows *Folding · 5 pcs* (Folding is priced per **kg**) and *Dry Cleaning · 1 pc · ₱80* (the price list says ₱50/pc). Two rules fix this:
   * the unit price is **copied onto the order** when the order is placed, so later price changes don't affect old orders or receipts;
   * the quantity unit always comes from the service (KG allows decimals, PC allows whole numbers only).
5. **Revenue isn't defined.** "Total Revenue ₱6,850" could mean ordered amounts or money actually received. Here it is **PAID payments only**.
6. **Laundry could be released unpaid.** Nothing stops an order from being marked Completed before it's paid. Rule BR-07 blocks this.
7. **Deleting a service.** If a service that old orders use is deleted, those orders break. A service that's already in use is **deactivated** instead of deleted.

---

## 2. Actors and permissions

| Feature | Customer | Staff | Admin |
|---|:-:|:-:|:-:|
| Register account | ✔ | – | – |
| Place own order | ✔ | – | – |
| Create walk-in order for a customer | – | ✔ | ✔ |
| Track own orders / history | ✔ | – | – |
| View all orders | – | ✔ | ✔ |
| Move order to next step | – | ✔ | ✔ |
| Override status (any status, with reason) | – | – | ✔ |
| Cancel order | only while *Order Placed* | ✔ (unpaid) | ✔ (unpaid) |
| Submit payment (GCash ref / COD / Card) | ✔ (→ Pending) | ✔ (→ Paid) | ✔ (→ Paid) |
| Confirm pending payment | – | ✔ | ✔ |
| View / print receipt | ✔ own | ✔ | ✔ |
| Add customer | – | ✔ | ✔ |
| Edit / deactivate users, reset passwords | – | – | ✔ |
| Manage staff & admin accounts | – | – | ✔ |
| Manage services & prices | – | – | ✔ |
| Dashboard KPIs, sales chart, reports, CSV export | – | counters only | ✔ |
| Change own password | ✔ | ✔ | ✔ |

---

## 3. Order life-cycle

```
 PLACED ──► RECEIVED ──► WASHING ──► DRYING ──► READY ──► COMPLETED
   │            │            │          │          │
   └────────────┴────────────┴──────────┴──────────┴──►  CANCELLED  (only if unpaid)
```

* Staff move an order forward **one step at a time**. They can't skip steps or go backwards.
* Only an **Admin** can override a status (for example, to fix a mistake). A reason is required and is saved in the history.
* Every change writes a row to `order_status_history` (who, when, remarks). The customer's tracking timeline is built from this table.

---

## 4. Business rules

| ID | Rule | Enforced in |
|---|---|---|
| BR-01 | Total = quantity × unit price. Unit price is copied from the service when the order is placed. | `OrderService.placeOrder` |
| BR-02 | Quantity must be > 0 and ≤ 100. Per-piece services accept whole numbers only. | `OrderService.placeOrder` |
| BR-03 | Estimated completion = time placed + the service's turnaround hours. | `OrderService.placeOrder` |
| BR-04 | Order No. = `ML-yyyyMMdd###`. Receipt No. = `RC-yyyyMMdd###` (same suffix). | `OrderDAO.insert`, `PaymentService` |
| BR-05 | Status moves forward one step at a time. Only staff/admin can change it. | `OrderService.advance` |
| BR-06 | Customers may cancel only while the order is *Order Placed*. Paid orders can't be cancelled; refunds are handled at the counter. | `OrderService.cancel` |
| BR-07 | An order can't be **Completed** (released) until its payment is **Paid**. | `OrderService.advance / overrideStatus` |
| BR-08 | One payment per order. The amount always equals the order total (it can't be edited in the UI). | `PaymentService.submit` |
| BR-09 | GCash requires a 6–13 digit reference number. Customer payments start as *Pending*. Payments recorded by staff at the counter are *Paid* right away. | `PaymentService.submit` |
| BR-10 | Revenue = sum of PAID payments. | `ReportDAO` |
| BR-11 | A service used by orders is deactivated, not deleted. | `ServiceDAO.deleteOrDeactivate` |
| BR-12 | Usernames are unique, 3–30 characters. Passwords have at least 6 characters. PH mobile format is 09xxxxxxxxx. | `AuthService` |
| BR-13 | An admin can't deactivate or demote their own account (so the last admin can't be locked out). | `AuthService.updateUser`, `UsersPage` |

---

## 5. Data model (ERD)

```mermaid
erDiagram
    USERS ||--o{ ORDERS : "places (customer_id)"
    USERS ||--o{ ORDER_STATUS_HISTORY : "changes"
    USERS ||--o{ PAYMENTS : "receives (staff)"
    SERVICES ||--o{ ORDERS : "is ordered"
    ORDERS ||--o{ ORDER_STATUS_HISTORY : "has"
    ORDERS ||--o| PAYMENTS : "is paid by"

    USERS { int id PK; varchar username UK; varchar password_hash; varchar salt; varchar full_name; varchar email; varchar phone; varchar address; enum role; bool active; datetime created_at }
    SERVICES { int id PK; varchar name UK; decimal price; enum unit; varchar description; int turnaround_hours; bool active }
    ORDERS { int id PK; varchar order_no UK; int customer_id FK; int service_id FK; decimal quantity; decimal unit_price; decimal total; enum status; varchar notes; datetime estimated_completion; int created_by FK; datetime created_at; datetime updated_at }
    ORDER_STATUS_HISTORY { int id PK; int order_id FK; enum status; int changed_by FK; varchar remarks; datetime changed_at }
    PAYMENTS { int id PK; varchar receipt_no UK; int order_id FK_UK; enum method; decimal amount; varchar reference_no; enum status; int received_by FK; datetime created_at; datetime paid_at }
```

Design notes: one `users` table with a `role` column, which keeps the login simple. Money is stored as `DECIMAL(10,2)` and handled as `BigDecimal` in Java (never `double`). There are indexes on `status`, `created_at` and `role` for the dashboard queries.

---

## 6. Architecture

```
com.metriclaundry
├── Main                  start-up + DB health check
├── config/Database       JDBC connection (db.properties, overridable)
├── model/                POJOs + enums (Role, OrderStatus, PaymentMethod, PaymentStatus)
├── dao/                  SQL only (PreparedStatement, try-with-resources, transactions)
├── service/              ALL business rules (Auth, Order, Payment, Catalog)
├── util/                 PasswordUtil (PBKDF2), Formats (₱, dates), Session, AppException
└── ui/
    ├── theme/            Theme (colours, fonts, Look&Feel) + Icons (vector icons)
    ├── components/       RoundedPanel, AppButton, InputField, Logo, Badge, StepTracker,
    │                     Timeline, LineChart, StatCard, QrCode, HeroBanner, ...
    ├── common/           DashboardFrame (sidebar shell), Page, shared pages & dialogs
    ├── customer/         Home, Place Order, Track, Payment, History, About
    ├── staff/            Staff dashboard + work queue
    └── admin/            Admin dashboard, Services, Reports
```

* **Layers:** UI → Service → DAO → MySQL. The UI never runs SQL, and the DAO never makes business decisions.
* **One shell, three roles:** `DashboardFrame` is reused. Each role registers only the pages it's allowed to use, and the service layer checks the role again on every action (defence in depth).
* **Responsive UI:** database calls run in a `SwingWorker` (`Page.load`) so the window doesn't freeze.
* **Errors:** `AppException` carries a message that's safe to show the user. Unexpected exceptions are logged.

---

## 7. Security

* Passwords are hashed with **PBKDF2-HMAC-SHA256** (65,536 iterations) with a random salt for each user. Hashes are compared in constant time.
* Every query uses a `PreparedStatement`, so SQL injection isn't possible.
* A failed login says "Invalid username or password" whether the username or the password was wrong, so usernames can't be guessed.
* No card data is stored. GCash payments are verified by a person.
* All status and payment changes are written to an audit trail.

---

## 8. Not included yet / next steps

* SMS or email notification when an order is *Ready for Pickup*.
* Rider / delivery scheduling for Cash on Delivery.
* Live GCash API integration (needs a GCash for Business merchant account).
* Unit tests (JUnit 5) for `OrderService` and `PaymentService`. The business rules were already checked against a real MariaDB database during development.
