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
package teetime.variant.explicitScheduling.stage.basic.distributor;

import java.util.List;

import teetime.variant.explicitScheduling.framework.core.Context;
import teetime.variant.explicitScheduling.framework.core.IOutputPort;

/**
 * @author Nils Christian Ehmke
 * 
 * @since 1.10
 */
public final class RoundRobinStrategy<T> implements IDistributorStrategy<T> {

	private int index = 0;

	public <S extends Distributor<T>> boolean distribute(final Context<S> context, final List<IOutputPort<S, ?>> outputPorts, final T object) {
		final IOutputPort<S, T> port = this.getNextPortInRoundRobinOrder(outputPorts);
		context.put(port, object);

		return true;
	}

	@SuppressWarnings("unchecked")
	private <S extends Distributor<T>> IOutputPort<S, T> getNextPortInRoundRobinOrder(final List<IOutputPort<S, ?>> outputPorts) {
		final IOutputPort<S, T> port = (IOutputPort<S, T>) outputPorts.get(this.index);

		this.index = (this.index + 1) % outputPorts.size();

		return port;
	}

}
