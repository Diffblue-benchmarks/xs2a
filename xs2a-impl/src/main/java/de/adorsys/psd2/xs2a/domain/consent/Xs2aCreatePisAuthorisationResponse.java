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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Xs2aCreatePisAuthorisationResponse implements AuthorisationResponse {
    private String authorisationId;
    private ScaStatus scaStatus;
    private PaymentType paymentType;
    private Links links = new Links();
    private String internalRequestId;

    public Xs2aCreatePisAuthorisationResponse(String authorisationId, ScaStatus scaStatus, PaymentType paymentType,
                                              String internalRequestId) {
        this.authorisationId = authorisationId;
        this.scaStatus = scaStatus;
        this.paymentType = paymentType;
        this.internalRequestId = internalRequestId;
    }
}
