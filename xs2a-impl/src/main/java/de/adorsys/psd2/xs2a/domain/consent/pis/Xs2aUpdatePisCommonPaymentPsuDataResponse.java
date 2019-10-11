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

package de.adorsys.psd2.xs2a.domain.consent.pis;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
public class Xs2aUpdatePisCommonPaymentPsuDataResponse implements AuthorisationResponse, CancellationAuthorisationResponse {

    private String paymentId;
    private String authorisationId;

    private ScaStatus scaStatus;
    private List<Xs2aAuthenticationObject> availableScaMethods;
    private Xs2aAuthenticationObject chosenScaMethod;
    private ChallengeData challengeData;
    private Links links = new Links();
    private String psuMessage;
    private PsuIdData psuData;

    private ErrorHolder errorHolder;

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, String paymentId, String authorisationId, PsuIdData psuData) {
        this.scaStatus = scaStatus;
        this.paymentId = paymentId;
        this.authorisationId = authorisationId;
        this.psuData = psuData;
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ErrorHolder errorHolder, String paymentId, String authorisationId, PsuIdData psuData) {
        this(ScaStatus.FAILED, paymentId, authorisationId, psuData);
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }

    /**
     * Returns chosenScaMethod. Should be used ONLY for mapping to PSD2 response.
     *
     * @return chosenScaMethod
     */
    public Xs2aAuthenticationObject getChosenScaMethodForPsd2Response() {
        return getChosenScaMethod();
    }

    @NotNull
    @Override
    public String getCancellationId() {
        return authorisationId;
    }

    @NotNull
    @Override
    public AuthorisationResponseType getAuthorisationResponseType() {
        return AuthorisationResponseType.UPDATE;
    }
}

