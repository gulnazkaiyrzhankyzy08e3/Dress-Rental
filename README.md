# Car Rental

## Product

Car Rental Management System is a system for managing car rentals.
A client books a car, takes it on the agreed date, and returns it after the rental period.

## Core item

The core item is Rental — one rental record for one car and one client.

The RentalId identifies the rental and cannot be null or blank.

## Status table

| From | To | Allowed |
|---|---|---|
| Reserved | Rented | Yes |
| Rented | Returned | Yes |
| Reserved | Returned | No |
| Returned | Rented | No |

## Forbidden — why

**Reserved → Returned** — forbidden because the car was not handed over to the client yet.

**Returned → Rented** — forbidden because a completed rental cannot be reopened. A new rental must be created.