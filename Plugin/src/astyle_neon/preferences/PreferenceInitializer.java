package astyle_neon.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import astyle_neon.Activator;

/**
 * This initializer initializes the AStyle preferences with empty strings.
 * @author Robin Weiss
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

    @Override
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        store.setDefault(AStylePreferencePage.BINARY_PATH_OPTION, "");
        store.setDefault(AStylePreferencePage.OPTIONS_FILE_PATH_OPTION, "");
    }

}
