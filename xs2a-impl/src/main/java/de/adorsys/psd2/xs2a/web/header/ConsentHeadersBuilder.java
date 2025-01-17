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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsentHeadersBuilder {
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Builds response headers for successful create consent request
     *
     * @param authorisationId id of the authorisation, if it was created implicitly
     * @param selfLink        link to the newly created consent
     * @return response headers
     */
    public ResponseHeaders buildCreateConsentHeaders(@Nullable String authorisationId, @NotNull String selfLink) {
        if (authorisationId == null) {
            return ResponseHeaders.builder()
                       .location(selfLink)
                       .build();
        }

        ScaApproach scaApproach = scaApproachResolver.getInitiationScaApproach(authorisationId);
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .location(selfLink)
                   .build();
    }

    /**
     * Builds response headers for successful start consent authorisation request
     *
     * @param authorisationId id of the created authorisation
     * @return response headers
     */
    public ResponseHeaders buildStartConsentAuthorisationHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    /**
     * Builds response headers for successful update consents PSU Data request
     *
     * @param authorisationId id of the authorisation, used in the request
     * @return response headers
     */
    public ResponseHeaders buildUpdateConsentsPsuDataHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    private ResponseHeaders buildHeadersForExistingAuthorisation(String authorisationId) {
        ScaApproach authorisationScaApproach = scaApproachResolver.getInitiationScaApproach(authorisationId);
        return buildScaApproachHeader(authorisationScaApproach);
    }

    private ResponseHeaders buildScaApproachHeader(ScaApproach scaApproach) {
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .build();
    }
}
