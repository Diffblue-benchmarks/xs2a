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

package de.adorsys.psd2.xs2a.web.validator.body.payment.type;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;


@Component
public class BulkPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {

    @Autowired
    public BulkPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                        PaymentMapper paymentMapper, AmountValidator amountValidator) {
        super(errorBuildingService, xs2aObjectMapper, paymentMapper, amountValidator);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }

    @Override
    public void validate(Object body, MessageError messageError, PaymentValidationConfig validationConfig) {
        try {
            doBulkValidation(paymentMapper.getBulkPayment(body), messageError, validationConfig);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
            }
        }
    }

    void doBulkValidation(BulkPayment bulkPayment, MessageError messageError, PaymentValidationConfig validationConfig) {

        if (Objects.isNull(bulkPayment.getDebtorAccount())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "debtorAccount"));
        } else {
            validateAccount(bulkPayment.getDebtorAccount(), messageError, validationConfig);
        }

        List<SinglePayment> payments = bulkPayment.getPayments();

        if (CollectionUtils.isEmpty(payments)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_BULK));
        } else {
            payments.forEach(singlePayment -> super.doSingleValidation(singlePayment, messageError, validationConfig));
        }

        if (isDateInThePast(bulkPayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(
                messageError, TppMessageInformation.of(MessageErrorCode.EXECUTION_DATE_INVALID_IN_THE_PAST));
        }
    }
}
