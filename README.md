# Product
Dress Rental Service. People track dress rental orders.

# Core item
RentalId

# Status table
| From | To | Allowed | Reason |
| :--- | :--- | :--- | :--- |
| Booked | Rented | Yes | Customer picked up the dress. |
| Rented | Returned | Yes | Customer returned the dress back. |
| Booked | Returned | No | Cannot return a dress that was never picked up. |
| Returned | Booked | No | Completed order cannot be re-booked directly. |

# Forbidden - why
1. **Booked -> Returned:** A dress cannot be marked as returned without first being handed over to the client.
2. **Returned -> Booked:** A completed rental cycle cannot be reset; a new booking requires a new rental order ID.

# Package Architecture (Lab 2)
```text
  dto         client        handler         config
  (JSON)      (HTTP)        (HTTP)         Application
                                           @Service
        \            \            /           |
         \            \          /       injects Rule
          \            \        /
                    domain
              RentalId  RentalStatus  RentalPolicy
              RentalRule + two implementations
                    (no Spring)
