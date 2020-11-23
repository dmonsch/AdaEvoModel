package adaevomodel.runtime.pipeline.entry.core;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.IPipelineClassProvider;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;

public class SpringContextClassProvider implements IPipelineClassProvider<RuntimePipelineBlackboard> {

	private ApplicationContext ctx;

	public SpringContextClassProvider(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean contains(
			Class<? extends AbstractIterativePipelinePart<RuntimePipelineBlackboard>> transformationClass) {
		try {
			ctx.getBean(transformationClass);
		} catch (NoSuchBeanDefinitionException e) {
			return false;
		}
		return true;
	}

	@Override
	public AbstractIterativePipelinePart<RuntimePipelineBlackboard> provide(
			Class<? extends AbstractIterativePipelinePart<RuntimePipelineBlackboard>> transformationClass) {
		return ctx.getBean(transformationClass);
	}

}