# Basic Cashback Calculation

**As a customer, I want to earn cashback on my purchases so that I'm rewarded for shopping with partner merchants.**

## Rules and Examples

### Rule: Must calculate cashback as the merchant's configured rate multiplied by the purchase amount

| Merchant Rate | Purchase Amount | Cashback |
|---|---|---|
| 5%   | $100.00 | $5.00 |
| 2.5% | $40.00  | $1.00 |
| 10%  | $25.50  | $2.55 |
| 0%   | $100.00 | $0.00 |

The 0% row is the boundary case: a partner merchant configured with no cashback earns nothing, but the calculation still runs.

---

### Rule: Must use the merchant rate that was effective at the time of payment (the purchase timestamp)

Acme's rate changes from 2% to 5%, effective `2026-05-01 14:00:00`:

| Purchase Time | Rate Applied |
|---|---|
| 2026-05-01 13:59:59 | 2% |
| 2026-05-01 14:00:00 | 5% |
| 2026-05-01 14:00:01 | 5% |

The 14:00:00 row is the boundary: a new rate applies from the exact moment it takes effect.

- **Counter-example:** The one where a purchase is paid at 13:59 and settles after 14:00 — it still earns 2%, because the rate is fixed at payment time, not settlement time.
- **Counter-example:** The one where an admin changes Acme's rate today — purchases made yesterday are not recalculated.

---

### Rule: Must only award cashback for purchases at registered partner merchants

- **Example:** The one where a customer pays $80 at GreenGrocer (partner, 3%) and earns $2.40.
- **Counter-example:** The one where a customer pays $80 at a registered non-partner café — the purchase is accepted, but no cashback is awarded.

---

### Rule: Must reject purchases from unknown merchants

- **Example:** The one where a purchase comes from "UnknownShop", which has no merchant record — the system rejects it with an error saying the merchant is not recognised.
- **Counter-example:** The one where the merchant is registered but is not a partner — the purchase is accepted without cashback (the partner rule applies, not this one).

---

### Rule: Must round cashback to whole cents using banker's rounding (half-even)

| Computed | Credited |
|---|---|
| $1.664 | $1.66 |
| $1.665 | $1.66 |
| $1.666 | $1.67 |
| $1.675 | $1.68 |

The two `.5` rows show the tie boundary: a tie rounds to the nearest even cent.

---

### Rule: Must not award cashback when the purchase amount is zero or negative

- **Example:** The one where a $0.00 purchase (a free promo item) at a partner merchant produces no cashback.
- **Counter-example:** The one where a $0.01 purchase earns cashback normally — it is the minimum qualifying amount.

Refunds (negative amounts) are out of scope and will be handled in a separate Refund & Reversal feature.

---

### Rule: Must credit cashback to the customer's available rewards balance only after the transaction has settled

- **Example:** The one where a customer with a $12.00 available balance makes a $50 purchase at 4% on Monday; when the transaction settles on Wednesday, $2.00 is credited and the available balance becomes $14.00.
- **Counter-example:** The one where the purchase has been made but has not settled yet — the available balance stays at $12.00.

---

### Rule: Must show cashback from unsettled transactions as "pending" on the customer's rewards balance

- **Example:** The one where a customer with a $12.00 available balance makes a $50 purchase at 4% on Monday; before settlement, their rewards balance shows $12.00 available plus a separate $2.00 pending entry tied to the Monday purchase.
- **Counter-example:** The one where the transaction settles on Wednesday — the $2.00 moves from pending into the available balance ($14.00) and is no longer shown as pending.
