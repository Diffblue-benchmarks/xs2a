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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Additional account information 
 */
@ApiModel(description = "Additional account information ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-09-19T11:57:34.922302+03:00[Europe/Kiev]")

public class AdditionalInformationAccess   {
  @JsonProperty("ownerName")
  @Valid
  private List<AccountReference> ownerName = null;

  @JsonProperty("ownerAddress")
  @Valid
  private List<AccountReference> ownerAddress = null;

  public AdditionalInformationAccess ownerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  public AdditionalInformationAccess addOwnerNameItem(AccountReference ownerNameItem) {
    if (this.ownerName == null) {
      this.ownerName = new ArrayList<>();
    }
    this.ownerName.add(ownerNameItem);
    return this;
  }

  /**
   * Is asking for account owner name of the accounts referenced within. 
   * @return ownerName
  **/
  @ApiModelProperty(value = "Is asking for account owner name of the accounts referenced within. ")

  @Valid


  @JsonProperty("ownerName")
  public List<AccountReference> getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
  }

  public AdditionalInformationAccess ownerAddress(List<AccountReference> ownerAddress) {
    this.ownerAddress = ownerAddress;
    return this;
  }

  public AdditionalInformationAccess addOwnerAddressItem(AccountReference ownerAddressItem) {
    if (this.ownerAddress == null) {
      this.ownerAddress = new ArrayList<>();
    }
    this.ownerAddress.add(ownerAddressItem);
    return this;
  }

  /**
   * Is asking for account owner address related to the accounts referenced within 
   * @return ownerAddress
  **/
  @ApiModelProperty(value = "Is asking for account owner address related to the accounts referenced within ")

  @Valid


  @JsonProperty("ownerAddress")
  public List<AccountReference> getOwnerAddress() {
    return ownerAddress;
  }

  public void setOwnerAddress(List<AccountReference> ownerAddress) {
    this.ownerAddress = ownerAddress;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalInformationAccess additionalInformationAccess = (AdditionalInformationAccess) o;
    return Objects.equals(this.ownerName, additionalInformationAccess.ownerName) &&
        Objects.equals(this.ownerAddress, additionalInformationAccess.ownerAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerName, ownerAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalInformationAccess {\n");
    
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    ownerAddress: ").append(toIndentedString(ownerAddress)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

