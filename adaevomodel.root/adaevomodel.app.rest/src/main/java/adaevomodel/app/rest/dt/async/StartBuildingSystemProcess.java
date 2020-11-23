package adaevomodel.app.rest.dt.async;

import java.util.List;

import org.palladiosimulator.pcm.repository.OperationInterface;

import adaevomodel.base.core.ICallGraphProvider;
import adaevomodel.base.shared.util.AbstractObservable;
import adaevomodel.designtime.systemextraction.pcm.data.AbstractConflict;
import adaevomodel.designtime.systemextraction.pcm.impl.PCMSystemBuilder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartBuildingSystemProcess extends AbstractObservable<AbstractConflict<?>> implements Runnable {
	private PCMSystemBuilder builder;
	private ICallGraphProvider provider;
	private List<OperationInterface> systemInterfaces;

	@Override
	public void run() {
		boolean finished = builder.startBuildingSystem(provider.provideCallGraph(), systemInterfaces);
		if (finished) {
			this.flood(null);
		} else {
			this.flood(builder.getCurrentConflict());
		}
	}

}
