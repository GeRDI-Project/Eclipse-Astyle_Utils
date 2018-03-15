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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.gerdiproject.astyle.Activator;

/**
 * This initializer initializes the AStyle preferences with default values.
 *
 * @author Robin Weiss
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

    @Override
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        store.setDefault(AStylePreferenceConstants.BINARY_PATH_OPTION, "");
        store.setDefault(AStylePreferenceConstants.OPTIONS_FILE_PATH_OPTION, "");
        store.setDefault(AStylePreferenceConstants.AUTO_FORMAT_OPTION, false);
        store.setDefault(AStylePreferenceConstants.FEEDBACK_STYLE_OPTION, FeedbackStyle.TextBox.toString());
    }

}
