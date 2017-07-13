package astyle_neon.handlers;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

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
    private static final String EXPLORER_SUFFIX = "Explorer";
    private static final String ERROR_NO_PROJECT = "You need to select a project from the Project Explorer, or open a file that belongs to a project before formatting!";
    private static final String ERROR_NO_PATH = "Could not format Project '%s':\nPlease, specify the AStyle paths in the preferences.";
    private static final String ERROR_GENERIC = "Could not format Project '%s':\nAn error occurred during the formatting process.";
    private static final String ERROR_RETURN = "%s\n\n%s\n\nCould not format Project '%s':\nReturn code: %d";
    private static final String SUCCESS = "%s\n\nFormatted all files in Project '%s'!";
    private static final String SUCCESS_REFRESH = SUCCESS + "\nYou need to refresh your Project!";
    private static final String PROJECT_SOURCE_DIRECTORY = "src";

    private static final String TARGET_FOLDER_CMD = "\"%s" + File.separatorChar + "*\"";
    private static final String ASTYLE_BIN_CMD = "%s" + File.separatorChar + "astyle";
    private static final String OPTIONS_CMD_PARAM = "--options=\"%s\"";
    private static final String RECURSIVE_CMD_PARAM = "--recursive";
    private static final String NO_BACKUP_CMD_PARAM = "--suffix=none";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // get window and project
        IProject currentProject = getActiveProject(event);

        // format only if we found an active project
        String statusMessage;

        if (currentProject != null) {
            // format project and memorize the status message
            statusMessage = formatProject(currentProject);
        } else
            statusMessage = ERROR_NO_PROJECT;

        // notify the user about the status
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        System.out.println( statusMessage );
        MessageDialog.openInformation(window.getShell(), ASTYLE_NAME, statusMessage);

        return null;
    }


    /**
     * Formats all source files within a project
     *
     * @param project
     *            the project of which the source files are formatted
     * @return a message describing the status of the formatting operation
     */
    private String formatProject(IProject project)
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
        /*String[] formattingCommand = {
            String.format(ASTYLE_BIN_CMD, binPath),
            RECURSIVE_CMD_PARAM,
            NO_BACKUP_CMD_PARAM,
            String.format(OPTIONS_CMD_PARAM, optionsPath),
            String.format(TARGET_FOLDER_CMD, sourcePath)
        };
        
        // log process
        System.out.println( "Executing:\n" + String.join( " ", formattingCommand ));*/
        
        String processOutput;
        try {
        	File testOptions = new File( optionsPath);
        	System.out.println( "exists? " +testOptions.exists() );
        	System.out.println(testOptions.getAbsolutePath() );
        	System.out.println(testOptions.getPath() );
        	System.out.println("readable? " + testOptions.canRead() );
        	System.out.println("executable? " + testOptions.canExecute() );
        	
        	
            // execute formatting command
        	ProcessBuilder pb = new ProcessBuilder(
        			String.format(ASTYLE_BIN_CMD, binPath),
                    RECURSIVE_CMD_PARAM,
                    NO_BACKUP_CMD_PARAM,
                    String.format(OPTIONS_CMD_PARAM, testOptions.getAbsolutePath()),
                    "--verbose",
                    String.format(TARGET_FOLDER_CMD, sourcePath)
			);
            Process formattingProcess = pb.start();
            //Runtime.getRuntime().exec(formattingCommand);
            
            // read returned string
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(formattingProcess.getInputStream()));
            int returnCode = formattingProcess.waitFor();
            
            processOutput = outputReader.lines().collect( Collectors.joining("\n") );
            
            if (returnCode != 0)
            {
            	// read returned error string
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(formattingProcess.getErrorStream()));
                String errorOutput = errorReader.lines().collect( Collectors.joining("\n") );
                
                return String.format(ERROR_RETURN, processOutput, errorOutput, project.getName(), returnCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return String.format(ERROR_GENERIC, project.getName());
        }

        // try to refresh the project's changed files
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
            return String.format(SUCCESS_REFRESH, processOutput, project.getName());
        }

        return String.format(SUCCESS, processOutput, project.getName());
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
        boolean isExplorerFocussed = HandlerUtil.getActivePartId(event).endsWith(EXPLORER_SUFFIX);
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        // get an adaptable in order to retrieve its project
        IAdaptable adaptable = null;

        if (isExplorerFocussed) {
            // get selection
            IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();

            if (!selection.isEmpty())
                adaptable = (IAdaptable) selection.getFirstElement();
        }

        // fallback: choose the project of the active editor
        if (adaptable == null) {
            // get active editor
            IEditorPart activeEditor = window.getActivePage().getActiveEditor();

            if (activeEditor != null)
                adaptable = activeEditor.getEditorInput();
        }

        if (adaptable != null) {
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
