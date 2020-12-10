package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.ChromeExtJsonLocaleType;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;


/**
 * @author jeanaurambault
 */
public class ChromeExtensionJSONFileType extends FileType {

    public ChromeExtensionJSONFileType() {
        this.sourceFileExtension = "json";
        this.baseNamePattern = "messages";
        this.subPath = "_locales";
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + SUB_PATH + "}" + PATH_SEPERATOR + "{" + LOCALE + "}" + PATH_SEPERATOR + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new ChromeExtJsonLocaleType();
        this.textUnitNameToTextUnitNameInSourceSingular = Pattern.compile("(?<s>.*)/message");
        this.textUnitNameToTextUnitNameInSourcePlural = Pattern.compile("(?<s>.*)"); // plural not support just accept anything
        this.defaultFilterOptions = Arrays.asList("noteKeyPattern=description", "extractAllPairs=false", "exceptions=message");
    }
}
