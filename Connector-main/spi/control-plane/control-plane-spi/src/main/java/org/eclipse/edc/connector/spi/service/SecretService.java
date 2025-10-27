/*
 *  Copyright (c) 2024 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.connector.spi.service;

import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.types.domain.secret.Secret;

public interface SecretService {

    /**
     * Returns an secret by its id
     *
     * @param secretId id of the secret
     * @return the secret, null if it's not found
     */
    Secret findById(String secretId);

    /**
     * Create a secret
     *
     * @param secret the secret
     * @return successful result if the secret is created correctly, failure otherwise
     */
    ServiceResult<Secret> create(Secret secret);

    /**
     * Delete a secret
     *
     * @param secretId the id of the secret to be deleted
     * @return successful result if the secret is deleted correctly, failure otherwise
     */
    ServiceResult<Secret> delete(String secretId);

    /**
     * Updates a secret. If the secret does not yet exist, {@link ServiceResult#notFound(String)} will be returned.
     *
     * @param secret The content of the Secret. Note that {@link Secret#getId()} will be ignored, rather the separately supplied ID is used
     * @return successful if updated, a failure otherwise.
     */
    ServiceResult<Secret> update(Secret secret);
}