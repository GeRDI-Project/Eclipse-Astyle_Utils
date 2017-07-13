package astyle_neon.preferences;


import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import astyle_neon.Activator;

/**
 * The AStyle preference page that can be found in Window > Preferences > AStyle.
 * @author Robin Weiss
 *
 */
public class AStylePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private final static String TITLE = "AStyle";
    private final static String DESCRIPTION = "ArtisticStyle formatter options";

    public final static String OPTIONS_FILE_PATH_OPTION = "PATH_OPTIONS";
    private final static String[] OPTIONS_FILE_PATH_FILTER = { "*.ini" };
    private final static String OPTIONS_FILE_PATH_LABEL = "&AStyle options file:";

    public final static String BINARY_PATH_OPTION = "PATH_BIN";
    private final static String BINARY_PATH_LABEL = "&AStyle bin directory:";


    /**
     * Simple constructor.
     */
    public AStylePreferencePage()
    {
        super(GRID);
    }


    @Override
    public String getTitle()
    {
        return TITLE;
    }


    @Override
    public void init(IWorkbench arg0)
    {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(DESCRIPTION);
    }


    @Override
    protected void createFieldEditors()
    {
        addField(createBinaryPathEditor());
        addField(createOptionsPathEditor());
    }

    /**
     * Creates an editable, browsable field, that aims to select the AStyle bin folder.
     * @return the UI component of the editable field
     */
    private DirectoryFieldEditor createBinaryPathEditor()
    {
        DirectoryFieldEditor binaryField = new DirectoryFieldEditor(
            BINARY_PATH_OPTION,
            BINARY_PATH_LABEL,
            getFieldEditorParent());

        return binaryField;
    }

    /**
     * Creates an editable, browsable field, that aims to select an AStyle options file.
     * @return the UI component of the editable field
     */
    private FileFieldEditor createOptionsPathEditor()
    {
        FileFieldEditor optionsField = new FileFieldEditor(
            OPTIONS_FILE_PATH_OPTION,
            OPTIONS_FILE_PATH_LABEL,
            getFieldEditorParent());

        optionsField.setFileExtensions(OPTIONS_FILE_PATH_FILTER);
        return optionsField;
    }
}
