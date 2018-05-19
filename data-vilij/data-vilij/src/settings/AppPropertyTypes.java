package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    CSS_PATH,
    USER_DIRECTORY,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    SETTINGS_ICON,
    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    EXIT_TITLE,
    EXIT_WHILE_RUNNING_WARNING,
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    DEFAULT_ERROR_TITLE,
    
    SAVE_UNSAVED_WORK_TITLE,
    INPUT_TITLE,
    CLEAR_INTERFACE_TITLE,
    
    SAVE_DIALOG_TITLE,
    LOAD_DIALOG_TITLE,
    SCREENSHOT_DIALOG_TITLE,

    DATA_DISPLAY_FAIL_TITLE,
    
    LOADED_MANY_LINES_DIALOG_TITLE,
    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    INPUT,
    
    SAVE_FILE_ERROR,
    READ_DATA_FAIL,
    NEW_FILE_ERROR,
    EXIT_APP_ERROR,
    SCREENSHOT_FILE_ERROR,
    SCREENSHOT_OUTPUT_ERROR,
    CLEAR_INTERFACE_DESC,
    
    LOADED_MANY_LINES_DESC_1,
    LOADED_MANY_LINES_DESC_2,

    /* application-specific parameters */
    FONT,
    TEXTBOX_TITLE,
    ALGORITHM_TYPES_TITLE,
    ALGORITHM_LIST_ID,
    
    ALGORITHM_TITLE_ID,
    SETTINGS_CSS_CLASS,
    
    CLASSIFICATION,
    CLASSIFICATION_ID,
    CLASSIFICATION_OPTION_ONE,
    
    CLUSTERING,
    CLUSTERING_ID,
    CLUSTERING_OPTION_ONE,
    
    
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    
    SCREENSHOT_FILE_EXT,
    IMAGE_FILE_TYPE_NAME,
    IMAGE_FILE_TYPE_PARAM,
    
    DEFAULT_FILE_NAME,
    TEXT_AREA,
    SPECIFIED_FILE,
    
    DISPLAY_NAME,
    DONE_BUTTON_NAME,
    EDIT_BUTTON_NAME,
    RUN_BUTTON_NAME,
    
    READ_ONLY,
    CHART_TITLE,
    
    INSTANCE_COUNT_INFO,
    LABEL_COUNT_INFO,
    LABEL_NAME_INFO,
    SOURCE_INFO,
    
    ALGO_SETTINGS_TITLE,
    ITERATIONS_TITLE,
    INTERVALS_TITLE
}
