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


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import astyle_neon.AStyleEclipseUtils;

import org.eclipse.core.resources.IProject;

/**
 * The handler for the  AStyle_Neon.commands.formatProjectCommand.
 * This command attempts to find out which project is currently being worked on,
 * and formats all files of this project.
 *
 * @author Robin Weiss
 *
 */
public final class FormatProjectHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // format project and memorize the status message
        final FeedbackMessage statusMessage = formatProject(event);

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
    private FeedbackMessage formatProject(ExecutionEvent event)
    {
        final IProject project = AStyleEclipseUtils.getActiveProject(event);

        // the project is needed for retrieving the filepath. Abort if it could not be retrieved
        if (project != null) {
            final String filePath = project.getFolder(AStyleHandlerConstants.PROJECT_SOURCE_DIRECTORY)
                                    .getLocation()
                                    .toOSString();
            final String successPrefix = String.format(AStyleHandlerConstants.CAN_FORMAT_PROJECT, filePath);
            final String errorPrefix = String.format(AStyleHandlerConstants.CANNOT_FORMAT_PROJECT, filePath);

            return FormattingUtils.format(filePath, project, errorPrefix, successPrefix);
        } else
            return FeedbackMessage.CreateError(AStyleHandlerConstants.ERROR_NO_PROJECT);
    }
}
