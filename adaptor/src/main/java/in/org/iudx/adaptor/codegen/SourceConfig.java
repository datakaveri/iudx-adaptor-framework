package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

/**
 * {@link SinkConfig} - SinkConfig
 * Marker interface
 * Specifies a method to get the specific config for a particular sink
 *
 * TODO: 
 *  - Use this
 *
 */
public interface SourceConfig<SourceConfigurationType> extends Serializable {
  SourceConfigurationType getConfig();
}
