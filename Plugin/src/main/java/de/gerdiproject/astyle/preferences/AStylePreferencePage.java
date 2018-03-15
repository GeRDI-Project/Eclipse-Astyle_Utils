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
package de.gerdiproject.astyle.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * The AStyle preference page that can be found in Window > Preferences > AStyle.
 *
 * @author Robin Weiss
 *
 */
public class AStylePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
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
        return AStylePreferenceConstants.PREFERENCES_TITLE;
    }


    @Override
    public void init(IWorkbench arg0)
    {
        setPreferenceStore(AStylePreferenceConstants.STORE);
        setDescription(AStylePreferenceConstants.PREFERENCES_DESCRIPTION);
    }


    @Override
    protected void createFieldEditors()
    {
        addField(createBinaryPathEditor());
        addField(createOptionsPathEditor());
        addField(createAutoFormatCheckbox());
        addField(createFeedbackStyleRadioButtons());
    }


    /**
     * Creates an editable, browsable field, that aims to select the AStyle bin folder.
     *
     * @return the UI component of the editable field
     */
    private DirectoryFieldEditor createBinaryPathEditor()
    {
        return new DirectoryFieldEditor(
                   AStylePreferenceConstants.BINARY_PATH_OPTION,
                   AStylePreferenceConstants.BINARY_PATH_LABEL,
                   getFieldEditorParent());
    }


    /**
     * Creates an editable, browsable field, that aims to select an AStyle options file.
     *
     * @return the UI component of the editable field
     */
    private FileFieldEditor createOptionsPathEditor()
    {
        FileFieldEditor optionsField = new FileFieldEditor(
            AStylePreferenceConstants.OPTIONS_FILE_PATH_OPTION,
            AStylePreferenceConstants.OPTIONS_FILE_PATH_LABEL,
            getFieldEditorParent());

        optionsField.setFileExtensions(AStylePreferenceConstants.OPTIONS_FILE_PATH_FILTER);
        return optionsField;
    }


    /**
     * Creates an a check box for toggling the auto-format flag.
     *
     * @return the UI component of the check box
     */
    private BooleanFieldEditor createAutoFormatCheckbox()
    {
        return new BooleanFieldEditor(
                   AStylePreferenceConstants.AUTO_FORMAT_OPTION,
                   AStylePreferenceConstants.AUTO_FORMAT_LABEL,
                   getFieldEditorParent());
    }


    /**
     * Creates radio buttons for selecting the means of displaying feedback messages.
     *
     * @return the UI component of the radio group
     */
    private RadioGroupFieldEditor createFeedbackStyleRadioButtons()
    {
        // create an array of radio group options
        final FeedbackStyle[] rawValues = FeedbackStyle.values();
        final String[][] radioValues = new String[rawValues.length][2];
        int i = rawValues.length;

        while (i != 0) {
            i--;
            radioValues[i][0] = rawValues[i].getDisplayName();
            radioValues[i][1] = rawValues[i].toString();
        }

        return new RadioGroupFieldEditor(
                   AStylePreferenceConstants.FEEDBACK_STYLE_OPTION,
                   AStylePreferenceConstants.FEEDBACK_STYLE_LABEL,
                   1,
                   radioValues,
                   getFieldEditorParent());
    }


}
