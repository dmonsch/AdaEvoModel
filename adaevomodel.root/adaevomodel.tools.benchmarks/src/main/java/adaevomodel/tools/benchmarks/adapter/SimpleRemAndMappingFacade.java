package adaevomodel.tools.benchmarks.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.pcm.headless.api.util.PCMUtil;

import com.google.common.collect.Maps;

import adaevomodel.base.core.facade.IRuntimeEnvironmentQueryFacade;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.base.vsum.facade.ISpecificVsumFacade;
import adaevomodel.base.vsum.manager.VsumManager.VsumChangeSource;
import dmodel.base.models.inmodel.InstrumentationMetamodel.ServiceInstrumentationPoint;
import dmodel.base.models.runtimeenvironment.REModel.REModelFactory;
import dmodel.base.models.runtimeenvironment.REModel.RuntimeResourceContainer;
import dmodel.base.models.runtimeenvironment.REModel.RuntimeResourceContainerConnection;

public class SimpleRemAndMappingFacade implements IRuntimeEnvironmentQueryFacade, ISpecificVsumFacade {
	private Map<String, RuntimeResourceContainer> rrcMapping;
	private Map<RuntimeResourceContainer, ResourceContainer> rcMapping;

	public SimpleRemAndMappingFacade(List<String> mappings, InMemoryPCM pcm) {
		rcMapping = Maps.newHashMap();
		rrcMapping = Maps.newHashMap();

		mappings.forEach(m -> {
			String[] mpSplit = m.split(",");
			if (mpSplit.length == 2) {
				RuntimeResourceContainer rrc = REModelFactory.eINSTANCE.createRuntimeResourceContainer();
				rrc.setHostID(mpSplit[0]);
				rrcMapping.put(mpSplit[0], rrc);

				// get container
				ResourceContainer container = PCMUtil.getElementById(pcm.getResourceEnvironmentModel(),
						ResourceContainer.class, mpSplit[1]);
				rcMapping.put(rrc, container);
			}
		});
	}

	@Override
	public void reset(boolean hard) {
	}

	@Override
	public <T> Optional<T> resolveGenericCorrespondence(EObject obj, String tag, Class<T> type) {
		return null;
	}

	@Override
	public <T> List<T> resolveGenericCorrespondences(EObject obj, String tag, Class<T> type) {
		return null;
	}

	@Override
	public <T extends EObject> void createdObject(T obj, VsumChangeSource source) {
	}

	@Override
	public <T extends EObject> void deletedObject(T obj, VsumChangeSource source) {
	}

	@Override
	public Optional<ResourceContainer> getCorrespondingResourceContainer(RuntimeResourceContainer rrc) {
		return Optional.of(rcMapping.get(rrc));
	}

	@Override
	public Optional<ServiceInstrumentationPoint> getCorrespondingInstrumentationPoint(ServiceEffectSpecification seff) {
		return null;
	}

	@Override
	public RuntimeResourceContainer getContainerById(String hostId) {
		return rrcMapping.get(hostId);
	}

	@Override
	public RuntimeResourceContainerConnection getLinkByIds(String fromId, String toId) {
		return null;
	}

	@Override
	public boolean containsHostId(String hostId) {
		return rrcMapping.containsKey(hostId);
	}

	@Override
	public boolean containsLink(String fromId, String toId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RuntimeResourceContainer createResourceContainer(String hostId, String hostName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createResourceContainerLink(String fromId, String toId) {
		// TODO Auto-generated method stub

	}

}
