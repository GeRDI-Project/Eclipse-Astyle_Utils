/**
 * Copyright © 2017 Robin Weiss (http://www.gerdi-project.de)
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
 */
package astyle_neon.handlers;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import astyle_neon.preferences.AStylePreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * The handler for the Formatting command. This command attempts to find out which project is currently being worked on,
 * and formats all files of this project.
 *
 * @author Robin Weiss
 *
 */
public final class FormattingHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // get window and project
        IProject currentProject = AStyleEclipseUtils.getActiveProject(event);

        // format only if we found an active project
        FeedbackMessage statusMessage;

        if (currentProject != null) {
            // format project and memorize the status message
            statusMessage = formatProject(currentProject);
        } else
            statusMessage = FeedbackMessage.CreateError(AStyleHandlerConstants.ERROR_NO_PROJECT);

        // notify the user about the status
        statusMessage.display(event);

        return null;
    }


    /**
     * Formats all source files within a project
     *
     * @param project
     *            the project of which the source files are formatted
     * @return a message describing the status of the formatting operation
     */
    private FeedbackMessage formatProject(IProject project)
    {
        final ProcessBuilder formattingBuilder = createFormattingProcess(project);

        // abort if any path is missing
        if (formattingBuilder == null)
            return FeedbackMessage.CreateError(
                       String.format(AStyleHandlerConstants.ERROR_NO_PATH, project.getName()));

        final String processOutput;

        try {
            // execute command
            final Process formattingProcess = formattingBuilder.start();
            int returnCode = formattingProcess.waitFor();

            // read returned string
            final BufferedReader outputReader = new BufferedReader(
                new InputStreamReader(
                    formattingProcess.getInputStream(),
                    StandardCharsets.UTF_8));

            processOutput = outputReader.lines().collect(Collectors.joining("\n"));

            // handle erroneous return code
            if (returnCode != 0) {
                // read returned error string
                final BufferedReader errorReader =
                    new BufferedReader(
                    new InputStreamReader(
                        formattingProcess.getErrorStream(),
                        StandardCharsets.UTF_8));
                final String errorOutput = errorReader.lines().collect(Collectors.joining("\n"));

                return FeedbackMessage.CreateError(
                           String.format(
                               AStyleHandlerConstants.ERROR_RETURN,
                               processOutput,
                               errorOutput,
                               project.getName(),
                               returnCode));
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return FeedbackMessage.CreateError(
                       String.format(AStyleHandlerConstants.ERROR_GENERIC, project.getName()));
        }

        // try to refresh the project's changed files
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);

        } catch (CoreException e) {
            e.printStackTrace();
            return FeedbackMessage.CreateInfo(
                       String.format(
                           AStyleHandlerConstants.SUCCESS_REFRESH,
                           processOutput,
                           project.getName()));
        }

        return FeedbackMessage.CreateInfo(
                   String.format(
                       AStyleHandlerConstants.SUCCESS,
                       processOutput,
                       project.getName()));
    }


    /**
     * Returns a process builder for running a formatting process.
     * If the current project uses the HarvesterUtils astyle-format script,
     * this script is preferred over the plugin preferences.
     *
     * @param project the active project
     *
     * @return a formatting process builder
     */
    private ProcessBuilder createFormattingProcess(IProject project)
    {
        // get source path from the project
        final String sourcePath = project
                                  .getFolder(AStyleHandlerConstants.PROJECT_SOURCE_DIRECTORY)
                                  .getLocation()
                                  .toOSString();

        // if the project has a formatting util script, use that instead
        final IFile formattingUtilScript = project.getFile(AStyleHandlerConstants.HARVESTER_FORMATTING_SCRIPT);

        if (formattingUtilScript.exists())
            return createHarvesterFormattingProcess(formattingUtilScript);
        else
            return createDefaultFormattingProcess(sourcePath);
    }


    /**
     * Returns a process builder for running the scripts/formatting/astyle-format
     * script.
     *
     * @param formattingScript the script that is to be executed
     *
     * @return a process builder for formatting the active project
     */
    private ProcessBuilder createHarvesterFormattingProcess(IFile formattingScript)
    {
        final String scriptFullPath = formattingScript.getLocation().toOSString();
        final String projectPath = scriptFullPath.substring(0, scriptFullPath.length() - formattingScript.getProjectRelativePath().toOSString().length());

        final ProcessBuilder pb = new ProcessBuilder(scriptFullPath);
        pb.directory(new File(projectPath));

        return pb;
    }


    /**
     * Returns a process builder for running formatting the project using the
     * formatter defined via the plugin preferences.
     *
     * @param unescapedSourcePath the source path of the active project without escaped whitespaces
     *
     * @return a process builder for formatting the active project
     */
    private ProcessBuilder createDefaultFormattingProcess(String unescapedSourcePath)
    {
        final String sourcePath =
            unescapedSourcePath.replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // get astyle binary path from preferences
        final String binPath = AStylePreferenceConstants.STORE
                               .getString(AStylePreferenceConstants.BINARY_PATH_OPTION)
                               .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // get astyle options ini path from preferences
        final String optionsPath = AStylePreferenceConstants.STORE
                                   .getString(AStylePreferenceConstants.OPTIONS_FILE_PATH_OPTION)
                                   .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);


        if (!binPath.isEmpty() && !optionsPath.isEmpty())
            return new ProcessBuilder(
                       String.format(AStyleHandlerConstants.ASTYLE_BIN_CMD, binPath),
                       AStyleHandlerConstants.RECURSIVE_CMD_PARAM,
                       AStyleHandlerConstants.NO_BACKUP_CMD_PARAM,
                       AStyleHandlerConstants.ONLY_FORMATTED_CMD_PARAM,
                       String.format(AStyleHandlerConstants.OPTIONS_CMD_PARAM, optionsPath),
                       String.format(AStyleHandlerConstants.TARGET_FOLDER_CMD, sourcePath)
                   );
        else
            return null;
    }
}
