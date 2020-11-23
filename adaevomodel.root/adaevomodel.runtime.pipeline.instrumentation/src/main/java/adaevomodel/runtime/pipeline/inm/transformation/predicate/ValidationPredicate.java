package adaevomodel.runtime.pipeline.inm.transformation.predicate;

import adaevomodel.runtime.pipeline.validation.data.ValidationPoint;

public interface ValidationPredicate {

	boolean satisfied(ValidationPoint validationPoint);

}
