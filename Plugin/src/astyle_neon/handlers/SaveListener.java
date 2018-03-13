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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;

import astyle_neon.AStyleEclipseUtils;

/**
 * This listener reacts to save commands and triggers a formatting process.
 *
 * @author Robin Weiss
 */
public class SaveListener implements IExecutionListener
{
    @Override
    public void postExecuteSuccess(final String action, final Object arg1)
    {
        switch (action) {
            case AStyleHandlerConstants.SAVE_COMMAND:
            case AStyleHandlerConstants.SAVE_AS_COMMAND:
                AStyleEclipseUtils.executeCommand(AStyleHandlerConstants.FORMAT_FILE_COMMAND);
                break;

            case AStyleHandlerConstants.SAVE_ALL_COMMAND:
                AStyleEclipseUtils.executeCommand(AStyleHandlerConstants.FORMAT_PROJECT_COMMAND);
                break;
        }
    }


    @Override
    public void notHandled(String arg0, NotHandledException arg1)
    {
    }


    @Override
    public void postExecuteFailure(String arg0, ExecutionException arg1)
    {
    }


    @Override
    public void preExecute(String arg0, ExecutionEvent arg1)
    {
    }
}
