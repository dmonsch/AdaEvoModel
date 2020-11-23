package adaevomodel.designtime.instrumentation.transformation;

import adaevomodel.base.vsum.domains.java.IJavaPCMCorrespondenceModel;
import adaevomodel.designtime.instrumentation.project.ParsedApplicationProject;

public interface IApplicationProjectInstrumenter {

	public void transform(ParsedApplicationProject pap, IJavaPCMCorrespondenceModel correspondence);

}
