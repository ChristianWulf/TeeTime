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

package teetime.util.concurrent.workstealing;

import java.util.Arrays;

/**
 * 
 * @author Christian Wulf
 * 
 * @see "Dynamic Circular WorkStealing Deque"
 * 
 * @since 1.10
 * 
 * @param <T>
 */
public class CircularArray<T> {

	private final long logSize;
	private final T[] segment;
	private final long mask;

	/**
	 * 
	 * @param logSize
	 *            The initial size of this array in log2, i.e., the number of bits to use
	 */
	@SuppressWarnings("unchecked")
	public CircularArray(final long logSize) {
		this.logSize = logSize;
		this.segment = (T[]) new Object[1 << this.logSize];
		this.mask = this.getCapacity() - 1; // mask = 0..01..1
	}

	public long getCapacity() {
		return this.segment.length;
	}

	public T get(final long i) {
		return this.segment[(int) (i & this.mask)]; // risk of overflow
	}

	public void put(final long i, final T o) {
		this.segment[(int) (i & this.mask)] = o; // risk of overflow
	}

	public CircularArray<T> grow(final long b, final long t) {
		final CircularArray<T> a = new CircularArray<T>(this.logSize + 1);
		for (long i = t; i < b; i++) {
			a.put(i, this.get(i));
		}
		return a;
	}

	public CircularArray<T> shrink(final long b, final long t) {
		final CircularArray<T> a = new CircularArray<T>(this.logSize - 1);
		for (long i = t; i < b; i++) {
			a.put(i, this.get(i));
		}
		return a;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.segment);
	}
}
