package adaevomodel.designtime.instrumentation.mapping.bridge;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import adaevomodel.base.vsum.domains.java.IJavaPCMCorrespondenceModel;
import adaevomodel.designtime.instrumentation.project.ParsedApplicationProject;

public interface IEMFSubMappingImporter {

	public boolean targets(List<EObject> a, List<EObject> b);

	public void process(IJavaPCMCorrespondenceModel container, ParsedApplicationProject project, List<EObject> a,
			List<EObject> b);

}
