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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class RedirectLinkBuilderTest {
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";


    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @InjectMocks
    private RedirectLinkBuilder redirectLinkBuilder;

    @Test
    public void buildConsentScaRedirectLink() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else", redirectLink);
    }

    @Test
    public void buildPaymentScaRedirectLink() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    public void buildPaymentCancellationScaRedirectLink() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    public void buildConsentScaRedirectLinkWithInternalRequestId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    public void buildPaymentScaRedirectLinkWithInternalRequestId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    public void buildPaymentCancellationScaRedirectLinkWithInternalRequestId() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID);

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }
}
