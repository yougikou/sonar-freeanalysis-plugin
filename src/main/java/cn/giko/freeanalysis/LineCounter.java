/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package cn.giko.freeanalysis;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import cn.giko.freeanalysis.rules.base.TargetFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class LineCounter {

  private static final Logger LOG = Loggers.get(LineCounter.class);

  private LineCounter() {
  }

  private static <T extends Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(inputFile)
      .save();
  }

  public static void analyse(SensorContext context, FileLinesContextFactory fileLinesContextFactory, TargetFile targetFile) {
    LOG.debug("Count lines in {}", targetFile.getInputFile().uri());

    Set<Integer> linesOfCode = new HashSet<>();
    Set<Integer> commentLines = new HashSet<>();

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(targetFile.getInputFile());
    linesOfCode.forEach(lineOfCode -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineOfCode, 1));
    fileLinesContext.save();

    // TODO add comment lines logic, add non-comment lines logic

    saveMeasure(context, targetFile.getInputFile(), CoreMetrics.COMMENT_LINES, commentLines.size());
    saveMeasure(context, targetFile.getInputFile(), CoreMetrics.NCLOC, linesOfCode.size());
  }
}
