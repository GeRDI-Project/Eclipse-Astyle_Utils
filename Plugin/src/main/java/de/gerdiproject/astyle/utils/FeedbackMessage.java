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
package de.gerdiproject.astyle.utils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import de.gerdiproject.astyle.handlers.AStyleHandlerConstants;
import de.gerdiproject.astyle.preferences.AStylePreferenceConstants;
import de.gerdiproject.astyle.preferences.FeedbackStyle;

/**
 * This class represents a feedback message of AStyle formatting.
 *
 * @author Robin Weiss
 */
public class FeedbackMessage
{
    private final String message;
    private final FeedbackStyle displayStyle;


    /**
     * Creates an error message.
     * @param message the message text
     *
     * @return a TextBox FeedbackMessage
     */
    public static FeedbackMessage CreateError(String message)
    {
        return new FeedbackMessage(message, true);
    }


    /**
     * Creates a regular feedback message.
     * @param message the message text
     *
     * @return a FeedbackMessage
     */
    public static FeedbackMessage CreateInfo(String message)
    {
        return new FeedbackMessage(message, false);
    }


    /**
     * Private constructor that is invoked via static creators.
     * @param message the message that is to be displayed
     * @param isError if true, it is an error message
     */
    private FeedbackMessage(String message, boolean isError)
    {
        this.message = message;

        // errors are always displayed in Textboxes
        if (isError)
            this.displayStyle = FeedbackStyle.TextBox;
        else
            this.displayStyle = FeedbackStyle.valueOf(
                                    AStylePreferenceConstants.STORE.getString(
                                        AStylePreferenceConstants.FEEDBACK_STYLE_OPTION));
    }


    /**
     * Prints a message to the screen either in a text box, to the status bar or not at all.
     *
     * @param event the event that triggered the message
     * @param message the message that is to be printed
     */
    public void display(ExecutionEvent event)
    {
        switch (displayStyle) {
            case TextBox:
                final IWorkbenchWindow window = AStyleEclipseUtils.getActiveWorkbenchWindow(event);

                MessageDialog.openInformation(window.getShell(), AStyleHandlerConstants.ASTYLE_NAME, message);
                break;

            case StatusBar:
                final String lastLineOfMessage = message.substring(message.lastIndexOf('\n') + 1);
                AStyleEclipseUtils.getActionBars(event).getStatusLineManager().setMessage(lastLineOfMessage);
                break;

            case Disabled:
            default:
                // do nothing
        }
    }
}
