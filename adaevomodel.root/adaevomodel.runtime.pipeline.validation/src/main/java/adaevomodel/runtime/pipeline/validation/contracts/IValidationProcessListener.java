package adaevomodel.runtime.pipeline.validation.contracts;

import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.runtime.pipeline.validation.data.ValidationState;

public interface IValidationProcessListener {

	public void stateChanged(ValidationState state);

	public void validationFinished(ValidationData data);

}
