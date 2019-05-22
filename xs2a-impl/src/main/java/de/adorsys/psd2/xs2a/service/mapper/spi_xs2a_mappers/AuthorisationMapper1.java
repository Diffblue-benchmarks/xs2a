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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.web.mapper.CoreObjectsMapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import org.mapstruct.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
    uses= {HrefLinkMapper.class, CoreObjectsMapper.class},

    imports = {HrefLinkMapper.class, AuthenticationType.class})
//@DecoratedWith(AuthorisationMapperDecorator.class)
public interface AuthorisationMapper1 {
    static final String HREF = "href";


    @Mapping(target = "authenticationType", expression = "java( AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()) )")
//    @Mapping(target = "authenticationType", source = "xs2aAuthenticationObject", qualifiedByName = "mapToAuthenticationType")
    ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject);

    //    @Mapping(target = "authorisationIds", source = "xs2AAuthorisationSubResources.authorisationIds")
    Authorisations mapToAuthorisations(Xs2aAuthorisationSubResources xs2AAuthorisationSubResources);

//    @Mapping(target = "", source = "")
//    Object mapToPisCreateOrUpdateAuthorisationResponse(ResponseObject responseObject);
/*
    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "chosenScaMethod", ignore = true)
    @Mapping(target = "challengeData", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
//    @Mapping(target = "scaStatus", expression = "java( CoreObjectsMapper.mapToModelScaStatus(response.getScaStatus()))")
//    @Mapping(target = "_links", source = "response.links")
    @Mapping(target = "_links", resultType = Map.class, source = "response", qualifiedByName = "mapToLinks")
//    @Mapping(target = "links", ignore = true)
//    @Mapping(target = "_links", source = "links", resultType = Map.class)
//    @Mapping(target = "_links", source = "response", qualifiedByName = "mapToLinks")
    StartScaprocessResponse mapToStartScaProcessResponseFromPis(Xs2aCreatePisAuthorisationResponse response);

//    default Map mapToLinks(Xs2aCreatePisAuthorisationResponse response) {
//       return new HrefLinkMapper().mapToLinksMap(response.getLinks());
//    }

    default Map<String, Map<String, String>> mapToLinksMap(Xs2aCreatePisAuthorisationResponse response) {
        if (response.getLinks() == null) {
            return null;
        }

        Map<String, String> linksMap = new ObjectMapper().convertValue(response.getLinks(), new TypeReference<Map<String, String>>() {
        });
        return linksMap
                   .entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Collections.singletonMap(HREF, e.getValue())
            ));
    }
*/
    default String mapToAuthenticationType1(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        if (true) {

        }
        return AuthenticationType.fromValue(xs2aAuthenticationObject.getAuthenticationType()).toString();
    }

   @IterableMapping(elementTargetType = AuthenticationObject.class, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ScaMethods getAvailableScaMethods(List<Xs2aAuthenticationObject> availableScaMethods);

}
