package org.eclipse.edc.connector.dataplane.pulsar.pipeline;

import org.eclipse.edc.connector.dataplane.pulsar.config.PulsarPropertiesFactory;
// ...other imports...

public class PulsarDataSinkFactory implements DataSinkFactory {
    // ...existing code...
    @Override
    public DataSink create(DataAddress sink, DataFlowRequest request) {
        // ...existing code...
        return new PulsarDataSink(/* ... */);
    }
    // ...existing code...
}
