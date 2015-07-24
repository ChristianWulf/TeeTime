/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://christianwulf.github.io/teetime)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teetime.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import teetime.framework.pipe.InstantiationPipe;

/**
 * Represents a context that is used by a configuration and composite stages to connect ports, for example.
 * Stages can be added by executing {@link #addThreadableStage(Stage)}.
 *
 * @since 2.0
 */
final class ConfigurationContext {

	public static final ConfigurationContext EMPTY_CONTEXT = new ConfigurationContext(null);

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationContext.class);

	private final List<ConfigurationContext> childs = new ArrayList<ConfigurationContext>(); // parent-child-tree
	private final AbstractCompositeStage compositeStage;

	private ThreadService runtimeService;

	ConfigurationContext(final AbstractCompositeStage compositeStage) {
		this.compositeStage = compositeStage;
		this.runtimeService = new ThreadService(compositeStage);
	}

	Map<Stage, String> getThreadableStages() {
		return runtimeService.getThreadableStages();
	}

	/**
	 * @see AbstractCompositeStage#addThreadableStage(Stage)
	 */
	final void addThreadableStage(final Stage stage, final String threadName) {
		childFunction(stage);
		runtimeService.addThreadableStage(stage, threadName);
	}

	/**
	 * @see AbstractCompositeStage#connectPorts(OutputPort, InputPort, int)
	 */
	final <T> void connectPorts(final OutputPort<? extends T> sourcePort, final InputPort<T> targetPort, final int capacity) {
		if (sourcePort.getOwningStage().getInputPorts().size() == 0) {
			if (!runtimeService.getThreadableStages().containsKey(sourcePort.getOwningStage())) {
				addThreadableStage(sourcePort.getOwningStage(), sourcePort.getOwningStage().getId());
			}
		}
		if (sourcePort.getPipe() != null || targetPort.getPipe() != null) {
			LOGGER.warn("Overwriting existing pipe while connecting stages " +
					sourcePort.getOwningStage().getId() + " and " + targetPort.getOwningStage().getId() + ".");
		}
		childFunction(sourcePort.getOwningStage());
		childFunction(targetPort.getOwningStage());
		new InstantiationPipe(sourcePort, targetPort, capacity);
	}

	// FIXME: Rename method
	final void childFunction(final Stage stage) {
		if (!stage.owningContext.equals(EMPTY_CONTEXT)) {
			if (stage.owningContext != this) { // Performance
				// this.runtimeService.getThreadableStages().putAll(stage.owningContext.getRuntimeService().getThreadableStages());
				// stage.owningContext.getRuntimeService().setThreadableStages(this.getRuntimeService().getThreadableStages());
				childs.add(stage.owningContext);
			}
		} else {
			stage.owningContext = this;
		}

	}

	final void finalizeContext() {
		for (ConfigurationContext child : childs) {
			child.finalizeContext();
			mergeContexts(child);
		}
	}

	final void initializeServices() {
		runtimeService.onInitialize();
	}

	private void mergeContexts(final ConfigurationContext child) {
		runtimeService.merge(child.getRuntimeService());
	}

	public ThreadService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(final ThreadService runtimeService) {
		this.runtimeService = runtimeService;
	}

}
