/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 */

package org.eclipse.edc.boot.system;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.eclipse.edc.boot.monitor.MultiplexingMonitor;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.system.MonitorExtension;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.telemetry.Telemetry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.edc.spi.monitor.ConsoleMonitor.COLOR_PROG_ARG;

public class ExtensionLoader {

    private final ServiceLocator serviceLocator;

    public ExtensionLoader(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public static @NotNull Telemetry loadTelemetry() {
        var loader = ServiceLoader.load(OpenTelemetry.class);
        var openTelemetries = loader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        return new Telemetry(selectOpenTelemetryImpl(openTelemetries));
    }

    static @NotNull OpenTelemetry selectOpenTelemetryImpl(List<OpenTelemetry> openTelemetries) {
        if (openTelemetries.size() > 1) {
            throw new IllegalStateException(String.format("Found %s OpenTelemetry implementations. Please provide only one OpenTelemetry service provider.", openTelemetries.size()));
        }
        return openTelemetries.isEmpty() ? GlobalOpenTelemetry.get() : openTelemetries.get(0);
    }

    /**
     * Load the monitor instance.
     *
     * @param programArgs program args.
     * @return the monitor instance.
     */
    public Monitor loadMonitor(String... programArgs) {
        var extensions = serviceLocator.loadImplementors(MonitorExtension.class, false);

        return switch (extensions.size()) {
            case 0 -> {
                var parseResult = parseLogLevel(programArgs);
                if (parseResult.failed()) {
                    throw new EdcException(parseResult.getFailureDetail());
                }
                yield new ConsoleMonitor(parseResult.getContent(), !Set.of(programArgs).contains(COLOR_PROG_ARG));
            }
            case 1 -> extensions.get(0).getMonitor();
            default -> new MultiplexingMonitor(extensions.stream().map(MonitorExtension::getMonitor).toList());
        };
    }

    /**
     * Loads and orders the service extensions.
     */
    public DependencyGraph buildDependencyGraph(ServiceExtensionContext context) {
        var serviceExtensions = loadExtensions(ServiceExtension.class, true);
        return DependencyGraph.of(context, serviceExtensions);
    }

    /**
     * Loads multiple extensions, raising an exception if at least one is not found.
     */
    public <T> List<T> loadExtensions(Class<T> type, boolean required) {
        return serviceLocator.loadImplementors(type, required);
    }

    /**
     * Parses the ConsoleMonitor log level from the program args. If no log level is provided, defaults to Level default.
     */
    private static Result<ConsoleMonitor.Level> parseLogLevel(String[] programArgs) {
        return Stream.of(programArgs)
                .filter(arg -> arg.startsWith(ConsoleMonitor.LEVEL_PROG_ARG))
                .map(arg -> {
                    var validValueMessage = String.format("Valid values for the console level are %s", Stream.of(ConsoleMonitor.Level.values()).toList());
                    var splitArgs = arg.split("=");
                    if (splitArgs.length != 2) {
                        return Result.<ConsoleMonitor.Level>failure(String.format("Value missing for the --log-level argument. %s", validValueMessage));
                    }
                    try {
                        return Result.success(ConsoleMonitor.Level.valueOf(splitArgs[1].toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return Result.<ConsoleMonitor.Level>failure(String.format("Invalid value \"%s\" for the --log-level argument. %s", splitArgs[1], validValueMessage));
                    }
                })
                .findFirst()
                .orElse(Result.success(ConsoleMonitor.Level.getDefaultLevel()));
    }

}
