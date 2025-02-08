package io.crunch.viewer;

import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@ApplicationScoped
public class DynamicContentErrorExtension implements ServletExtension {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DYNAMIC_CONTENT_RESOURCE = "/jakarta.faces.resource/dynamiccontent.properties.xhtml";

    /**
     * Adds an exception handler to the deployment info to log exceptions that occur during dynamic content processing.
     * <p>Exception can occur for example when for example user close the bowser during the dynamic content is being processed.</p>
     */
    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addOuterHandlerChainWrapper(handler ->
            new ExceptionHandler(handler)
                .addExceptionHandler(Throwable.class, exchange -> {
                    if (DYNAMIC_CONTENT_RESOURCE.equals(exchange.getRequestPath())) {
                        logger.warn("Error occurred during the dynamic content processing", exchange.getAttachment(ExceptionHandler.THROWABLE));
                    }
                })
        );
    }
}
