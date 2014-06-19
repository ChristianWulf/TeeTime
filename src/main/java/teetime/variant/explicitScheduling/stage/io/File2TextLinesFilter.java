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

package teetime.variant.explicitScheduling.stage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import teetime.variant.explicitScheduling.framework.core.AbstractFilter;
import teetime.variant.explicitScheduling.framework.core.Context;
import teetime.variant.explicitScheduling.framework.core.IInputPort;
import teetime.variant.explicitScheduling.framework.core.IOutputPort;
import teetime.variant.explicitScheduling.stage.util.TextLine;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class File2TextLinesFilter extends AbstractFilter<File2TextLinesFilter> {

	public final IInputPort<File2TextLinesFilter, File> fileInputPort = this.createInputPort();

	public final IOutputPort<File2TextLinesFilter, TextLine> textLineOutputPort = this.createOutputPort();

	private String charset = "UTF-8";

	/**
	 * @since 1.10
	 */
	@Override
	protected boolean execute(final Context<File2TextLinesFilter> context) {
		final File textFile = context.tryTake(this.fileInputPort);
		if (textFile == null) {
			return false;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), this.charset));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) {
					context.put(this.textLineOutputPort, new TextLine(textFile, line));
				} // else: ignore empty line
			}
		} catch (final FileNotFoundException e) {
			this.logger.error("", e);
		} catch (final IOException e) {
			this.logger.error("", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException e) {
				this.logger.warn("", e);
			}
		}
		return true;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(final String charset) {
		this.charset = charset;
	}

}
