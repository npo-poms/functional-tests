package nl.vpro.junit.extensions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nl.vpro.api.client.utils.Config;

/**
 * @author Michiel Meeuwissen
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface For {
    Config.Prefix value();
}
