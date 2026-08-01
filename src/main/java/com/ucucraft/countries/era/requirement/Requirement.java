package com.ucucraft.countries.era.requirement;

import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.model.Country;

public interface Requirement {

    String type();

    boolean isMet(Country country);

    /** Human readable progress, e.g. "128/256". */
    String progress(Country country);

    /** Lang key used to describe this requirement. */
    String descriptionKey();

    /** Placeholders for {@link #descriptionKey()}, as key/value pairs. */
    String[] placeholders(Country country);

    default String describe(Messages lang, Country country) {
        return lang.text(descriptionKey(), placeholders(country));
    }

    /** Called on ascension; only resource requirements actually take anything. */
    default void consume(Country country) {
    }
}
