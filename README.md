# 🏡 PropApp - Real Estate Query & Relevance Scoring Engine (Python Edition)

PropApp is a robust, production-ready real estate data filtering and ranking platform built with **FastAPI** and backed by **SQLAlchemy** connected to a **PostgreSQL** cluster. The engine ingests property streams from multiple feed sources, parses complex data constraints, applies an active timeline aging decay algorithm, and paginates responses securely.

---

## 🚀 System Architecture Layout
The application utilizes a modular design configured using standard Python abstractions to ensure optimal runtime execution speed and clean type validation:

* **`main.py (Models Layer)`** -> Declares database relational mappings (`ListingModel`) using SQLAlchemy ORM to manage composite keys (`id` + `source`) smoothly.
* **`main.py (Pydantic Schemas)`** -> Handles inbound parameters and serializes JSON responses (`ScoredListing`) to match standard camelCase layouts (`relevanceScore`, `content`).
* **`main.py (Scoring Engine)`** -> Houses the core mathematical logic executing structural queries, keyword extraction, and compound weekly aging calculations.
* **`main.py (API Routes Ingress)`** -> Validation gateway layer that evaluates incoming data parameters via FastAPI Dependencies before executing active database sessions.

---

## 🧠 Core Relevance Ranking Engine Matrix
Instead of basic database sort queries, listings are dynamically assigned a **Relevance Match Score (0.0% - 100.0%)** at runtime using a weighted evaluation index:

$$\text{Relevance Score} = \text{Budget Variance (70% Max)} + \text{Chronological Recency (30% Max)}$$

### 1. Budget Fit Variance (70 Points Max)
* **Under-Budget Properties:** If a property list price matches or sits underneath the user's explicit target budget parameter, it instantly secures the complete **70 points**.
* **Over-Budget Properties:** If a property exceeds the target budget threshold, points degrade linearly using a relative distance variance decay equation to avoid abrupt dataset cutoffs:
$$\text{Budget Score} = \max\left(0.0,\; 70.0 \times \left(1.0 - \frac{\text{Property Price} - \text{Target Budget}}{\text{Target Budget}}\right)\right)$$

### 2. Timeline Market Aging Decay (30 Points Max)
* **Fresh Inventory:** Active property listings published to the market feed inside the last 7 days receive the full **30 points**.
* **Compounding Weekly Penalty:** Older listings are gradually penalized to prioritize new inventory, using a 10% compounding weekly decay formula based on their exact day offset age relative to our fixed timeline anchor (**September 12, 2026**):
$$\text{Recency Score} = \max\left(0.0,\; 30.0 \times 0.90^{\left(\frac{\text{Days Old}}{7.0}\right)}\right)$$

### 🏆 Tie-Breaker Resolution Rules
If multiple properties achieve an identical relevance percentage match, the application runs a tie-breaker routine that evaluates the raw `listed_date` attribute to force the **newest inventory to display first**.

---

## 🛡️ Deliberate Input Validation Guardrails
The system protects against faulty inputs at the API ingress gate, throwing precise `HTTP 400`, `HTTP 422`, or `HTTP 404` exceptions rather than allowing silent data failures or empty data sets:

* **Pricing Logic Contradictions:** Rejects requests with an immediate `HTTP 400 Bad Request` if `minPrice` is greater than `maxPrice`.
* **Negative Pricing Bounds:** Rejects queries with an `HTTP 400 Bad Request` if `minPrice` or `maxPrice` fall below zero.
* **Invalid Layout Limits:** Blocks pagination page sizes (`size`) less than 1 natively via FastAPI Query parameters, returning `HTTP 422 Unprocessable Content`.
* **Explicit Missing City Alerts:** Checks if a requested city exists in the database. If it is not found, it responds with an `HTTP 404 Not Found` error string instead of returning a misleading, blank list.

---

## 🛠️ Verification & Deployment Manual

### 1. Verify Virtual Dependencies
Ensure you have the required modern SQLAlchemy and Postgres runtime binaries installed in your environment:
```bash
python -m pip install fastapi uvicorn pydantic sqlalchemy psycopg2-binary httpx pytest selenium
```

### 2. Boot the FastAPI Gateway App
```bash
python main.py
```

### 3. Run Automated Validation Checks (Pytest)
```bash
python -m pytest -v test_ui.py
```

### 4. Execute Scored Evaluation Engine (cURL)
```bash
curl "http://127.0.0"
```
