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

import java.util.List;

import teetime.util.concurrent.workstealing.exception.DequePopException;

/**
 * 
 * @author Christian Wulf
 * 
 * @since 1.10
 * 
 * @param <T>
 *            the type of elements this pipe may contain
 * @param <P>
 */
public interface IPipe<T> {

	/**
	 * @since 1.10
	 */
	void put(T object);

	/**
	 * @since 1.10
	 */
	void putMultiple(List<T> elements);

	// Let this uncommented for documentation purpose:<br>
	// Since the concurrent execution does not base on locks, it is difficult to define a blocking take. Since there is no definition of blocking in the sequential
	// execution, we do not define a blocking take in this interface
	// /**
	// * @since 1.10
	// */
	// @Deprecated
	// T take();

	/**
	 * @return and removes the next element if the pipe is not empty, otherwise <code>null</code>
	 * 
	 * @since 1.10
	 */
	<A extends T> A tryTake();

	/**
	 * 
	 * @return and removes the next element if the pipe is not empty, otherwise it throws a <code>DequePopException</code>
	 * 
	 * @throws DequePopException
	 */
	<A extends T> A take();

	/**
	 * @since 1.10
	 * @return but does not removes the next element if the pipe is not empty, otherwise <code>null</code>
	 */
	<A extends T> A read();

	/**
	 * @since 1.10
	 */
	List<?> tryTakeMultiple(int numElementsToTake);

	/**
	 * <i>Attention: Checking for emptiness is rarely appropriate, since the state from EMPTY to NON-EMPTY can change between check and access (stale state).</i>
	 * 
	 * @return <code>true</code> if this pipe is empty in this special point in time, otherwise <code>false</code>.
	 */
	boolean isEmpty();

	/**
	 * @since 1.10
	 */
	<S extends ISource, A extends T> void setSourcePort(final IOutputPort<S, A> sourcePort);

	/**
	 * @since 1.10
	 */
	<S extends ISink<S>, A extends T> void setTargetPort(final IInputPort<S, T> targetPort);

	/**
	 * @since 1.10
	 */
	IOutputPort<?, ? extends T> getSourcePort();

	/**
	 * @since 1.10
	 */
	IInputPort<?, T> getTargetPort();

	/**
	 * @throws Exception
	 * @since 1.10
	 */
	void notifyPipelineStarts() throws Exception;

	/**
	 * @since 1.10
	 */
	void notifyPipelineStops();

	/**
	 * @since 1.10
	 */
	void close();

}
