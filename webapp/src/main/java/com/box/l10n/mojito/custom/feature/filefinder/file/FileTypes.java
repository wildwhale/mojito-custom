package com.box.l10n.mojito.custom.feature.filefinder.file;

/**
 * Enum of {@link FileType} supported.
 *
 * @author jaurambault
 */
public enum FileTypes {

    XLIFF(XliffFileType.class),
    XCODE_XLIFF(XcodeXliffFileType.class),
    ANDROID_STRINGS(AndroidStringsFileType.class),
    MAC_STRING(MacStringsFileType.class),
    MAC_STRINGSDICT(MacStringsdictFileType.class),
    PROPERTIES(PropertiesFileType.class),
    PROPERTIES_NOBASENAME(PropertiesNoBasenameFileType.class),
    PROPERTIES_JAVA(PropertiesJavaFileType.class),
    RESW(ReswFileType.class),
    RESX(ResxFileType.class),
    PO(POFileType.class),
    XTB(XtbFileType.class),
    CSV(CSVFileType.class),
    JS(JSFileType.class),
    JSON(JSONFileType.class),
    JSON_NOBASENAME(JSONNoBasenameFileType.class),
    CHROME_EXT_JSON(ChromeExtensionJSONFileType.class),
    TS(TSFileType.class);

    Class<? extends FileType> clazz;

    FileTypes(Class<? extends FileType> clazz) {
        this.clazz = clazz;
    }

    /**
     * Gets the a {@link FileType} instance.
     *
     * @return
     */
    public FileType toFileType() {

        FileType fileType;

        try {
            fileType = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Can't create FileType", e);
        }

        return fileType;
    }

}