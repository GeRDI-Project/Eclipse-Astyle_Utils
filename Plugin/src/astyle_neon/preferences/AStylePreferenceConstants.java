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
package astyle_neon.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import astyle_neon.Activator;

/**
 * This class offers constants that are used for setting and retrieving AStyle preferences.
 *
 * @author Robin Weiss
 */
public class AStylePreferenceConstants
{
    public final static IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();

    public final static String PREFERENCES_TITLE = "AStyle";
    public final static String PREFERENCES_DESCRIPTION = "ArtisticStyle formatter options";

    public final static String OPTIONS_FILE_PATH_OPTION = "PATH_OPTIONS";
    public final static String[] OPTIONS_FILE_PATH_FILTER = { "*.ini" };
    public final static String OPTIONS_FILE_PATH_LABEL = "AStyle &options file:";

    public final static String BINARY_PATH_OPTION = "PATH_BIN";
    public final static String BINARY_PATH_LABEL = "AStyle &bin directory:";

    public final static String AUTO_FORMAT_OPTION = "AUTO_FORMAT";
    public final static String AUTO_FORMAT_LABEL = "&Format on save:";

    public final static String FEEDBACK_STYLE_OPTION = "FEEDBACK_STYLE";
    public final static String FEEDBACK_STYLE_LABEL = "&Formatting Feedback:";

    /**
     * Private constructor, because this is just a collection of constants.
     */
    private AStylePreferenceConstants()
    {

    }
}
