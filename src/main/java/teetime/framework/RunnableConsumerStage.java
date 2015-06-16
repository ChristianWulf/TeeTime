/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime.sourceforge.net)
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

import teetime.framework.idle.IdleStrategy;
import teetime.framework.idle.YieldStrategy;
import teetime.framework.signal.ISignal;
import teetime.framework.signal.TerminatingSignal;

public final class RunnableConsumerStage extends AbstractRunnableStage {

	// cache the input ports here since getInputPorts() always returns a new copy
	private final InputPort<?>[] inputPorts;

	/**
	 * Creates a new instance with the {@link YieldStrategy} as default idle strategy.
	 *
	 * @param stage
	 *            to execute within an own thread
	 */
	public RunnableConsumerStage(final Stage stage) {
		this(stage, new YieldStrategy());
	}

	public RunnableConsumerStage(final Stage stage, final IdleStrategy idleStrategy) {
		super(stage);
		this.inputPorts = stage.getInputPorts(); // FIXME should getInputPorts() really be defined in Stage?
	}

	@SuppressWarnings("PMD.GuardLogStatement")
	@Override
	protected void beforeStageExecution(final Stage stage) throws InterruptedException {
		logger.trace("Waiting for start signals..." + inputPorts);
		for (InputPort<?> inputPort : inputPorts) {
			inputPort.waitForStartSignal();
		}
		for (InputPort<?> inputPort : inputPorts) {
			inputPort.waitForStartSignal();
		}
		logger.trace("Starting..." + stage);
	}

	@Override
	protected void executeStage(final Stage stage) {
		try {
			stage.executeStage();
		} catch (NotEnoughInputException e) {
			checkForTerminationSignal(stage);
		}
	}

	private void checkForTerminationSignal(final Stage stage) {
		for (InputPort<?> inputPort : inputPorts) {
			if (!inputPort.isClosed()) {
				return;
			}
		}

		stage.terminate();
	}

	@Override
	protected void afterStageExecution(final Stage stage) {
		final ISignal signal = new TerminatingSignal();
		for (InputPort<?> inputPort : inputPorts) {
			stage.onSignal(signal, inputPort);
		}
	}

}
