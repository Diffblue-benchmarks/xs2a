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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PisAuthorisationValidatorTest {
    private static final String AUTHORISATION_ID = "62561aa4-5d69-4bac-9483-09376188eb78";
    private static final String UNKNOWN_AUTHORISATION_ID = "unknown id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id", null, null, null);

    private static final MessageError UNKNOWN_AUTHORISATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(RESOURCE_UNKNOWN_403));

    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private PisAuthorisationValidator pisAuthorisationValidator;

    @Test
    public void validate_withValidAuthorisation_shouldReturnValid() {
        PisCommonPaymentResponse paymentResponse = buildPisCommonPaymentResponse(new Authorisation(AUTHORISATION_ID, SCA_STATUS, PSU_ID_DATA));

        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(AUTHORISATION_ID, paymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withUnknownAuthorisationId_shouldReturnUnknownError() {
        // Given
        PisCommonPaymentResponse paymentResponse = buildPisCommonPaymentResponse(new Authorisation(AUTHORISATION_ID, SCA_STATUS, PSU_ID_DATA));

        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(UNKNOWN_AUTHORISATION_ID, paymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(UNKNOWN_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(Authorisation authorisation) {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setAuthorisations(Collections.singletonList(authorisation));
        return response;
    }
}
