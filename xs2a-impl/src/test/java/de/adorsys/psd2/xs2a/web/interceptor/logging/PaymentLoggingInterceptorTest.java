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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.X_REQUEST_ID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentLoggingInterceptorTest {
    private static final String TPP_IP = "1.1.1.1";
    private static final String TPP_INFO_JSON = "json/web/interceptor/logging/tpp-info.json";
    private static final String REQUEST_URI = "request_uri";
    private static final String X_REQUEST_ID_HEADER_VALUE = "222";
    private static final String REDIRECT_ID = "redirect-id";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final byte[] CONTENT = new byte[]{1, 2, 3};
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("b571c834-4eb1-468f-91b0-f5e83589bc22");

    private PaymentLoggingInterceptor interceptor;
    @Mock
    private TppService tppService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private MultiReadHttpServletResponse response;
    @Mock
    private RedirectIdService redirectIdService;
    @Mock
    private RequestProviderService requestProviderService;
    @Spy
    private Xs2aObjectMapper objectMapper = new Xs2aObjectMapper();

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        when(tppService.getTppInfo()).thenReturn(jsonReader.getObjectFromFile(TPP_INFO_JSON, TppInfo.class));
        when(request.getHeader(X_REQUEST_ID)).thenReturn(X_REQUEST_ID_HEADER_VALUE);
        when(requestProviderService.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);
        when(response.getCachedContent()).thenReturn(CONTENT);
        interceptor = new PaymentLoggingInterceptor(tppService, redirectIdService, requestProviderService, objectMapper);
    }

    @Test
    public void preHandle_pathVariableIsNull() {
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(request).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verify(tppService).getTppInfo();
        verify(requestProviderService).getInternalRequestId();
        verify(request).getHeader(eq(X_REQUEST_ID));
        verify(request).getRemoteAddr();
        verify(request).getRequestURI();
    }

    @Test
    public void preHandle_success() {
        Map<Object, Object> pathVariables = new HashMap<>();
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariables);
        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(request).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verify(tppService).getTppInfo();
        verify(requestProviderService).getInternalRequestId();
        verify(request).getHeader(eq(X_REQUEST_ID));
        verify(request).getRemoteAddr();
        verify(request).getRequestURI();
    }

    @Test
    public void postHandle_emptyContent() throws IOException {
        when(response.getCachedContent()).thenReturn(null);

        interceptor.postHandle(request, response, null, null);

        verify(response).getCachedContent();
        verify(objectMapper, times(0)).readValue((byte[]) null, Map.class);
    }

    @Test
    public void postHandle_objectMapper_failed() throws IOException {
        byte[] bytes = jsonReader.getBytesFromFile("json/web/interceptor/logging/wrong-response.json");
        when(response.getCachedContent()).thenReturn(bytes);

        interceptor.postHandle(request, response, null, null);

        verify(response).getCachedContent();
        verify(objectMapper).readValue(bytes, Map.class);
    }

    @Test
    public void postHandle_successful() throws IOException {
        byte[] bytes = jsonReader.getBytesFromFile("json/web/interceptor/logging/transactionStatus-response.json");
        when(response.getCachedContent()).thenReturn(bytes);

        interceptor.postHandle(request, response, null, null);

        verify(response).getCachedContent();
        verify(objectMapper).readValue(bytes, Map.class);
    }

    @Test
    public void afterCompletion() {
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        when(redirectIdService.getRedirectId()).thenReturn(REDIRECT_ID);

        interceptor.afterCompletion(request, response, null, null);

        verify(tppService).getTppInfo();
        verify(requestProviderService).getInternalRequestId();
        verify(response).getHeader(eq(X_REQUEST_ID));
        verify(response).getStatus();
        verify(redirectIdService).getRedirectId();
    }

    private Map<String, String> buildMap() {
        Map<String, String> map = new HashMap<>();
        map.put("transactionStatus", TRANSACTION_STATUS.name());
        map.put("scaStatus", SCA_STATUS.name());
        return map;
    }
}
