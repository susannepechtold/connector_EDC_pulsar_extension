/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.policy.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A permission, prohibition, or duty contained in a {@link Policy}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "edctype")
public abstract class Rule {

    public interface Visitor<R> {
        R visitPermission(Permission policy);

        R visitProhibition(Prohibition policy);

        R visitDuty(Duty policy);
    }

    protected Action action;

    protected List<Constraint> constraints = new ArrayList<>();

    public Action getAction() {
        return action;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public abstract <R> R accept(Visitor<R> visitor);

    @SuppressWarnings("unchecked")
    protected abstract static class Builder<T extends Rule, B extends Builder<T, B>> {
        protected T rule;

        public B action(Action action) {
            rule.action = action;
            return (B) this;
        }

        public B constraint(Constraint constraint) {
            rule.constraints.add(constraint);
            return (B) this;
        }

        public B constraints(List<Constraint> constraints) {
            rule.constraints.addAll(constraints);
            return (B) this;
        }

    }

}
