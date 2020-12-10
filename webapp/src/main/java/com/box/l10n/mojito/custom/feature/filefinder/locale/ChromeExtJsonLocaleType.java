package com.box.l10n.mojito.custom.feature.filefinder.locale;

/**
 * {@link LocaleType} implementation for Chrome extension. Use "_" in locale names.
 *
 * @author jaurambault
 */
public class ChromeExtJsonLocaleType extends AnyLocaleTargetNotSourceType {
    public String getTargetLocaleRepresentation(String targetLocale) {
        return targetLocale.replace("-", "_");
    }
}
