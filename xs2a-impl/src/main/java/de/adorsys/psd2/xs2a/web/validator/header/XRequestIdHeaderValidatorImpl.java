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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListDownloadHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_HEADER;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.X_REQUEST_ID;

@Component
public class XRequestIdHeaderValidatorImpl extends AbstractHeaderValidatorImpl
    implements ConsentHeaderValidator, PaymentHeaderValidator, TransactionListHeaderValidator, FundsConfirmationHeaderValidator,
                   CancelPaymentHeaderValidator, TransactionListDownloadHeaderValidator {

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\z";
    private static final Pattern PATTERN = Pattern.compile(UUID_REGEX, Pattern.CASE_INSENSITIVE);

    @Autowired
    public XRequestIdHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return X_REQUEST_ID;
    }

    @Override
    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        String header = headers.get(getHeaderName());
        if (isNonValid(header)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(),FORMAT_ERROR_WRONG_HEADER);
        }

        return super.checkHeaderContent(headers);
    }

    private boolean isNonValid(String xRequestId) {
        return !PATTERN.matcher(xRequestId).matches();
    }

}
