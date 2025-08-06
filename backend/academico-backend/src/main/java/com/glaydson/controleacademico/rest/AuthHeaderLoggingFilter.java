package com.glaydson.controleacademico.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class AuthHeaderLoggingFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(AuthHeaderLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null) {
            LOG.info("Authorization header: " + authHeader);
        } else {
            LOG.info("No Authorization header present in request.");
        }
    }
}

