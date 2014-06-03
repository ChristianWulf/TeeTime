/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package teetime.framework.core;

import java.util.UUID;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public abstract class AbstractStage implements IStage {

	private final String id;
	/**
	 * A unique logger instance per stage instance
	 */
	protected Log logger;
	private IPipeline owningPipeline;

	private Thread owningThread;

	public AbstractStage() {
		this.id = UUID.randomUUID().toString(); // the id should only be represented by a UUID, not additionally by the class name
		this.logger = LogFactory.getLog(this.id);
	}

	public String getId() {
		return this.id;
	}

	public IPipeline getOwningPipeline() {
		return this.owningPipeline;
	}

	public void setOwningPipeline(final IPipeline owningPipeline) {
		this.owningPipeline = owningPipeline;
	}

	@Override
	public Thread getOwningThread() {
		return this.owningThread;
	}

	public void setOwningThread(final Thread owningThread) {
		this.owningThread = owningThread;
	}

	@Override
	public String toString() {
		// return "{" + "class=" + this.getClass().getSimpleName() + ", id=" + this.id + "}";
		return this.getClass().getSimpleName() + "(" + Thread.currentThread() + ")";
	}
}
