/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime-framework.github.io)
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
package teetime.stage;

import teetime.framework.AbstractProducerStage;
import teetime.framework.TerminationStrategy;

/**
 * This stage sends the current timestamp repeatedly with a given interval delay of {@link #intervalDelayInMs}.
 *
 * @stage.sketch
 *
 * 				<pre>
 * +------------------------+
 * |                        |
 * |                      +---+
 * |      *INTERVAL* +--&gt; |   |
 * |                      +---+
 * |                        |
 * +------------------------+
 *               </pre>
 *
 * @author Nelson Tavares de Sousa
 *
 * @stage.output Current timestamp as long.
 *
 */
public final class Clock extends AbstractProducerStage<Long> {

	private boolean initialDelayExceeded;// = false;

	/**
	 * Waiting time span until first sent element.
	 */
	private long initialDelayInMs;
	/**
	 * Interval between two sent elements in ms.
	 */
	private long intervalDelayInMs;

	@Override
	public TerminationStrategy getTerminationStrategy() {
		return TerminationStrategy.BY_INTERRUPT;
	}

	@Override
	protected void execute() {
		if (this.initialDelayExceeded) {
			this.sleep(this.intervalDelayInMs);
		} else {
			this.initialDelayExceeded = true;
			this.sleep(this.initialDelayInMs);
		}

		// this.logger.debug("Emitting timestamp");
		outputPort.send(this.getCurrentTimeInNs());
	}

	private void sleep(final long delayInMs) {
		try {
			Thread.sleep(delayInMs);
		} catch (InterruptedException e) {
			this.terminateStage();
		}
	}

	private long getCurrentTimeInNs() {
		return System.nanoTime();
	}

	public long getInitialDelayInMs() {
		return this.initialDelayInMs;
	}

	public void setInitialDelayInMs(final long initialDelayInMs) {
		this.initialDelayInMs = initialDelayInMs;
	}

	public long getIntervalDelayInMs() {
		return this.intervalDelayInMs;
	}

	public void setIntervalDelayInMs(final long intervalDelayInMs) {
		this.intervalDelayInMs = intervalDelayInMs;
	}

}
