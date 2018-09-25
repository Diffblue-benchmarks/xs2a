/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationServiceTest {
    private final String FUNDS_REQ_DATA = "/json/FundsConfirmationRequestTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String BALANCES_SOURCE = "/json/BalancesTestData.json";

    private final Currency EUR = Currency.getInstance("EUR");
    private final String AMOUNT_1600 = "1600.00";
    private final MessageError FORMAT_MESSAGE_ERROR = new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR));

    @InjectMocks
    private FundsConfirmationService fundsConfirmationService;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);
    @Mock
    private AccountReferenceValidationService referenceValidationService;
    @Mock
    private AccountService accountService;

    @Before
    public void setUp() throws IOException {
        when(accountService.getAccountDetailsByAccountReference(any(Xs2aAccountReference.class), anyString()))
            .thenReturn(Optional.of(new Xs2aAccountDetails(null, null, null, null, null, null, null, null, null, null, null, null, null, getBalances())));
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(ResponseObject.builder().build());
    }

    @Test
    public void fundsConfirmation_success() throws Exception {
        //Given:
        FundsConfirmationRequest request = readFundsConfirmationRequest();

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(request);

        //Then
        assertThat(actualResponse.getBody().isFundsAvailable()).isEqualTo(true);
    }

    @Test
    public void fundsConfirmation_notEnoughMoney() throws Exception {
        //Given:
        FundsConfirmationRequest request = readFundsConfirmationRequest();
        request.setInstructedAmount(getAmount1600());

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(request);

        //Then
        assertThat(actualResponse.getBody().isFundsAvailable()).isEqualTo(false);
    }

    //@Test - excluded from testing because request validating in de.adorsys.psd2.api.FundsConfirmationApi
    public void fundsConfirmation_reqIsNull() {
        //Given:
        FundsConfirmationRequest request = null;

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(request);

        //Then
        assertThat(actualResponse.getBody()).isEqualTo(null);
        assertThat(actualResponse.getError()).isEqualTo(FORMAT_MESSAGE_ERROR);
    }

    private FundsConfirmationRequest readFundsConfirmationRequest() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), FundsConfirmationRequest.class).get();
    }

    private Xs2aAmount getAmount1600() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount(AMOUNT_1600);
        amount.setCurrency(EUR);
        return amount;
    }

    private List<Xs2aBalance> getBalances() throws IOException {
        Xs2aBalance balance = jsonConverter.toObject(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Xs2aBalance.class).get();
        return Collections.singletonList(balance);
    }
}
