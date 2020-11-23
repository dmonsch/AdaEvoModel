package adaevomodel.runtime.pipeline.blackboard.facade;

import adaevomodel.base.core.facade.IResettableQueryFacade;
import adaevomodel.base.core.state.ValidationSchedulePoint;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;

public interface IValidationResultsQuery extends IResettableQueryFacade {
	ValidationData get(ValidationSchedulePoint point);
}
