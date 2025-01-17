/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Xs2aCreatePisCancellationAuthorisationResponseTest {
    @Test
    public void getAuthorisationResponseType_shouldReturnStart() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse response = new Xs2aCreatePisCancellationAuthorisationResponse("some cancellation id", ScaStatus.RECEIVED, PaymentType.SINGLE, null);

        // When
        AuthorisationResponseType actual = response.getAuthorisationResponseType();

        // Then
        assertEquals(AuthorisationResponseType.START, actual);
    }
}
