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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPiisExportControllerTest {
    private final String PSU_ID = "marion.mueller";
    private final String TPP_ID = "PSDDE-FAKENCA-87B2AC";
    private final String ACCOUNT_ID = "account_id";
    private final String EXPORT_PIIS_CONSENT_BY_TPP = "/aspsp-api/v1/piis/consents/tpp/PSDDE-FAKENCA-87B2AC";
    private final String EXPORT_PIIS_CONSENT_BY_PSU = "/aspsp-api/v1/piis/consents/psu";
    private final String EXPORT_PIIS_CONSENT_BY_ACCOUNT = "/aspsp-api/v1/piis/consents/account/account_id";
    private final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private final LocalDate END_DATE = LocalDate.of(2020, 7, 22);
    private final String INSTANCE_ID = "UNDEFINED";
    private final String LIST_OF_PIIS_CONSENTS_PATH = "json/piis/list-piis-consent.json";

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private PiisConsent piisConsent;
    private Collection<PiisConsent> piisConsents;

    @InjectMocks
    private CmsAspspPiisExportController cmsAspspPiisExportController;

    @Mock
    private CmsAspspPiisFundsExportService cmsAspspPiisExportService;

    @Before
    public void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        piisConsents = Collections.singletonList(piisConsent);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Start-Date", START_DATE.toString());
        httpHeaders.add("End-Date", END_DATE.toString());
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders
                      .standaloneSetup(cmsAspspPiisExportController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    public void getConsentsByTpp_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID))
            .thenReturn(piisConsents);

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID);
    }

    @Test
    public void getConsentsByPsu_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID))
            .thenReturn(piisConsents);

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_PSU)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID);
    }

    @Test
    public void getConsentsByAccount_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID))
            .thenReturn(piisConsents);

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_ACCOUNT)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID);
    }
}
