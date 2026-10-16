
package org.example.rental;

import com.example.rental.client.RentalNotifier;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RentalNotifierTest {

    @Test
    void notifierIsCalledOnce() {

        RentalNotifier notifier =
                mock(RentalNotifier.class);

        notifier.notifyCustomer("RENT-001");

        verify(notifier, times(1))
                .notifyCustomer("RENT-001");
    }
}
