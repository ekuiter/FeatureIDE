/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2019  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.core.sounds;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import de.ovgu.featureide.core.builder.ComposerExtensionClass;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.filter.ConcreteFeatureFilter;
import de.ovgu.featureide.fm.core.functional.Functional;

/**
 * Composer that mixes sound files.
 *
 * @author Elias Kuiter
 *
 */
public class SoundComposer extends ComposerExtensionClass {

	@Override
	public void performFullBuild(Path config) {
		final List<String> selectedFeatures =
			Functional.mapToStringList(Functional.filter(featureProject.loadConfiguration(config).getSelectedFeatures(), new ConcreteFeatureFilter()));
		final File output = featureProject.getBuildFolder().getRawLocation().makeAbsolute().toFile();
		compose(selectedFeatures, output, getOutputFileName());
	}

	private String getOutputFileName() {
		return featureProject.getFeatureModel().getStructure().getRoot().getFeature().getName() + ".mp3";
	}

	@Override
	public void copyNotComposedFiles(Configuration c, IFolder destination) {}

	@Override
	public void buildConfiguration(IFolder folder, Configuration configuration, String configurationName) {
		super.buildConfiguration(folder, configuration, configurationName);
		final List<String> selectedFeatures = Functional.mapToStringList(Functional.filter(configuration.getSelectedFeatures(), new ConcreteFeatureFilter()));
		final File output = new File(folder.getRawLocationURI());
		compose(selectedFeatures, output, getOutputFileName());
	}

	private void compose(List<String> selectedFeatures, File output, String name) {
		final List<File> soundFiles = new ArrayList<File>();
		for (int i = 0; i < selectedFeatures.size(); i++) {
			final IFolder f = featureProject.getSourceFolder().getFolder(selectedFeatures.get(i));
			final File folder = f.getRawLocation().makeAbsolute().toFile();
			soundFiles.addAll(getAllFiles(null, folder));
		}

		final File outputImageFile = new File(output, name);
		mixSoundFiles(soundFiles, outputImageFile);
	}

	public static void mixSoundFiles(List<File> soundFiles, File outputSoundFile) {
		String command =
			"ffmpeg -y " + soundFiles.stream().map(soundFile -> "-i \"" + soundFile.toString() + "\"").collect(Collectors.joining(" ")) + " -filter_complex \"";
		int i = 0;
		for (final File soundFile : soundFiles) {
			final String name = soundFile.getName().replaceFirst("[.][^.]+$", "");
			final String[] parts = name.split(",");
			final double offset = parts.length > 1 ? Double.parseDouble(parts[0]) : 0;
			final double gain = parts.length > 1 ? Double.parseDouble(parts[1]) : 1;
			command += "[" + i + "]volume=" + gain + ",atrim=start=" + offset + "[" + i + "_out];";
			i++;
		}
		for (int j = 0; j < soundFiles.size(); j++) {
			command += "[" + j + "_out]";
		}
		command += "amix=inputs=" + soundFiles.size() + ":duration=longest\" \"" + outputSoundFile.toString() + "\"";
		final ProcessBuilder processBuilder = new ProcessBuilder();
		// this is kind of bad
		if (System.getProperty("os.name").startsWith("Windows")) {
			processBuilder.command("cmd.exe", "/c", command);
		} else {
			processBuilder.command("/bin/sh", "-c", command);
		}
		try {
			final Process process = processBuilder.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			if (!builder.toString().startsWith("ffmpeg version")) {
				throw new RuntimeException("To use the sounds composer, please install ffmpeg and add it to the PATH environment variable.");
			}
			process.waitFor();
			process.destroy();
		} catch (final IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<File> getAllFiles(List<File> files, File dir) {
		if (files == null) {
			files = new ArrayList<File>();
		}

		if (!dir.isDirectory()) {
			files.add(dir);
			return files;
		}

		for (final File file : dir.listFiles()) {
			getAllFiles(files, file);
		}
		return files;
	}

	@Override
	public boolean supportsPartialFeatureProject() {
		return false;
	}

	@Override
	public void buildPartialFeatureProjectAssets(IFolder sourceFolder, ArrayList<String> removedFeatures, ArrayList<String> mandatoryFeatures)
			throws IOException, CoreException {

	}
}
