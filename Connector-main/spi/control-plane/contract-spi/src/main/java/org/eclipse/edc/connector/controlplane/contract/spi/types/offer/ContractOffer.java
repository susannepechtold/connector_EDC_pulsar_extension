/*
 *  Copyright (c) 2021 Daimler TSS GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Daimler TSS GmbH - Initial API and Implementation
 *
 */

package org.eclipse.edc.connector.controlplane.contract.spi.types.offer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.edc.policy.model.Policy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A contract offer is exchanged between two participant agents. It describes the which assets the consumer may use, and
 * the rules and policies that apply to each asset.
 */
@JsonDeserialize(builder = ContractOffer.Builder.class)
public class ContractOffer {
    private String id;

    /**
     * The policy that describes the usage conditions of the assets
     */
    private Policy policy;

    /**
     * The offered asset
     */
    private String assetId;

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getAssetId() {
        return assetId;
    }

    @NotNull
    public Policy getPolicy() {
        return policy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, policy, assetId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContractOffer that = (ContractOffer) o;
        return Objects.equals(id, that.id) && Objects.equals(policy, that.policy) && Objects.equals(assetId, that.assetId);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private final ContractOffer contractOffer;

        private Builder() {
            contractOffer = new ContractOffer();
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(String id) {
            contractOffer.id = id;
            return this;
        }

        public Builder assetId(String assetId) {
            contractOffer.assetId = assetId;
            return this;
        }

        public Builder policy(Policy policy) {
            contractOffer.policy = policy;
            return this;
        }

        public ContractOffer build() {
            Objects.requireNonNull(contractOffer.id, "Id must not be null");
            Objects.requireNonNull(contractOffer.assetId, "Asset id must not be null");
            Objects.requireNonNull(contractOffer.policy, "Policy must not be null");
            return contractOffer;
        }
    }
}
