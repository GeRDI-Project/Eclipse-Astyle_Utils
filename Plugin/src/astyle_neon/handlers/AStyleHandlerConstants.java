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

import java.io.File;

/**
 * This class offers constants that are used by the {@linkplain FormattingHandler}.
 *
 * @author Robin Weiss
 */
public class AStyleHandlerConstants
{
    public static final String WHITESPACE_ESCAPE =  "\\ ";

    public static final String ASTYLE_NAME = "AStyle";
    public static final String EXPLORER_SUFFIX = "Explorer";
    public static final String ERROR_NO_PROJECT = "You need to select a project from the Project Explorer, or open a file that belongs to a project before formatting!";
    public static final String ERROR_NO_PATH = "Could not format Project '%s':%nPlease, specify the AStyle paths in the preferences.";
    public static final String ERROR_GENERIC = "Could not format Project '%s':%nAn error occurred during the formatting process.";
    public static final String ERROR_RETURN = "%s%n%n%s%n%nCould not format Project '%s':%nReturn code: %d";
    public static final String SUCCESS = "%s%n%nFormatted all files in Project '%s'!";
    public static final String SUCCESS_REFRESH = SUCCESS + "\nYou need to refresh your Project!";
    public static final String PROJECT_SOURCE_DIRECTORY = "src";

    public static final String TARGET_FOLDER_CMD = "%s" + File.separatorChar + "\\*";
    public static final String ASTYLE_BIN_CMD = "%s" + File.separatorChar + "astyle";

    public static final String OPTIONS_CMD_PARAM = "--options=%s";
    public static final String RECURSIVE_CMD_PARAM = "--recursive";
    public static final String NO_BACKUP_CMD_PARAM = "--suffix=none";
    public static final String ONLY_FORMATTED_CMD_PARAM = "--formatted";

    public static final String HARVESTER_FORMATTING_SCRIPT = getHarvesterFormattingScriptLocation();

    /**
     * Private constructor, because this is just a collection of constants.
     */
    private AStyleHandlerConstants()
    {

    }

    /**
     * Returns the location of a possible harvester formatting script.
     *
     * @return a batch script path if the OS is Windows based, else a shell script path
     */
    private static String getHarvesterFormattingScriptLocation()
    {
        if (System.getProperty("os.name").contains("Windows"))
            return "/scripts/formatting/astyle-format.bat";
        else
            return "/scripts/formatting/astyle-format.sh";
    }
}
