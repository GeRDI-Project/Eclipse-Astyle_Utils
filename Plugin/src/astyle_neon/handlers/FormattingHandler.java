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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import astyle_neon.preferences.AStylePreferenceConstants;

import org.eclipse.jface.viewers.IStructuredSelection;
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
        IProject currentProject = getActiveProject(event);

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
        // get astyle binary path from preferences
        final String binPath = AStylePreferenceConstants.STORE
                               .getString(AStylePreferenceConstants.BINARY_PATH_OPTION)
                               .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // get astyle options ini path from preferences
        final String optionsPath = AStylePreferenceConstants.STORE
                                   .getString(AStylePreferenceConstants.OPTIONS_FILE_PATH_OPTION)
                                   .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // abort if any path is missing
        if (binPath.isEmpty() || optionsPath.isEmpty())
            return FeedbackMessage.CreateError(
                       String.format(AStyleHandlerConstants.ERROR_NO_PATH, project.getName()));

        // get source path from the project
        final String sourcePath = project
                                  .getFolder(AStyleHandlerConstants.PROJECT_SOURCE_DIRECTORY)
                                  .getLocation()
                                  .toOSString()
                                  .replaceAll(" ", AStyleHandlerConstants.WHITESPACE_ESCAPE);

        // assemble formatting command
        final ProcessBuilder pb = new ProcessBuilder(
            String.format(AStyleHandlerConstants.ASTYLE_BIN_CMD, binPath),
            AStyleHandlerConstants.RECURSIVE_CMD_PARAM,
            AStyleHandlerConstants.NO_BACKUP_CMD_PARAM,
            AStyleHandlerConstants.ONLY_FORMATTED_CMD_PARAM,
            String.format(AStyleHandlerConstants.OPTIONS_CMD_PARAM, optionsPath),
            String.format(AStyleHandlerConstants.TARGET_FOLDER_CMD, sourcePath)
        );

        final String processOutput;

        try {
            // execute command
            final Process formattingProcess = pb.start();
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
     * Retrieves the currently active Project in Eclipse.
     *
     * @param event
     *            the event that triggered the command
     * @return a project that is associated with a currently selected file or
     *         editor view
     * @throws ExecutionException this exception can occur while trying to retrieve the active window
     */
    private IProject getActiveProject(ExecutionEvent event) throws ExecutionException
    {
        IProject currentProject = null;

        // check which window is focussed
        boolean isExplorerFocussed = HandlerUtil.getActivePartId(event).endsWith(AStyleHandlerConstants.EXPLORER_SUFFIX);
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        // get an adaptable in order to retrieve its project
        IAdaptable adaptable = null;

        if (isExplorerFocussed) {
            // get selection
            final IStructuredSelection selection =
                (IStructuredSelection) window.getSelectionService().getSelection();

            if (!selection.isEmpty())
                adaptable = (IAdaptable) selection.getFirstElement();
        }

        // fallback: choose the project of the active editor
        if (adaptable == null) {
            // get active editor
            final IEditorPart activeEditor = window.getActivePage().getActiveEditor();

            if (activeEditor != null)
                adaptable = activeEditor.getEditorInput();
        }

        if (adaptable != null) {
            // retrieve current project from adaptable
            currentProject = adaptable.getAdapter(IProject.class);

            // fallback: get a resource from the adaptable and then its project
            if (currentProject == null) {
                final IResource resource = adaptable.getAdapter(IResource.class);

                if (resource != null)
                    currentProject = resource.getProject();
            }
        }

        return currentProject;
    }
}
