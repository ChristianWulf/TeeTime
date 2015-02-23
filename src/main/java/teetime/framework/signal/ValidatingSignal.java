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
package teetime.framework.signal;

import java.util.LinkedList;
import java.util.List;

import teetime.framework.Stage;
import teetime.framework.validation.InvalidPortConnection;

public final class ValidatingSignal implements ISignal {

	private final List<InvalidPortConnection> invalidPortConnections = new LinkedList<InvalidPortConnection>();

	public ValidatingSignal() {}

	@Override
	public void trigger(final Stage stage) {
		stage.onValidating(this.invalidPortConnections);
	}

	public List<InvalidPortConnection> getInvalidPortConnections() {
		return this.invalidPortConnections;
	}

}
