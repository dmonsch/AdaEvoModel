package adaevomodel.runtime.pipeline;

import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;

public interface IPipelineClassProvider<B extends RuntimePipelineBlackboard> {

	public boolean contains(Class<? extends AbstractIterativePipelinePart<B>> transformationClass);

	public AbstractIterativePipelinePart<B> provide(
			Class<? extends AbstractIterativePipelinePart<B>> transformationClass);

}
