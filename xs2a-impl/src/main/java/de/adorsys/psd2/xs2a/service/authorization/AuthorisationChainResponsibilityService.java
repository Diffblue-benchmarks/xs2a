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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.model.ScaStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.authorization.processor.*;

public class AuthorisationChainResponsibilityService {

    private final AuthorisationProcessor receivedAuthorisationProcessor;

    public AuthorisationChainResponsibilityService() {
        receivedAuthorisationProcessor = new ReceivedAuthorisationProcessor();
        AuthorisationProcessor psuIdentifiedAuthorisationProcessor = new PsuIdentifiedAuthorisationProcessor();
        AuthorisationProcessor psuAuthenticatedAuthorisationProcessor = new PsuAuthenticatedAuthorisationProcessor();
        AuthorisationProcessor scaMethodSelectedAuthorisationProcessor = new ScaMethodSelectedAuthorisationProcessor();
        AuthorisationProcessor startedAuthorisationProcessor = new StartedAuthorisationProcessor();
        AuthorisationProcessor finalisedAuthorisationProcessor = new FinalisedAuthorisationProcessor();
        AuthorisationProcessor failedAuthorisationProcessor = new FailedAuthorisationProcessor();
        AuthorisationProcessor exemptedAuthorisationProcessor = new ExemptedAuthorisationProcessor();

        receivedAuthorisationProcessor.setNext(psuIdentifiedAuthorisationProcessor);
        psuIdentifiedAuthorisationProcessor.setNext(psuAuthenticatedAuthorisationProcessor);
        psuAuthenticatedAuthorisationProcessor.setNext(scaMethodSelectedAuthorisationProcessor);
        scaMethodSelectedAuthorisationProcessor.setNext(startedAuthorisationProcessor);
        startedAuthorisationProcessor.setNext(finalisedAuthorisationProcessor);
        finalisedAuthorisationProcessor.setNext(failedAuthorisationProcessor);
        failedAuthorisationProcessor.setNext(exemptedAuthorisationProcessor);
    }

    public void process(AuthorisationProcessorRequest request) {
        receivedAuthorisationProcessor.process(request);
    }

    public static void main(String[] args) {
        AuthorisationChainResponsibilityService chainResponsibility = new AuthorisationChainResponsibilityService();
        chainResponsibility.process(new AuthorisationProcessorRequest(ScaApproach.EMBEDDED, ScaStatus.SCAMETHODSELECTED, null));
    }
}
