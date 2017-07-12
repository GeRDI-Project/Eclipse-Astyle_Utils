package astyle_neon.handlers;


import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import astyle_neon.Activator;
import astyle_neon.preferences.AStylePreferencePage;

import org.eclipse.jface.dialogs.MessageDialog;
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
public class FormattingHandler extends AbstractHandler
{
    private static final String ASTYLE_NAME = "AStyle";
    private static final String ERROR_NO_PROJECT = "You need to select a project from the Project Explorer, or open a file that belongs to a project before formatting!";
    private static final String ERROR_NO_PATH = "Could not format Project '%s':\nPlease, specify the AStyle paths in the preferences.";
    private static final String ERROR_GENERIC = "Could not format Project '%s':\nAn error occurred during the formatting process.";
    private static final String SUCCESS = "Formatted all files in Project '%s'!";
    private static final String SUCCESS_REFRESH = SUCCESS + "\nYou need to refresh your Project!";
    private static final String PROJECT_SOURCE_DIRECTORY = "src";
    private static final String FORMATTING_COMMAND = "%s" + File.separatorChar + "astyle --options=\"%s\" --recursive --suffix=none \"%s" + File.separatorChar + "*\"";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // get window and project
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IProject currentProject = getActiveProject(window);

        // format only if we found an active project
        String statusMessage;

        if (currentProject != null) {
            // format project and memorize the status message
            statusMessage = formatProject(currentProject, window);
        } else
            statusMessage = ERROR_NO_PROJECT;

        // notify the user about the status
        MessageDialog.openInformation(window.getShell(), ASTYLE_NAME, statusMessage);

        return null;
    }


    /**
     * Formats all source files within a project
     *
     * @param project
     *            the project of which the source files are formatted
     * @param window
     *            the window from where the event was triggered
     * @return a message describing the status of the formatting operation
     */
    private String formatProject(IProject project, IWorkbenchWindow window)
    {
        // get astyle binary path from preferences
        String binPath = Activator.getDefault().getPreferenceStore()
                         .getString(AStylePreferencePage.BINARY_PATH_OPTION);

        // get astyle options ini path from preferences
        String optionsPath = Activator.getDefault()
                             .getPreferenceStore()
                             .getString(AStylePreferencePage.OPTIONS_FILE_PATH_OPTION);

        if (binPath.isEmpty() || optionsPath.isEmpty())
            return String.format(ERROR_NO_PATH, project.getName());

        // get source path from the project
        String sourcePath = project.getFolder(PROJECT_SOURCE_DIRECTORY).getLocation().toOSString();

        // assemble command
        String scriptCommand = String.format(FORMATTING_COMMAND, binPath, optionsPath, sourcePath);

        try {
            // execute formatting command
            Process formattingProcess = Runtime.getRuntime().exec(scriptCommand);
            formattingProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return String.format(ERROR_GENERIC, project.getName());
        }

        // try to refresh the project's changed files
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
            return String.format(SUCCESS_REFRESH, project.getName());
        }

        return String.format(SUCCESS, project.getName());
    }


    /**
     * Retrieves the currently active Project in Eclipse.
     *
     * @param window
     *            the window from where the event was triggered
     * @return a project thatis associated with a currently selected file or
     *         editor view
     */
    private IProject getActiveProject(IWorkbenchWindow window)
    {
        IProject currentProject = null;

        // get selection
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();

        // get active editor
        IEditorPart activeEditor = window.getActivePage().getActiveEditor();

        // check if something is selected or an active editor exists
        if ((selection != null && !selection.isEmpty()) || activeEditor != null) {
            // get some adaptable object
            IAdaptable adaptable = selection == null || selection.isEmpty()
                                   ? activeEditor.getEditorInput()
                                   : (IAdaptable) selection.getFirstElement();

            // retrieve current project from adaptable
            currentProject = adaptable.getAdapter(IProject.class);

            // fallback: get a resource from the adaptable and then its project
            if (currentProject == null) {
                IResource resource = adaptable.getAdapter(IResource.class);
                if (resource != null)
                    currentProject = resource.getProject();
            }
        }

        return currentProject;
    }
}
