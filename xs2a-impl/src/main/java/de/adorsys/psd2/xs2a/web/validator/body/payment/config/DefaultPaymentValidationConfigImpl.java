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

package de.adorsys.psd2.xs2a.web.validator.body.payment.config;

import lombok.Data;

@Data
public class DefaultPaymentValidationConfigImpl implements PaymentValidationConfig {
    protected ValidationObject endToEndIdentification = new ValidationObject(35);
    protected ValidationObject ultimateDebtor = new ValidationObject(70);
    protected ValidationObject ultimateCreditor = new ValidationObject(70);
    protected ValidationObject creditorName = new ValidationObject(Occurrence.REQUIRED, 70);

    //address
    protected ValidationObject streetName = new ValidationObject(100);
    protected ValidationObject buildingNumber = new ValidationObject(20);
    protected ValidationObject townName = new ValidationObject(100);
    protected ValidationObject postCode = new ValidationObject(5);

    //account reference
    protected ValidationObject pan = new ValidationObject(35);
    protected ValidationObject maskedPan = new ValidationObject(35);
    protected ValidationObject msisdn = new ValidationObject(35);

    //remittance
    protected ValidationObject reference = new ValidationObject(Occurrence.REQUIRED, 35);
    protected ValidationObject referenceType = new ValidationObject(35);
    protected ValidationObject referenceIssuer = new ValidationObject(35);

    protected ValidationObject executionRule = new ValidationObject(Occurrence.REQUIRED, 140);
    protected ValidationObject creditorId = new ValidationObject(Occurrence.NONE, 0);
}
