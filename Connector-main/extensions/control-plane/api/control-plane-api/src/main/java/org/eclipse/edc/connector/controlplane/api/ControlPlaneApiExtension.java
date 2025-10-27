/*
 *  Copyright (c) 2022 Microsoft Corporation
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

package org.eclipse.edc.connector.controlplane.api;

import org.eclipse.edc.connector.controlplane.api.transferprocess.TransferProcessControlApiController;
import org.eclipse.edc.connector.controlplane.api.transferprocess.model.TransferProcessFailStateDto;
import org.eclipse.edc.connector.controlplane.services.spi.transferprocess.TransferProcessService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.ApiContext;

/**
 * {@link ControlPlaneApiExtension } exposes HTTP endpoints for internal interaction with the Control Plane
 */
@Extension(value = ControlPlaneApiExtension.NAME)
public class ControlPlaneApiExtension implements ServiceExtension {

    public static final String NAME = "Control Plane API";

    @Inject
    private WebService webService;

    @Inject
    private TransferProcessService transferProcessService;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        typeManager.registerTypes(TransferProcessFailStateDto.class);

        webService.registerResource(ApiContext.CONTROL, new TransferProcessControlApiController(transferProcessService));
    }

}
