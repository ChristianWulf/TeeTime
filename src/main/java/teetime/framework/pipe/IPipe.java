/**
 * Copyright (C) 2015 TeeTime (http://teetime.sourceforge.net)
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
package teetime.framework.pipe;

import teetime.framework.InputPort;
import teetime.framework.OutputPort;
import teetime.framework.signal.ISignal;

/**
 * Represents a pipe that connects an output port with an input port.
 */
public interface IPipe {

	/**
	 * Adds an element to the Pipe.
	 *
	 * @param element
	 *            Element which will be added
	 * @return <code>true</code> if the element could be added, false otherwise
	 */
	boolean add(Object element);

	/**
	 * Checks whether the pipe is empty or not.
	 *
	 * @return <code>true</code> if the pipe is empty, false otherwise.
	 */
	boolean isEmpty();

	/**
	 * Retrieves the number of elements, the pipe is capable to carry at the same time.
	 *
	 * @return Number of elements
	 */
	int size();

	/**
	 * Retrieves the last element of the pipe and deletes it.
	 *
	 * @return The last element in the pipe.
	 */
	Object removeLast();

	/**
	 * Retrieves the receiving port.
	 *
	 * @return InputPort which is connected to the pipe.
	 */
	InputPort<?> getTargetPort();

	/**
	 * A stage can pass on a signal by executing this method. The signal will be sent to the receiving stage.
	 *
	 * @param signal
	 *            The signal which needs to be passed on.
	 */
	void sendSignal(ISignal signal);

	@Deprecated
	<T> void connectPorts(OutputPort<? extends T> sourcePort, InputPort<T> targetPort);

	/**
	 * Stages report new elements with this method.
	 */
	void reportNewElement();

	boolean isClosed();

	boolean hasMore();

	// "signal" handling

	void waitForStartSignal() throws InterruptedException;

	void close();

}
