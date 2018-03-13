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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.commands.ICommandService;

import astyle_neon.Activator;
import astyle_neon.preferences.AStylePreferenceConstants;

/**
 * This listener listens to preference changes and toggles the Autoformatting behavior accordingly.
 *
 * @author Robin Weiss
 */
public class AutoFormatChangedListener implements IPropertyChangeListener
{
    private SaveListener saveListener;


    /**
     * Initializes the listener by checking the current state of the preferences.
     */
    public AutoFormatChangedListener()
    {
        saveListener = null;
        refreshSaveListener();
    }


    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(AStylePreferenceConstants.AUTO_FORMAT_OPTION))
            refreshSaveListener();
    }


    /**
     * Adds or removes a {@linkplain SaveListener}, depending
     * on the AUTO_FORMAT preference.
     */
    private void refreshSaveListener()
    {
        final boolean newState = isAutoFormatPreferenceEnabled();

        final ICommandService service =
            (ICommandService) Activator.getDefault()
            .getWorkbench()
            .getService(ICommandService.class);

        // if the state changed, add or remove the save listener
        if (saveListener == null && newState) {
            saveListener = new SaveListener();
            service.addExecutionListener(saveListener);

        } else if (saveListener != null && !newState) {
            service.removeExecutionListener(saveListener);
            saveListener = null;
        }
    }


    /**
     * Returns true if auto formatting is enabled.
     *
     * @return true if auto formatting is enabled
     */
    private boolean isAutoFormatPreferenceEnabled()
    {
        return AStylePreferenceConstants.STORE.getBoolean(AStylePreferenceConstants.AUTO_FORMAT_OPTION);
    }
}
