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

package teetime.variant.explicitScheduling.stage.composite;

import teetime.variant.explicitScheduling.framework.core.AbstractFilter;
import teetime.variant.explicitScheduling.framework.core.Context;
import teetime.variant.explicitScheduling.framework.core.IPipe;
import teetime.variant.explicitScheduling.stage.io.File2TextLinesFilter;
import teetime.variant.explicitScheduling.stage.kieker.className.ClassNameRegistryRepository;
import teetime.variant.explicitScheduling.stage.kieker.fileToRecord.textLine.TextLine2RecordFilter;
import teetime.variant.explicitScheduling.stage.util.TextLine;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
// FIXME extend from CompositeStage
public class ReadRecordFromCsvFileFilter extends AbstractFilter<File2TextLinesFilter> {

	private final File2TextLinesFilter stage0;
	private final TextLine2RecordFilter stage1;

	/**
	 * @since 1.10
	 * @param textLinePipe
	 */
	public ReadRecordFromCsvFileFilter(final IPipe<TextLine> textLinePipe, final ClassNameRegistryRepository classNameRegistryRepository) {
		this.stage0 = new File2TextLinesFilter();
		this.stage1 = new TextLine2RecordFilter(classNameRegistryRepository);

		textLinePipe.setSourcePort(this.stage0.textLineOutputPort);
		textLinePipe.setTargetPort(this.stage1.textLineInputPort);
		// FIXME textLinePipe needs to be added to a group

		/*
		 * FIXME a composite filter only serves as a convenient way to connect multiple stages or to realize self-connected queues.<br>
		 * How should the stage scheduler be aware of these internal stages?<br>
		 * Should the context not be passed to each of the internal stages?
		 */
	}

	/**
	 * @since 1.10
	 */
	@Override
	protected boolean execute(final Context<File2TextLinesFilter> context) {
		return this.stage0.execute();
	}

}
