package com.adiling.aws.projectname.servicename.config.filter;

import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.io.IOException;


/**
 * Simple Filter implementation that looks for a Cognito identity id in the API Gateway request context
 * and stores the value in a request attribute. The filter is registered with aws-serverless-java-container
 * in the onStartup method from the  class.
 */
public class CognitoIdentityFilter implements Filter {
    public static final String COGNITO_IDENTITY_ATTRIBUTE = "com.amazonaws.serverless.cognitoId";

    private static final Logger log = LoggerFactory.getLogger(CognitoIdentityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to do in init
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        Object apiGwContext = servletRequest.getAttribute(RequestReader.API_GATEWAY_CONTEXT_PROPERTY);
        if (apiGwContext == null) {
            log.warn("API Gateway context is null");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (!AwsProxyRequestContext.class.isAssignableFrom(apiGwContext.getClass())) {
            log.warn("API Gateway context object is not of valid type");
            filterChain.doFilter(servletRequest, servletResponse);
        }

        AwsProxyRequestContext ctx = apiGwContext instanceof AwsProxyRequestContext ? (AwsProxyRequestContext)apiGwContext : null;
        if(ctx == null) {
            log.warn("AwsProxyRequestContext can be casted");
            return;
        }
        if (ctx.getIdentity() == null) {
            log.warn("Identity context is null");
            filterChain.doFilter(servletRequest, servletResponse);
        }
        String cognitoIdentityId = ctx.getIdentity().getCognitoIdentityId();
        if (cognitoIdentityId == null || "".equals(cognitoIdentityId.trim())) {
            log.warn("Cognito identity id in request is null");
        }
        servletRequest.setAttribute(COGNITO_IDENTITY_ATTRIBUTE, cognitoIdentityId);
        filterChain.doFilter(servletRequest, servletResponse);
    }


    @Override
    public void destroy() {
        // nothing to do in destroy
    }
}
