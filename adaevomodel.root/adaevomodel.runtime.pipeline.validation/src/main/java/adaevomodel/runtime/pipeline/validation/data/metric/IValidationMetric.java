package adaevomodel.runtime.pipeline.validation.data.metric;

import adaevomodel.runtime.pipeline.validation.data.ValidationMetricValue;
import adaevomodel.runtime.pipeline.validation.data.ValidationPoint;

public interface IValidationMetric<T extends ValidationMetricValue> {

	public boolean isTarget(ValidationPoint point);

	public T calculate(ValidationPoint point);

}
