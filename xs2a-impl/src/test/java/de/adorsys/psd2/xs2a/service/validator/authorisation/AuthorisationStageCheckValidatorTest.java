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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_400;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthorisationStageCheckValidatorTest {
    private static final String TEST_PASSWORD = "123";
    private static final String TEST_AUTH_METHOD_ID = "SMS";
    private static final String TEST_AUTH_DATA = "123456";
    private static final ScaStatus RECEIVED_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus PSUIDENTIFIED_STATUS = ScaStatus.PSUIDENTIFIED;
    private static final ScaStatus PSUAUTHENTICATED_STATUS = ScaStatus.PSUAUTHENTICATED;
    private static final ScaStatus SCAMETHODSELECTED_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final ScaStatus FINALISED_STATUS = ScaStatus.FINALISED;
    private static final ScaStatus EXEMPTED_STATUS = ScaStatus.EXEMPTED;
    private static final AuthorisationServiceType AIS_AUTHORISATION = AuthorisationServiceType.AIS;
    private static final AuthorisationServiceType PIS_AUTHORISATION = AuthorisationServiceType.PIS;
    private static final AuthorisationServiceType PIS_CANCELLATION_AUTHORISATION = AuthorisationServiceType.PIS_CANCELLATION;
    private static final MessageErrorCode SERVICE_INVALID = SERVICE_INVALID_400;
    private static final ErrorType AIS_400_ERROR = ErrorType.AIS_400;
    private static final ErrorType PIS_400_ERROR = ErrorType.PIS_400;
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null);
    private static final PsuIdData NON_EMPTY_PSU_DATA = new PsuIdData("Test", null, null, null);

    private AuthorisationStageCheckValidator checkValidator;

    @Before
    public void setUp() {
        checkValidator = new AuthorisationStageCheckValidator();
    }

    @Test
    public void test_received_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPsuData(NON_EMPTY_PSU_DATA);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_received_failure_emptyPsuData_ais() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPsuData(EMPTY_PSU_DATA);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, AIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_received_failure_emptyPsuData_pis() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPsuData(EMPTY_PSU_DATA);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_received_failure_emptyPsuData_pis_cancellation() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPsuData(EMPTY_PSU_DATA);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, PIS_CANCELLATION_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuIdentified_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPassword(TEST_PASSWORD);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_psuIdentified_failure_noPassword_ais() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, AIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuIdentified_failure_noPassword_pis() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuIdentified_failure_noPassword_pis_cancellation() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, PIS_CANCELLATION_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuAuthenticated_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setAuthenticationMethodId(TEST_AUTH_METHOD_ID);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_psuAuthenticated_failure_noAuthenticationMethodId_ais() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, AIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuAuthenticated_failure_noAuthenticationMethodId_pis() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_psuAuthenticated_failure_noAuthenticationMethodId_pis_cancellation() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, PIS_CANCELLATION_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_scaMethodSelected_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setScaAuthenticationData(TEST_AUTH_DATA);

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_scaMethodSelected_failure_noScaAuthenticationData_ais() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, AIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_scaMethodSelected_failure_noScaAuthenticationData_pis() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_scaMethodSelected_failure_noScaAuthenticationData_pis_cancellation() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, PIS_CANCELLATION_AUTHORISATION);

        //Then
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_400_ERROR, validationResult.getMessageError().getErrorType());
        assertEquals(SERVICE_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void test_finalised_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, FINALISED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_exempted_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult validationResult = checkValidator.validate(updateRequest, EXEMPTED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(validationResult.isValid());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildPisUpdateRequest() {
        return new Xs2aUpdatePisCommonPaymentPsuDataRequest();
    }
}
