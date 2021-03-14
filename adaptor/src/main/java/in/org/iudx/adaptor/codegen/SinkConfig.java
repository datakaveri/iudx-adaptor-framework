package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

/**
 * {@link SinkConfig} - SinkConfig
 * Marker interface
 * Specifies a method to get the specific config for a particular sink
 *
 * TODO: 
 *  - Extend as needed
 *
 */
public interface SinkConfig<SinkConfigurationType> extends Serializable {
  SinkConfigurationType getConfig();
}
