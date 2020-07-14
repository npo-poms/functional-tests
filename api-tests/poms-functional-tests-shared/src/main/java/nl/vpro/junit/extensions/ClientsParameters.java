package nl.vpro.junit.extensions;

import org.junit.jupiter.api.extension.*;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class ClientsParameters implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return false;

    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return null;

    }
}
