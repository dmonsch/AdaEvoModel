package adaevomodel.app.start.spring.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import adaevomodel.base.core.IPcmModelProvider;
import adaevomodel.base.core.ISpecificModelProvider;
import adaevomodel.base.shared.json.JsonEObject;

@Service
public class TemplateHelper {
	@Autowired
	private IPcmModelProvider modelContainer;

	@Autowired
	private ISpecificModelProvider specificModels;

	public void addModelOverviewInformation(Model model) {
		// system
		model.addAttribute("system", JsonEObject.create(modelContainer.getSystem()));

		// repository
		model.addAttribute("repository", JsonEObject.create(modelContainer.getRepository()));

		// allocation
		model.addAttribute("allocation", JsonEObject.create(modelContainer.getAllocation()));

		// usage
		model.addAttribute("usage", JsonEObject.create(modelContainer.getUsage()));

		// resource environment
		model.addAttribute("resenv", JsonEObject.create(modelContainer.getResourceEnvironment()));

		// instrumentation model
		model.addAttribute("inm", JsonEObject.create(specificModels.getInstrumentation()));

		// runtime model
		model.addAttribute("rem", JsonEObject.create(specificModels.getRuntimeEnvironment()));

		// correspondences
		model.addAttribute("correspondences", JsonEObject.create(specificModels.getCorrespondences()));
	}

}
