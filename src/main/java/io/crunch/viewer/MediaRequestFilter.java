package io.crunch.viewer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.filter.HttpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

/**
 * The {@code MediaRequestFilter} class is a servlet filter that intercepts media requests to ensure they are valid.
 * It checks for the presence of a valid token in the request cookies and forwards the request accordingly.
 * If the token is missing or invalid, the request is redirected to an error page.
 */
@WebFilter(urlPatterns = "/jakarta.faces.resource/dynamiccontent.properties.xhtml")
public class MediaRequestFilter extends HttpFilter {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaRequestCache tokenCache;

    /**
     * Constructs a {@code MediaRequestFilter} with the specified {@code MediaRequestCache}.
     *
     * @param tokenCache the cache that stores and validates tokens associated with media requests
     * @param tokenGenerator the utility that validates the format of the request token
     */
    public MediaRequestFilter(MediaRequestCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    /**
     * Filters incoming requests to ensure they have a valid token in their cookies.
     *
     * <p>If the request URI matches the media URI path, this method checks for the presence of a valid token in the request cookies.
     * If the token is missing or invalid, the request is redirected to an error page. Otherwise, the request is passed on to the next filter in the chain.</p>
     *
     * @param request  the {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response the {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @param session  the {@link HttpSession} associated with the request
     * @param chain    the {@link FilterChain} that allows the request to be passed to the next filter or servlet
     * @throws IOException      if an input or output error occurs during the processing of the request
     * @throws ServletException if the request could not be handled
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpSession session, FilterChain chain) throws IOException, ServletException {
        var token = Arrays.stream(request.getCookies())
                .filter(c -> MediaViewerRequestParameters.TOKEN_COOKIE_NAME.equals(c.getName()))
                .findAny()
                .map(Cookie::getValue)
                .orElse("");
        if(StringUtils.isBlank(token) || !tokenCache.isValidToken(token)) {
            logger.error("Cookie {} is missing, invalid or expired, forwarding request to {}", MediaViewerRequestParameters.TOKEN_COOKIE_NAME, ViewerUrls.GENERAL_ERROR_PAGE);
            response.setStatus(400); // Bad Request
            request.getRequestDispatcher(ViewerUrls.GENERAL_ERROR_PAGE).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
