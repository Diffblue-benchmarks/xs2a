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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ais.AccountHelperService;
import de.adorsys.psd2.xs2a.service.ais.TransactionService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.service.validator.ais.account.DownloadTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountTransactionsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "Test consentId";
    private static final String ACCOUNT_ID = "Test accountId";
    private static final String TRANSACTION_ID = "Test transactionId";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.now();
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = buildSpiAccountConsent();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE_GLOBAL = buildSpiAccountReferenceGlobal();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiTransactionReport SPI_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final SpiContextData SPI_CONTEXT_DATA = buildSpiContextData();
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOTH;
    private static final MessageError VALIDATION_ERROR = buildMessageError();
    private static final String ENTRY_REFERENCE_FROM = "777";
    private static final Boolean DELTA_LIST = Boolean.FALSE;
    private static final Xs2aTransactionsReportByPeriodRequest XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST = buildXs2aTransactionsReportByPeriodRequest();
    private static final String BASE64_STRING_EXAMPLE = "dGVzdA==";
    private static final int DATA_SIZE_BYTES = 1000;
    private static final String FILENAME = "transactions.json";

    private SpiAccountReference spiAccountReference;
    private Xs2aTransactionsDownloadResponse xs2aTransactionsDownloadResponse;
    private AccountConsent accountConsent;
    private InputStream inputStream;
    private TransactionsReportByPeriodObject transactionsReportByPeriodObject;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
    @Mock
    private SpiToXs2aAccountReferenceMapper referenceMapper;
    @Mock
    private SpiTransactionListToXs2aAccountReportMapper transactionsToAccountReportMapper;
    @Mock
    private ValueValidatorService validatorService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private SpiTransaction spiTransaction;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;
    @Mock
    private Transactions transactions;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetTransactionsReportValidator getTransactionsReportValidator;
    @Mock
    private GetTransactionDetailsValidator getTransactionDetailsValidator;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private DownloadTransactionsReportValidator downloadTransactionsReportValidator;
    @Mock
    private SpiToXs2aDownloadTransactionsMapper spiToXs2aDownloadTransactionsMapper;
    @Mock
    private AccountHelperService accountHelperService;

    @Before
    public void setUp() {
        accountConsent = createConsent(createAccountAccess());
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        xs2aTransactionsDownloadResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-transactions-download-response.json", Xs2aTransactionsDownloadResponse.class);
        inputStream = new ByteArrayInputStream("test string".getBytes());
        transactionsReportByPeriodObject = buildTransactionsReportByPeriodObject();
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);

        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        // Given
        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        // Given
        doNothing().when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(SPI_TRANSACTION_REPORT));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(SPI_TRANSACTION_REPORT), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    public void getTransactionsReportByPeriod_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void getTransactionsReportByPeriod_With406ErrorInSpiTransactionReport() {
        // Given
        doNothing().when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorServiceNotSupportedSpiResponse());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, REQUESTED_FORMATS_INVALID);
    }

    @Test
    public void getTransactionsReportByPeriod_Success() {
        // Given
        doNothing().when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();
    }

    @Test
    public void getTransactionsReportByPeriod_WhenConsentIsGlobal_Success() {
        // Given
        Xs2aAccountAccess xs2aAccountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-global.json", Xs2aAccountAccess.class);
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(SPI_ACCOUNT_REFERENCE_GLOBAL);

        AccountConsent accountConsent = createConsent(xs2aAccountAccess);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, SPI_ACCOUNT_REFERENCE_GLOBAL, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE_GLOBAL))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertTrue(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList()));
    }

    @Test
    public void getTransactionsReportByPeriod_Success_ShouldRecordEvent() {
        // Given
        doNothing().when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));
        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null);
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);
    }

    @Test
    public void getTransactionsReportByPeriod_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(getTransactionsReportValidator).validate(transactionsReportByPeriodObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }


    @Test
    public void downloadTransactions_success() throws IOException {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse = new SpiTransactionsDownloadResponse(inputStream, FILENAME, DATA_SIZE_BYTES);
        when(accountSpi.requestTransactionsByDownloadLink(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, new String(Base64.getDecoder().decode(BASE64_STRING_EXAMPLE)), spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransactionsDownloadResponse));
        xs2aTransactionsDownloadResponse.setTransactionStream(inputStream);

        when(spiToXs2aDownloadTransactionsMapper.mapToXs2aTransactionsDownloadResponse(spiTransactionsDownloadResponse)).thenReturn(xs2aTransactionsDownloadResponse);

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, BASE64_STRING_EXAMPLE);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertResponseHasNoErrors(actualResponse);
        assertEquals(DATA_SIZE_BYTES, (long) actualResponse.getBody().getDataSizeBytes());
        assertEquals(FILENAME, actualResponse.getBody().getDataFileName());
        inputStream.close();
    }

    @Test
    public void downloadTransactions_Failure_no_consent_shouldReturn_400() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, BASE64_STRING_EXAMPLE);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    public void downloadTransactions_Failure_validation_fails_shouldReturn_400() {
        // Given
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.invalid(AIS_401, CONSENT_EXPIRED));

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, BASE64_STRING_EXAMPLE);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_EXPIRED);
    }

    @Test
    public void downloadTransactions_Failure_SPI_fails_shouldReturn_error() throws IOException {
        // Given
        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse = new SpiTransactionsDownloadResponse(inputStream, FILENAME, DATA_SIZE_BYTES);
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionsByDownloadLink(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, new String(Base64.getDecoder().decode(BASE64_STRING_EXAMPLE)), spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(spiTransactionsDownloadResponse));
        ErrorHolder errorHolder = ErrorHolder.builder(AIS_401)
                                      .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                                      .build();
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiTransactionsDownloadResponse), ServiceType.AIS)).thenReturn(errorHolder);

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, BASE64_STRING_EXAMPLE);

        // Then
        assertNotNull(actualResponse.getError().getTppMessage());
        inputStream.close();
    }

    @Test
    public void getTransactionDetails_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    public void getTransactionDetails_Failure_AllowedAccountDataHasError() {
        // Given
        when(getTransactionDetailsValidator.validate(new CommonAccountTransactionsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void getTransactionDetails_Failure_SpiResponseHasError() {
        // Given
        doNothing().when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(spiTransaction));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiTransaction), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    public void getTransactionDetails_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getTransactionDetailsValidator.validate(new CommonAccountTransactionsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void getTransactionDetails_Success() {
        // Given
        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));

        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Transactions body = actualResponse.getBody();

        assertThat(body).isEqualTo(transactions);
    }

    @Test
    public void getTransactionDetails_Success_ShouldRecordEvent() {
        // Given
        doNothing().when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));
        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);
    }

    @Test
    public void getTransactionDetails_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        verify(getTransactionDetailsValidator).validate(new CommonAccountTransactionsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorServiceNotSupportedSpiResponse() {
        return SpiResponse.<T>builder()
                   .payload((T) SPI_TRANSACTION_REPORT)
                   .error(new TppMessage(SERVICE_NOT_SUPPORTED))
                   .build();
    }

    private void assertThatErrorIs(ResponseObject actualResponse, MessageErrorCode messageErrorCode) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(messageErrorCode);
    }

    private void assertResponseHasNoErrors(ResponseObject actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private static AccountConsent createConsent(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap());
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static Xs2aAccountAccess createAccountAccess() {
        return new Xs2aAccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null, null, null, null);
    }

    private static SpiAccountReference buildSpiAccountReferenceGlobal() {
        return new SpiAccountReference(ACCOUNT_ID, null, null, null, null, null, null);
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    // Needed because SpiTransactionReport is final, so it's impossible to mock it
    private static SpiTransactionReport buildSpiTransactionReport() {
        return new SpiTransactionReport(BASE64_STRING_EXAMPLE, Collections.emptyList(), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null);
    }

    @NotNull
    private TransactionsReportByPeriodObject buildTransactionsReportByPeriodObject() {
        return new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS);
    }

    private static SpiContextData buildSpiContextData() {
        return new SpiContextData(new SpiPsuData(null, null, null, null, null), new TppInfo(), UUID.randomUUID(), UUID.randomUUID());
    }

    @NotNull
    private static Xs2aTransactionsReportByPeriodRequest buildXs2aTransactionsReportByPeriodRequest() {
        return new Xs2aTransactionsReportByPeriodRequest(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST);
    }

    @NotNull
    private static MessageError buildMessageError() {
        return new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    }

    @NotNull
    private static SpiAccountConsent buildSpiAccountConsent() {
        return new SpiAccountConsent();
    }
}
