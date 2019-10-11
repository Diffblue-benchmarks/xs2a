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
import de.adorsys.psd2.xs2a.component.logger.TppLogger;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentLoggingInterceptor extends HandlerInterceptorAdapter {
    private static final String NOT_EXIST_IN_URI = "Not exist in URI";
    private final TppService tppService;
    private final RedirectIdService redirectIdService;
    private final RequestProviderService requestProviderService;
    private final Xs2aObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, String> pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String paymentId = Optional.ofNullable(pathVariables)
                               .map(vr -> vr.get("paymentId"))
                               .orElse(NOT_EXIST_IN_URI);

        TppLogger.logRequest(request)
            .withTpp(tppService.getTppInfo())
            .withInternalRequestId(requestProviderService.getInternalRequestId())
            .withXRequestId()
            .withRequestUri()
            .withParam("Payment ID", paymentId)
            .perform();

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
        MultiReadHttpServletResponse multiReadHttpServletResponse = (MultiReadHttpServletResponse) response;
        byte[] content = multiReadHttpServletResponse.getCachedContent();

        if (ArrayUtils.isEmpty(content)) {
            return;
        }

        Map<String, String> responseVariables;
        try {
            responseVariables = objectMapper.readValue(content, Map.class);
        } catch (IOException exception) {
            return;
        }

        String transactionStatus = responseVariables.get("transactionStatus");
        String scaStatus = responseVariables.get("scaStatus");

        TppLogger.logResponse(response)
            .withParam("transactionStatus", transactionStatus)
            .withParam("scaStatus", scaStatus)
            .perform();

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TppLogger.logResponse(response)
            .withTpp(tppService.getTppInfo())
            .withInternalRequestId(requestProviderService.getInternalRequestId())
            .withXRequestId()
            .withResponseStatus()
            .withOptionalRedirectId(redirectIdService.getRedirectId())
            .perform();
    }
}
