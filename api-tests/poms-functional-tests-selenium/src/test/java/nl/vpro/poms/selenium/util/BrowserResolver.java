package nl.vpro.poms.selenium.util;

import org.junit.jupiter.api.extension.*;

/**
 * @author Michiel Meeuwissen
 */
public class BrowserResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(WebDriverFactory.Browser.class);

    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return null;

    }
}
