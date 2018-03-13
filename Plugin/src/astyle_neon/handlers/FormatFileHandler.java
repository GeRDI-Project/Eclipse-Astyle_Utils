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
import astyle_neon.AStyleEclipseUtils;
import org.eclipse.core.resources.IProject;

/**
 * The handler for the AStyle_Neon.commands.formatFileCommand.
 * This handler attempts to find out which file is currently being viewed and formats it.
 *
 * @author Robin Weiss
 *
 */
public final class FormatFileHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event)
    {
        // format file and memorize the status message
        final FeedbackMessage statusMessage = formatFile(event);

        // notify the user about the status
        statusMessage.display(event);

        return null;
    }


    /**
     * Formats a the currently active file.
     *
     * @param event the event that triggered the formatting
     *
     * @return a feedback message of the formatting process
     */
    public FeedbackMessage formatFile(ExecutionEvent event)
    {
        final IProject project = AStyleEclipseUtils.getActiveProject(event);
        final String filePath = AStyleEclipseUtils.getFilePathOfSelectedFile(event);
        final String successPrefix = String.format(AStyleHandlerConstants.CAN_FORMAT_FILE, filePath);
        final String errorPrefix = String.format(AStyleHandlerConstants.CANNOT_FORMAT_FILE, filePath);

        // execute Eclipse Java formatting
        if (filePath.endsWith(AStyleHandlerConstants.JAVA_FILE_EXTENSION))
            AStyleEclipseUtils.executeCommand(AStyleHandlerConstants.ECLIPSE_FORMAT_JAVA_COMMAND);

        // execute AStyle formatting
        return FormattingUtils.format(filePath, project, errorPrefix, successPrefix);
    }

}
