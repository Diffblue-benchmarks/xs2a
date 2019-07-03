package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class Xs2AUpdatePaymentAfterSpiServiceTest {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACSP;
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";

    @InjectMocks
    private Xs2aUpdatePaymentAfterSpiService xs2AUpdatePaymentAfterSpiService;
    @Mock
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;


    @Test
    public void updatePaymentStatus_success() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID,TRANSACTION_STATUS))
            .thenReturn(true);

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void updatePaymentStatus_failed() {
        //Given

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isFalse();
    }
}
