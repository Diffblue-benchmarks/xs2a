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

package de.adorsys.psd2.xs2a.service.validator.ais.account.dto;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import lombok.Value;

import java.util.List;

@Value
public class TransactionsReportByPeriodObject implements TppInfoProvider {
    private AccountConsent accountConsent;
    private String accountId;
    private boolean withBalance;
    private String requestUri;
    private String entryReferenceFrom;
    private Boolean deltaList;
    private String acceptHeader;
    private BookingStatus bookingStatus;

    @Override
    public TppInfo getTppInfo() {
        return accountConsent.getTppInfo();
    }

    public List<AccountReference> getTransactions() {
        return accountConsent.getAspspAccess().getTransactions();
    }
}
