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

package de.adorsys.psd2.consent.web.xs2a.controller;


import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentControllerTest {

    private static final String CONSENT_ID = "ed4190c7-64ee-42fb-b671-d62645f54672";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final ConsentStatus WRONG_CONSENT_STATUS = ConsentStatus.EXPIRED;
    private static final AisConsentAuthorizationRequest CONSENT_AUTHORIZATION_REQUEST = getConsentAuthorizationRequest();
    private static final AisConsentAuthorizationRequest WRONG_CONSENT_AUTHORIZATION_REQUEST = getWrongConsentAuthorizationRequest();
    private static final String PSU_ID = "4e5dbef0-2377-483f-9ab9-ad510c1a557a";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String AUTHORIZATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String AUTHORIZATION_ID_1 = "4400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_AUTHORIZATION_ID = "Wrong authorization id";
    private static final AisConsentAuthorizationResponse CONSENT_AUTHORIZATION_RESPONSE = getConsentAuthorizationResponse();

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private AisConsentController aisConsentController;

    @Mock
    private AisConsentServiceEncrypted aisConsentService;
    @Mock
    private AisConsentAuthorisationServiceEncrypted aisAuthorisationServiceEncrypted;

    @Before
    public void setUp() {
        when(aisConsentService.getConsentStatusById(eq(CONSENT_ID))).thenReturn(Optional.of(ConsentStatus.RECEIVED));
        when(aisConsentService.getConsentStatusById(eq(WRONG_CONSENT_ID))).thenReturn(Optional.empty());
        when(aisConsentService.updateConsentStatusById(eq(CONSENT_ID), eq(CONSENT_STATUS))).thenReturn(true);
        when(aisConsentService.updateConsentStatusById(eq(WRONG_CONSENT_ID), eq(WRONG_CONSENT_STATUS))).thenReturn(false);
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(CONSENT_ID), eq(CONSENT_AUTHORIZATION_REQUEST))).thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(WRONG_CONSENT_ID), eq(CONSENT_AUTHORIZATION_REQUEST))).thenReturn(Optional.empty());
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(CONSENT_ID), eq(WRONG_CONSENT_AUTHORIZATION_REQUEST))).thenReturn(Optional.empty());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(eq(AUTHORIZATION_ID_1), eq(CONSENT_AUTHORIZATION_REQUEST))).thenReturn(true);
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(eq(AUTHORIZATION_ID), eq(CONSENT_AUTHORIZATION_REQUEST))).thenReturn(false);
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(eq(WRONG_AUTHORIZATION_ID), eq(WRONG_CONSENT_AUTHORIZATION_REQUEST))).thenReturn(false);
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(AUTHORIZATION_ID), eq(CONSENT_ID))).thenReturn(Optional.of(CONSENT_AUTHORIZATION_RESPONSE));
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(AUTHORIZATION_ID), eq(WRONG_CONSENT_ID))).thenReturn(Optional.empty());
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(WRONG_AUTHORIZATION_ID), eq(CONSENT_ID))).thenReturn(Optional.empty());
    }

    @Test
    public void createConsent_success() {
        // Given
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        CreateAisConsentResponse serviceResponse = new CreateAisConsentResponse(CONSENT_ID, new AisAccountConsent());
        when(aisConsentService.createConsent(createRequest)).thenReturn(Optional.of(serviceResponse));

        // When
        ResponseEntity<CreateAisConsentResponse> actualResponse = aisConsentController.createConsent(createRequest);

        // Then
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(serviceResponse, actualResponse.getBody());
    }

    @Test
    public void createConsent_emptyServiceResponse() {
        // Given
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        when(aisConsentService.createConsent(createRequest)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CreateAisConsentResponse> actualResponse = aisConsentController.createConsent(createRequest);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void getConsentStatusById_Success() {

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(CONSENT_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getConsentStatus()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    public void getConsentStatusById_Fail() {

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(WRONG_CONSENT_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentStatus_Success() {

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateConsentStatus_Fail() {

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(WRONG_CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createConsentAuthorization_Success() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorizationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().getAuthorizationId()).isEqualTo(AUTHORIZATION_ID);
    }

    @Test
    public void createConsentAuthorization_Fail_WrongConsentId() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorizationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(WRONG_CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createConsentAuthorization_Fail_WrondRequest() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getWrongConsentAuthorizationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Success() {
        doReturn(true)
            .when(aisAuthorisationServiceEncrypted).updateConsentAuthorization(anyString(), any(AisConsentAuthorizationRequest.class));

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorizationRequest();

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORIZATION_ID_1, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongConsentId() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorizationRequest();

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORIZATION_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongAuthorizationId() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorizationRequest();

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(WRONG_AUTHORIZATION_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongRequest() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getWrongConsentAuthorizationRequest();

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORIZATION_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorization_Success() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, AUTHORIZATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(getConsentAuthorizationResponse());
    }

    @Test
    public void getConsentAuthorization_Fail_WrongConsentId() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(WRONG_CONSENT_ID, AUTHORIZATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorization_Fail_WrongAuthorizationId() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, WRONG_AUTHORIZATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorizationScaStatus_success() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaStatus(CONSENT_ID, AUTHORIZATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(CONSENT_ID, AUTHORIZATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getConsentAuthorizationScaStatus_failure_wrongIds() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORIZATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORIZATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getAuthorisationScaApproach_success() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORIZATION_ID))
            .thenReturn(Optional.of(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORIZATION_ID);

        verify(aisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORIZATION_ID));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScaApproach()).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    public void getAuthorisationScaApproach_error() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORIZATION_ID))
            .thenReturn(Optional.empty());

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORIZATION_ID);

        verify(aisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORIZATION_ID));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    private static AisConsentAuthorizationRequest getConsentAuthorizationRequest() {
        AisConsentAuthorizationRequest request = new AisConsentAuthorizationRequest();
        request.setPsuData(new PsuIdData(PSU_ID, null, null, null));
        request.setPassword("zzz");

        return request;
    }

    private static AisConsentAuthorizationRequest getWrongConsentAuthorizationRequest() {
        AisConsentAuthorizationRequest request = new AisConsentAuthorizationRequest();
        request.setPsuData(new PsuIdData(WRONG_PSU_ID, null, null, null));
        request.setPassword("zzz");

        return request;
    }

    private static AisConsentAuthorizationResponse getConsentAuthorizationResponse() {
        AisConsentAuthorizationResponse authorizationResponse = new AisConsentAuthorizationResponse();
        authorizationResponse.setAuthorizationId(AUTHORIZATION_ID);
        authorizationResponse.setConsentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(new PsuIdData(PSU_ID, null, null, null));

        return authorizationResponse;
    }

    private CreateAisConsentAuthorizationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAisConsentAuthorizationResponse(AUTHORIZATION_ID, ScaStatus.RECEIVED, "");
    }

}
