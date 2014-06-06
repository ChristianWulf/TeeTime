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
package teetime.stage.kieker;

import java.io.File;

import teetime.framework.concurrent.ConcurrentWorkStealingPipe;
import teetime.framework.concurrent.ConcurrentWorkStealingPipeFactory;
import teetime.framework.core.CompositeFilter;
import teetime.framework.core.IInputPort;
import teetime.framework.core.IOutputPort;
import teetime.stage.FileExtensionFilter;
import teetime.stage.basic.merger.Merger;
import teetime.stage.kieker.className.ClassNameRegistryCreationFilter;
import teetime.stage.kieker.className.ClassNameRegistryRepository;
import teetime.stage.kieker.fileToRecord.BinaryFile2RecordFilter;
import teetime.stage.kieker.fileToRecord.DatFile2RecordFilter;
import teetime.stage.kieker.fileToRecord.ZipFile2RecordFilter;
import teetime.stage.predicate.IsDirectoryPredicate;
import teetime.stage.predicate.PredicateFilter;

import kieker.common.record.IMonitoringRecord;
import kieker.common.util.filesystem.BinaryCompressionMethod;
import kieker.common.util.filesystem.FSUtil;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class File2RecordFilter extends CompositeFilter {

	public final IInputPort<PredicateFilter<File>, File> fileInputPort;

	public final IOutputPort<Merger<IMonitoringRecord>, IMonitoringRecord> recordOutputPort;

	private ClassNameRegistryRepository classNameRegistryRepository;

	/**
	 * @since 1.10
	 */
	public File2RecordFilter(final ClassNameRegistryRepository classNameRegistryRepository) {
		this.classNameRegistryRepository = classNameRegistryRepository;

		// FIXME does not yet work with more than one thread due to classNameRegistryRepository (reason not comprehensible)
		// create stages
		final PredicateFilter<File> isDirectoryFilter = new PredicateFilter<File>(new IsDirectoryPredicate());
		final ClassNameRegistryCreationFilter classNameRegistryCreationFilter = new ClassNameRegistryCreationFilter(this.classNameRegistryRepository);
		final MonitoringLogDirectory2Files directory2FilesFilter = new MonitoringLogDirectory2Files();
		final FileExtensionFilter fileExtensionFilter = new FileExtensionFilter();
		final Merger<File> fileMerger = new Merger<File>();

		final DatFile2RecordFilter datFile2RecordFilter = new DatFile2RecordFilter(this.classNameRegistryRepository);
		final BinaryFile2RecordFilter binaryFile2RecordFilter = new BinaryFile2RecordFilter(this.classNameRegistryRepository);
		final ZipFile2RecordFilter zipFile2RecordFilter = new ZipFile2RecordFilter();

		final Merger<IMonitoringRecord> recordMerger = new Merger<IMonitoringRecord>();

		// store ports due to readability reasons
		final IOutputPort<FileExtensionFilter, File> normalFileOutputPort = fileExtensionFilter.createOutputPortForFileExtension(FSUtil.NORMAL_FILE_EXTENSION);
		final IOutputPort<FileExtensionFilter, File> binFileOutputPort = fileExtensionFilter.createOutputPortForFileExtension(BinaryCompressionMethod.NONE
				.getFileExtension());
		final IOutputPort<FileExtensionFilter, File> zipFileOutputPort = fileExtensionFilter.createOutputPortForFileExtension(FSUtil.ZIP_FILE_EXTENSION);

		// connect ports by pipes
		this.connectWithPipe(isDirectoryFilter.matchingOutputPort, classNameRegistryCreationFilter.directoryInputPort);
		this.connectWithPipe(isDirectoryFilter.mismatchingOutputPort, fileMerger.getNewInputPort()); // BETTER restructure pipeline
		this.connectWithPipe(classNameRegistryCreationFilter.relayDirectoryOutputPort, directory2FilesFilter.directoryInputPort);
		this.connectWithPipe(classNameRegistryCreationFilter.filePrefixOutputPort, directory2FilesFilter.filePrefixInputPort);
		this.connectWithPipe(directory2FilesFilter.fileOutputPort, fileExtensionFilter.fileInputPort);
		this.connectWithPipe(zipFileOutputPort, fileMerger.getNewInputPort());

		final ConcurrentWorkStealingPipeFactory<File> concurrentWorkStealingPipeFactory0 = new ConcurrentWorkStealingPipeFactory<File>();
		final ConcurrentWorkStealingPipe<File> concurrentWorkStealingPipe0 = concurrentWorkStealingPipeFactory0.create();
		concurrentWorkStealingPipe0.setSourcePort(normalFileOutputPort);
		concurrentWorkStealingPipe0.setTargetPort(datFile2RecordFilter.fileInputPort);

		final ConcurrentWorkStealingPipeFactory<File> concurrentWorkStealingPipeFactory1 = new ConcurrentWorkStealingPipeFactory<File>();
		final ConcurrentWorkStealingPipe<File> concurrentWorkStealingPipe1 = concurrentWorkStealingPipeFactory1.create();
		concurrentWorkStealingPipe1.setSourcePort(binFileOutputPort);
		concurrentWorkStealingPipe1.setTargetPort(binaryFile2RecordFilter.fileInputPort);

		final ConcurrentWorkStealingPipeFactory<File> concurrentWorkStealingPipeFactory2 = new ConcurrentWorkStealingPipeFactory<File>();
		final ConcurrentWorkStealingPipe<File> concurrentWorkStealingPipe2 = concurrentWorkStealingPipeFactory2.create();
		concurrentWorkStealingPipe2.setSourcePort(fileMerger.outputPort);
		concurrentWorkStealingPipe2.setTargetPort(zipFile2RecordFilter.fileInputPort);

		final ConcurrentWorkStealingPipeFactory<IMonitoringRecord> concurrentWorkStealingPipeFactoriesNormal = new ConcurrentWorkStealingPipeFactory<IMonitoringRecord>();
		final ConcurrentWorkStealingPipe<IMonitoringRecord> datPipe = concurrentWorkStealingPipeFactoriesNormal.create();
		datPipe.connect(datFile2RecordFilter.recordOutputPort, recordMerger.getNewInputPort());

		final ConcurrentWorkStealingPipeFactory<IMonitoringRecord> concurrentWorkStealingPipeFactoriesBinary = new ConcurrentWorkStealingPipeFactory<IMonitoringRecord>();
		final ConcurrentWorkStealingPipe<IMonitoringRecord> binaryPipe = concurrentWorkStealingPipeFactoriesBinary.create();
		binaryPipe.connect(binaryFile2RecordFilter.recordOutputPort, recordMerger.getNewInputPort());

		final ConcurrentWorkStealingPipeFactory<IMonitoringRecord> concurrentWorkStealingPipeFactoriesZip = new ConcurrentWorkStealingPipeFactory<IMonitoringRecord>();
		final ConcurrentWorkStealingPipe<IMonitoringRecord> zipPipe = concurrentWorkStealingPipeFactoriesZip.create();
		zipPipe.connect(zipFile2RecordFilter.recordOutputPort, recordMerger.getNewInputPort());

		this.fileInputPort = isDirectoryFilter.inputPort;
		this.recordOutputPort = recordMerger.outputPort;

		this.schedulableStages.add(isDirectoryFilter);

		// this.schedulableStages.add(classNameRegistryCreationFilter);
		// this.schedulableStages.add(directory2FilesFilter);
		// this.schedulableStages.add(fileMerger);
		// this.schedulableStages.add(fileExtensionFilter);

		this.schedulableStages.add(datFile2RecordFilter);
		this.schedulableStages.add(binaryFile2RecordFilter);
		this.schedulableStages.add(zipFile2RecordFilter);
		this.schedulableStages.add(recordMerger);
	}

	/**
	 * @since 1.10
	 */
	public File2RecordFilter() {
		this(null);
	}

	public ClassNameRegistryRepository getClassNameRegistryRepository() {
		return this.classNameRegistryRepository;
	}

	public void setClassNameRegistryRepository(final ClassNameRegistryRepository classNameRegistryRepository) {
		this.classNameRegistryRepository = classNameRegistryRepository;
	}

}
