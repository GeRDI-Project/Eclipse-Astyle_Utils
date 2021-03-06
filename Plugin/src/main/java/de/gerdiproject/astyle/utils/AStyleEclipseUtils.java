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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import de.gerdiproject.astyle.handlers.AStyleHandlerConstants;

/**
 * This helper class offers static methods for retrieving Eclipse UI elements.
 *
 * @author Robin Weiss
 */
public class AStyleEclipseUtils
{
    /**
     * Private constructor, because this is just a collection of useful methods.
     */
    private AStyleEclipseUtils()
    {

    }


    /**
     * Retrieves the Eclipse action bars.
     *
     * @param event the event that triggered the message
     * @return Eclipse action bars
     */
    public static IActionBars getActionBars(ExecutionEvent event)
    {
        final IEditorPart editor = getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();

        // check if there is an active editor
        if (editor != null)
            return editor.getEditorSite().getActionBars();
        else {
            IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
            return ((IViewSite) activePart.getSite()).getActionBars();
        }
    }


    /**
     * Attempts to execute an Eclipse command.
     *
     * @param commandName the name of the command
     *
     * @return true if the command could be executed
     */
    public static boolean executeCommand(String commandName)
    {
        final IHandlerService handlerService = getActiveWorkbenchWindow(null).getService(IHandlerService.class);

        if (handlerService != null) {
            try {
                handlerService.executeCommand(commandName, null);
                return true;
            } catch (Exception ex) {
                // do nothing
            }
        }

        return false;
    }


    /**
     * Retrieves the file path of the file that is selected in the Project
     * Explorer. If the Editor is focussed, retrieves the path of the edited
     * file.
     *
     * @return the file path, or null if there is no active editor and nothing
     *         selected in the project explorer
     */
    public static String getFilePathOfSelectedFile(ExecutionEvent event)
    {
        // check which window is focussed
        boolean isExplorerFocussed =
            HandlerUtil.getActivePartId(event).endsWith(AStyleHandlerConstants.EXPLORER_SUFFIX);
        IWorkbenchWindow window = getActiveWorkbenchWindow(event);

        // try to get the file that is focussed in the Explorer
        if (isExplorerFocussed) {
            // get selection
            final IStructuredSelection selection =
                (IStructuredSelection) window.getSelectionService().getSelection();

            if (!selection.isEmpty()) {
                IAdaptable adaptable = (IAdaptable) selection.getFirstElement();

                if (adaptable != null)
                    return adaptable.getAdapter(IResource.class).getLocation().toOSString();
            }
        }

        final IEditorPart activeEditor =
            window.getActivePage().getActiveEditor();

        if (activeEditor != null)
            return activeEditor.getEditorInput().getAdapter(IResource.class).getLocation().toOSString();
        else
            return null;
    }


    /**
     * Retrieves the currently active Project in Eclipse via an event.
     *
     * @param event the event that triggered the command
     * @return a project that is associated with a currently selected file, or
     *         null if the project could not be retrieved
     */
    public static IProject getActiveProject(ExecutionEvent event)
    {
        IProject currentProject = null;

        // check which window is focussed
        boolean isExplorerFocussed =
            HandlerUtil.getActivePartId(event).endsWith(AStyleHandlerConstants.EXPLORER_SUFFIX);
        final IWorkbenchWindow window = getActiveWorkbenchWindow(event);

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


    /**
     * Returns the active workbench window.
     *
     * @param event the event for which this window is required, or null if no
     *            event was fired
     *
     * @return the active workbench window
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow(ExecutionEvent event)
    {
        if (event != null) {
            try {
                return HandlerUtil.getActiveWorkbenchWindowChecked(event);
            } catch (ExecutionException ex) {
                // do nothing, this case will be handled below
            }
        }

        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
}
