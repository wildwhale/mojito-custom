package com.box.l10n.mojito.custom.feature.filefinder.locale;

/**
 * Generic {@link LocaleType} implementation, accepts any string as locale.
 *
 * @author jaurambault
 */
public class AnyLocaleType extends LocaleType {

    @Override
    public String getTargetLocaleRegex() {
        return ".+?";
    }

}
