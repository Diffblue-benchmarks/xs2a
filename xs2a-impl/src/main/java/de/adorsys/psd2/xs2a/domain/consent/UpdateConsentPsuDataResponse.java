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

import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import lombok.Data;

import java.util.List;

// Class can't be immutable, because it it used in aspect (links setting)
@Data
public class UpdateConsentPsuDataResponse implements AuthorisationResponse {

    private String consentId;
    private String authorisationId;

    private ScaStatus scaStatus;
    private List<Xs2aAuthenticationObject> availableScaMethods;
    private Xs2aAuthenticationObject chosenScaMethod;
    private ChallengeData challengeData;
    private String authenticationMethodId;
    private String scaAuthenticationData;
    private Links links;
    private String psuMessage;
    private String internalRequestId;

    private MessageError messageError;

    public UpdateConsentPsuDataResponse(ScaStatus scaStatus, String consentId, String authorisationId) {
        this.scaStatus = scaStatus;
        this.consentId = consentId;
        this.authorisationId = authorisationId;
    }

    public boolean hasError() {
        return messageError != null;
    }

    /**
     * Returns chosenScaMethod. Should be used ONLY for mapping to PSD2 response.
     *
     * @return chosenScaMethod
     */
    public Xs2aAuthenticationObject getChosenScaMethodForPsd2Response() {
        return getChosenScaMethod();
    }
}
