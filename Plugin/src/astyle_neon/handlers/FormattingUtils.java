/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package astyle_neon.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import astyle_neon.preferences.AStylePreferenceConstants;

/**
 * This helper class offers static formatting related methods.
 * @author Robin Weiss
 */
public class FormattingUtils
{
    /**
     * Private constructor, because this is just a collection of useful methods.
     */
    private FormattingUtils()
    {

    }


    /**
     * Formats a file or folder, returning a feedback message.
     *
     * @param filePath filePath the absolute filepath to the folder or file that is to be formatted
     * @param project the project to which the file belongs
     * @param errorPrefix a short error message that appears if the formatting fails
     * @param successMessage a short message that appears if the formatting was successful
     *
     * @return a feedback message of the formatting process
     */
    public static FeedbackMessage format(String filePath, IProject project, String errorPrefix, String successMessage)
    {
        final ProcessBuilder formattingBuilder = FormattingUtils.createFormattingProcess(filePath);

        // abort if project is missing
        if (project == null)
            return FeedbackMessage.CreateError(AStyleHandlerConstants.ERROR_NO_PROJECT);

        // abort if file to be formatted is missing
        if (filePath == null)
            return FeedbackMessage.CreateError(AStyleHandlerConstants.ERROR_NO_FILE);

        // abort if any path is missing
        if (formattingBuilder == null)
            return FeedbackMessage.CreateError(
                       String.format(AStyleHandlerConstants.ERROR_NO_PATH, errorPrefix));

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
                               errorPrefix,
                               returnCode));
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return FeedbackMessage.CreateError(
                       String.format(AStyleHandlerConstants.ERROR_GENERIC, errorPrefix));
        }

        // try to refresh the project's changed files
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);

        } catch (CoreException e) {
            e.printStackTrace();
        }

        return FeedbackMessage.CreateInfo(String.format(successMessage, processOutput));
    }


    /**
     * Returns a process builder for running a formatting process.
     * If the current project uses the HarvesterUtils astyle-format script,
     * this script is preferred over the plugin preferences.
     *
     * @param filePath the absolute filepath to the folder or file that is to be formatted
     *
     * @return a formatting process builder
     */
    public static ProcessBuilder createFormattingProcess(String filePath)
    {
        // add * to folders in order to format recursively
        if (new File(filePath).isDirectory()) {
            if (filePath.charAt(filePath.length() - 1) != File.separatorChar)
                filePath += File.separatorChar;

            filePath += '*';
        }

        // if the project has a formatting util script, use that instead
        final int sourceIndex = filePath.indexOf("src");
        final String projectPath =
            (sourceIndex > 0)
            ? filePath.substring(0, filePath.indexOf("src") - 1)
            : filePath;
        final File formattingUtilScript = new File(projectPath + AStyleHandlerConstants.HARVESTER_FORMATTING_SCRIPT);

        if (formattingUtilScript.exists())
            return createHarvesterFormattingProcess(filePath, formattingUtilScript);
        else
            return createDefaultFormattingProcess(filePath);
    }



    /**
     * Returns a process builder for running the scripts/formatting/astyle-format
     * script.
     *
     * @param filePath the absolute path of the file that is to be formatted
     * @param formattingScript the script that is to be executed
     *
     * @return a process builder for formatting the active project
     */
    private static ProcessBuilder createHarvesterFormattingProcess(String filePath, File formattingScript)
    {
        final String scriptFullPath = formattingScript.getAbsolutePath();

        final ProcessBuilder pb = new ProcessBuilder(scriptFullPath);
        pb.directory(formattingScript.getParentFile().getParentFile());

        return pb;
    }


    /**
     * Returns a process builder for running formatting the project using the
     * formatter defined via the plugin preferences.
     *
     * @param unescapedFilePath the absolute path of the folder or file that is to be formatted
     *
     * @return a process builder for formatting the active project
     */
    private static ProcessBuilder createDefaultFormattingProcess(String unescapedFilePath)
    {
        final String filePath =
            unescapedFilePath.replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // get astyle binary path from preferences
        final String binPath = AStylePreferenceConstants.STORE
                               .getString(AStylePreferenceConstants.BINARY_PATH_OPTION)
                               .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // get astyle options ini path from preferences
        final String optionsPath = AStylePreferenceConstants.STORE
                                   .getString(AStylePreferenceConstants.OPTIONS_FILE_PATH_OPTION)
                                   .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // return null if a required path is missing
        if (binPath.isEmpty() || optionsPath.isEmpty())
            return null;

        // add recursion flag if the filepath points to a folder
        else if (filePath.charAt(filePath.length() - 1) == '*')
            return new ProcessBuilder(
                       String.format(AStyleHandlerConstants.ASTYLE_BIN_CMD, binPath),
                       AStyleHandlerConstants.RECURSIVE_CMD_PARAM,
                       AStyleHandlerConstants.NO_BACKUP_CMD_PARAM,
                       AStyleHandlerConstants.ONLY_FORMATTED_CMD_PARAM,
                       String.format(AStyleHandlerConstants.OPTIONS_CMD_PARAM, optionsPath),
                       filePath + AStyleHandlerConstants.JAVA_FILE_EXTENSION
                   );
        else
            return new ProcessBuilder(
                       String.format(AStyleHandlerConstants.ASTYLE_BIN_CMD, binPath),
                       AStyleHandlerConstants.NO_BACKUP_CMD_PARAM,
                       AStyleHandlerConstants.ONLY_FORMATTED_CMD_PARAM,
                       String.format(AStyleHandlerConstants.OPTIONS_CMD_PARAM, optionsPath),
                       filePath
                   );
    }
}
