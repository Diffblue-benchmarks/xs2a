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

package de.adorsys.psd2.event.service.mapper;

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Xs2aEventBOMapperImpl.class, Xs2aObjectMapper.class})
public class EventBOMapperTest {
    private static final String PAYLOAD = "payload";

    @Autowired
    private Xs2aEventBOMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
    private byte[] payloadAsBytes;

    @Before
    public void setUp() throws Exception {
        payloadAsBytes = xs2aObjectMapper.writeValueAsBytes(PAYLOAD);
    }

    @Test
    public void toEventPO() {
        EventBO eventBO = jsonReader.getObjectFromFile("json/event-bo.json", EventBO.class);

        EventPO actualEventPO = mapper.toEventPO(eventBO);

        EventPO expectedEventPO = jsonReader.getObjectFromFile("json/event-po.json", EventPO.class);
        expectedEventPO.setPayload(payloadAsBytes);

        assertEquals(expectedEventPO, actualEventPO);
    }

    @Test
    public void toEventPO_nullValue() {
        EventPO actualEventPO = mapper.toEventPO(null);
        assertNull(actualEventPO);
    }
}
