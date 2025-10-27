/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.protocol.dsp.http.spi.message;

import jakarta.json.JsonObject;
import org.eclipse.edc.spi.types.domain.message.ErrorMessage;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;

/**
 * Defines an incoming DSP message as a remote message type.
 */
public class PostDspRequest<I extends RemoteMessage, R, E extends ErrorMessage> extends DspRequest<I, R, E> {

    private JsonObject message;
    private String processId;
    private String expectedMessageType;

    private PostDspRequest(Class<I> messageClass, Class<R> resultClass, Class<E> errorClass) {
        super(messageClass, resultClass, errorClass);
    }

    public JsonObject getMessage() {
        return message;
    }

    public String getProcessId() {
        return processId;
    }

    public String getExpectedMessageType() {
        return expectedMessageType;
    }

    public static class Builder<I extends RemoteMessage, R, E extends ErrorMessage> extends DspRequest.Builder<I, R, PostDspRequest<I, R, E>, E, Builder<I, R, E>> {

        private Builder(Class<I> inputClass, Class<R> resultClass, Class<E> errorClass) {
            super(new PostDspRequest<>(inputClass, resultClass, errorClass));
        }

        public static <I extends RemoteMessage, R, E extends ErrorMessage> Builder<I, R, E> newInstance(Class<I> inputClass, Class<R> resultClass, Class<E> errorClass) {
            return new Builder<>(inputClass, resultClass, errorClass);
        }

        public Builder<I, R, E> message(JsonObject message) {
            super.message.message = message;
            return this;
        }

        public Builder<I, R, E> processId(String processId) {
            super.message.processId = processId;
            return this;
        }

        public Builder<I, R, E> expectedMessageType(String expectedMessageType) {
            super.message.expectedMessageType = expectedMessageType;
            return this;
        }

        @Override
        protected Builder<I, R, E> self() {
            return this;
        }
    }
}
