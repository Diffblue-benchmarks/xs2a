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

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsAmount;
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPisExportControllerTest {
    private final String PSU_ID = "marion.mueller";
    private final String TPP_ID = "PSDDE-FAKENCA-87B2AC";
    private final String ACCOUNT_ID = "account_id";
    private final String EXPORT_PIS_CONSENT_BY_TPP = "/aspsp-api/v1/pis/payments/tpp/PSDDE-FAKENCA-87B2AC";
    private final String EXPORT_PIS_CONSENT_BY_PSU = "/aspsp-api/v1/pis/payments/psu";
    private final String EXPORT_PIS_CONSENT_BY_ACCOUNT = "/aspsp-api/v1/pis/payments/account/account_id";
    private final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private final LocalDate END_DATE = LocalDate.of(2020, 7, 22);
    private final String INSTANCE_ID = "UNDEFINED";
    private final String LIST_OF_PIIS_CONSENTS_PATH = "json/pis/list-pis-payment.json";

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private CmsPayment cmsPayment;
    private Collection<CmsPayment> cmsPayments;

    @InjectMocks
    private CmsAspspPisExportController cmsAspspPisExportController;

    @Mock
    private CmsAspspPisExportService cmsAspspPisExportService;

    @Before
    public void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        cmsPayment = getCmsPayment();
        cmsPayments = Collections.singletonList(cmsPayment);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Start-Date", START_DATE.toString());
        httpHeaders.add("End-Date", END_DATE.toString());
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders.standaloneSetup(cmsAspspPisExportController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    public void getPaymentsByTpp() throws Exception {
        when(cmsAspspPisExportService.exportPaymentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID))
            .thenReturn(cmsPayments);

        mockMvc.perform(get(EXPORT_PIS_CONSENT_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID);
    }

    @Test
    public void getPaymentsByPsu() throws Exception {
        when(cmsAspspPisExportService.exportPaymentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID))
            .thenReturn(cmsPayments);

        mockMvc.perform(get(EXPORT_PIS_CONSENT_BY_PSU)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID);
    }

    @Test
    public void getPaymentsByAccountId() throws Exception {
        when(cmsAspspPisExportService.exportPaymentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID))
            .thenReturn(cmsPayments);

        mockMvc.perform(get(EXPORT_PIS_CONSENT_BY_ACCOUNT)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPisExportService, times(1)).exportPaymentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID);
    }

    private CmsPayment getCmsPayment() {
        String paymentProduct = "paymentProduct";
        CmsSinglePayment result = new CmsSinglePayment(paymentProduct);
        result.setEndToEndIdentification("RI-1234567890");
        result.setInstructedAmount(new CmsAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(1000)));
        result.setCreditorAgent("agent");
        result.setCreditorName("name");
        result.setCreditorAddress(new CmsAddress());
        result.setRemittanceInformationUnstructured("remittanceInformationUnstructured");
        return result;
    }
}
