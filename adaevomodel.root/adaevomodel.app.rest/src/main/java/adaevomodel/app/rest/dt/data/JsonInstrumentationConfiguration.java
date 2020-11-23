package adaevomodel.app.rest.dt.data;

import adaevomodel.designtime.instrumentation.project.app.InstrumentationMetadata;
import lombok.Data;

@Data
public class JsonInstrumentationConfiguration {

	private boolean extractMappingFromCode;
	private InstrumentationMetadata metadata;

}
