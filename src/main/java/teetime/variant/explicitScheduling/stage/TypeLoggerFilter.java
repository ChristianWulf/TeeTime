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

package teetime.variant.explicitScheduling.stage;

import java.io.PrintStream;

import teetime.variant.explicitScheduling.framework.core.AbstractFilter;
import teetime.variant.explicitScheduling.framework.core.Context;
import teetime.variant.explicitScheduling.framework.core.IInputPort;
import teetime.variant.explicitScheduling.framework.core.IOutputPort;

/**
 * @author Matthias Rohr, Jan Waller, Nils Christian Ehmke, Christian Wulf
 * 
 * @since 1.10
 */
public class TypeLoggerFilter<T> extends AbstractFilter<TypeLoggerFilter<T>> {

	public final IInputPort<TypeLoggerFilter<T>, T> INPUT_OBJECT = this.createInputPort();

	public final IOutputPort<TypeLoggerFilter<T>, T> RELAYED_OBJECT = this.createOutputPort();

	private final PrintStream printStream;

	/**
	 * @since 1.10
	 */
	private TypeLoggerFilter() {
		this.printStream = System.out;
	}

	/**
	 * @since 1.10
	 */
	public static <T> TypeLoggerFilter<T> create() {
		return new TypeLoggerFilter<T>();
	}

	/**
	 * @since 1.10
	 */
	@Override
	protected boolean execute(final Context<TypeLoggerFilter<T>> context) {
		final T inputObject = context.tryTake(this.INPUT_OBJECT);
		if (inputObject == null) {
			return false;
		}

		final StringBuilder sb = new StringBuilder(128);
		sb.append(super.getId()).append('(').append(inputObject.getClass().getSimpleName()).append(") ").append(inputObject.toString());
		this.printStream.println(sb.toString());

		context.put(this.RELAYED_OBJECT, inputObject);
		return true;
	}

}
