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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.component.DayOfExecutionDeserializer;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperXs2a {
    private final ValueValidatorService validationService;
    private final HttpServletRequest httpServletRequest;
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final PaymentModelMapper paymentModelMapper;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;

    public Object mapToXs2aPayment(Object payment, PaymentInitiationParameters requestParameters) {

        if (standardPaymentProductsResolver.isRawPaymentProduct(requestParameters.getPaymentProduct())) {
            return buildBinaryBodyData(httpServletRequest);
        }

        if (requestParameters.getPaymentType() == SINGLE) {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, PaymentInitiationJson.class));
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, PeriodicPaymentInitiationJson.class));
        } else {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, BulkPaymentInitiationJson.class));
        }
    }

    public Object mapToXs2aRawPayment(PaymentInitiationParameters requestParameters, Object xmlSct, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType) {
        if (requestParameters.getPaymentType() == PERIODIC) {
            return buildPeriodicBinaryBodyData(xmlSct, jsonStandingOrderType);
        }

        return buildBinaryBodyData(httpServletRequest);
    }

    private <R> R validatePayment(Object payment, Class<R> clazz) {
        ObjectMapper customMapper = xs2aObjectMapper.copy();
        customMapper.registerModule(getDayOfExecutionDeserializerModule());
        R result = customMapper.convertValue(payment, clazz);
        validationService.validate(result);
        return result;
    }

    private byte[] buildBinaryBodyData(HttpServletRequest httpServletRequest) {
        try {
            return IOUtils.toByteArray(httpServletRequest.getInputStream());
        } catch (IOException e) {
            log.warn("Cannot deserialize httpServletRequest body!", e);
        }

        return null;
    }

    private byte[] buildPeriodicBinaryBodyData(Object xmlPart, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonPart) {
        String serialisedJsonPart = null;
        try {
            serialisedJsonPart = xs2aObjectMapper.writeValueAsString(jsonPart);
        } catch (JsonProcessingException e) {
            log.info("Can't convert object to json: {}", e.getMessage());
        }
        if (xmlPart == null || serialisedJsonPart == null) {
            throw new IllegalArgumentException("Invalid body of the multipart request!");
        }

        String body = xmlPart + "\n" + serialisedJsonPart;
        return body.getBytes(Charset.forName("UTF-8"));
    }

    private SimpleModule getDayOfExecutionDeserializerModule() {
        SimpleModule dayOfExecutionModule = new SimpleModule();
        dayOfExecutionModule.addDeserializer(DayOfExecution.class, new DayOfExecutionDeserializer());
        return dayOfExecutionModule;
    }
}
