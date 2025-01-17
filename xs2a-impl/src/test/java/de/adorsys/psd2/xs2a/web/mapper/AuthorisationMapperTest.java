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
package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AuthorisationMapperTest {

    private static final String SELF_LINK = "self";
    private static final String LOCALHOST_LINK = "http://localhost";

    private JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AuthorisationMapper mapper;

    @Mock
    private HrefLinkMapper hrefLinkMapper;

    @Mock
    private ScaMethodsMapper scaMethodsMapper;

    @Mock
    private CoreObjectsMapper coreObjectsMapper;

    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    @Mock
    private AuthorisationModelMapper authorisationModelMapper;

    @Before
    public void setUp() {
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());
    }

    @Test
    public void mapToAuthorisations_equals_success() {
        // given
        Authorisations expectedAuthorisations = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-Authorisations.json", Authorisations.class);
        Xs2aAuthorisationSubResources xs2AAuthorisationSubResources = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-Xs2aAutorisationSubResources.json", Xs2aAuthorisationSubResources.class);

        // when
        Authorisations actualAuthorisations = mapper.mapToAuthorisations(xs2AAuthorisationSubResources);

        // then
        assertEquals(expectedAuthorisations, actualAuthorisations);
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aCreatePisAuthorisationResponse() {
        // given
        StartScaprocessResponse expectedStartScaProcessResponse = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-StartScaProcessResponse-expected.json", StartScaprocessResponse.class);

        Xs2aCreatePisAuthorisationResponse xs2aCreatePisAuthorisationResponse = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-StartScaProcessResponse-ResponseObject.json", Xs2aCreatePisAuthorisationResponse.class);
        ResponseObject<Xs2aCreatePisAuthorisationResponse> responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                                                                .body(xs2aCreatePisAuthorisationResponse)
                                                                                .build();
        when(authorisationModelMapper.mapToStartScaProcessResponse(xs2aCreatePisAuthorisationResponse))
            .thenReturn(expectedStartScaProcessResponse);

        // when
        StartScaprocessResponse actualStartScaProcessResponse = (StartScaprocessResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertEquals(expectedStartScaProcessResponse, actualStartScaProcessResponse);
        verify(authorisationModelMapper).mapToStartScaProcessResponse(xs2aCreatePisAuthorisationResponse);
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aUpdatePisCommonPaymentPsuDataResponse() {
        // given
        UpdatePsuAuthenticationResponse expectedUpdatePsuAuthenticationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-expected.json", UpdatePsuAuthenticationResponse.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-ResponseObject.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(xs2aUpdatePisCommonPaymentPsuDataResponse)
                                                                                       .build();

        List<Xs2aAuthenticationObject> xs2aScaMethods = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-scaMethods.json", new TypeReference<List<Xs2aAuthenticationObject>>() {
        });
        ScaMethods scaMethods = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-scaMethods.json", ScaMethods.class);
        when(scaMethodsMapper.mapToScaMethods(xs2aScaMethods)).thenReturn(scaMethods);

        ChallengeData challengeData = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-challengeData.json", ChallengeData.class);
        de.adorsys.psd2.xs2a.core.sca.ChallengeData xs2aChallengeData = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-challengeData.json", de.adorsys.psd2.xs2a.core.sca.ChallengeData.class);
        when(coreObjectsMapper.mapToChallengeData(xs2aChallengeData)).thenReturn(challengeData);

        // when
        UpdatePsuAuthenticationResponse actualUpdatePsuAuthenticationResponse =
            (UpdatePsuAuthenticationResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertNotNull(actualUpdatePsuAuthenticationResponse.getLinks());
        assertFalse(actualUpdatePsuAuthenticationResponse.getLinks().isEmpty());

        assertFalse(actualUpdatePsuAuthenticationResponse.getScaMethods().isEmpty());

        assertEquals(expectedUpdatePsuAuthenticationResponse.getChosenScaMethod(), actualUpdatePsuAuthenticationResponse.getChosenScaMethod());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getPsuMessage(), actualUpdatePsuAuthenticationResponse.getPsuMessage());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getAuthorisationId(), actualUpdatePsuAuthenticationResponse.getAuthorisationId());
        assertNotNull(actualUpdatePsuAuthenticationResponse.getChallengeData());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getScaStatus(), actualUpdatePsuAuthenticationResponse.getScaStatus());
    }

    @Test
    public void mapToAisCreateOrUpdateAuthorisationResponse_for_CreateConsentAuthorizationResponse() {
        // Given
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-CreateConsentAuthorisationResponse.json", CreateConsentAuthorizationResponse.class);
        StartScaprocessResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/AuthorisationMapper-start-scaprocess-response-expected.json", StartScaprocessResponse.class);

        when(authorisationModelMapper.mapToStartScaProcessResponse(createConsentAuthorizationResponse))
            .thenReturn(expected);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorizationResponse)
                                                                   .build();
        // When
        StartScaprocessResponse actualStartScaProcessResponse =
            (StartScaprocessResponse) mapper.mapToAisCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertEquals(expected, actualStartScaProcessResponse);
        verify(authorisationModelMapper).mapToStartScaProcessResponse(createConsentAuthorizationResponse);
    }

    private Map<String, HrefType> buildLinks() {
        return Collections.singletonMap(SELF_LINK, new HrefType(LOCALHOST_LINK));
    }
}
