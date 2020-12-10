package com.box.l10n.mojito.rest.textunit;

import com.box.l10n.mojito.rest.EntityWithIdNotFoundException;

/**
 *
 * @author jeanaurambault
 */
public class TextUnitWithIdNotFoundException extends EntityWithIdNotFoundException {

    public TextUnitWithIdNotFoundException(String name) {
        super("AssetTextUnit", name);
    }
}
