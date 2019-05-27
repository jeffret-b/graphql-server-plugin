package io.jenkins.plugins.io.jenkins.plugins.graphql.filters;

import com.google.inject.Injector;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Borrored heavilty from cors-filter-plugin plugin
 * TODO - decide if we should lave it for cors-filter-plugin
 *  or only enable by descriptor / system property
 */
@Extension
public class CORSFilter implements Filter {
    private static final String PREFLIGHT_REQUEST = "OPTIONS";

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void init() throws ServletException {
        Injector inj = Jenkins.getInstance().getInjector();
        if (inj == null) {
            return;
        }
        PluginServletFilter.addFilter(inj.getInstance(CORSFilter.class));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {

            final HttpServletResponse resp = (HttpServletResponse) response;
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;

                String pathInfo = req.getPathInfo();
                if (pathInfo != null && pathInfo.startsWith("/graphql")) {

                    resp.addHeader("Access-Control-Allow-Credentials", "true");
                    resp.addHeader("Access-Control-Allow-Origin", req.getHeader("origin"));
                    resp.addHeader("Access-Control-Allow-Methods", "GET,POST");
                    resp.addHeader("Access-Control-Allow-Headers", "content-type,x-apollo-tracing");
//                    resp.addHeader("Access-Control-Expose-Headers", getDescriptor().getExposedHeaders());
//                    resp.addHeader("Access-Control-Max-Age", getDescriptor().getMaxAge());
                    /**
                     * If this is a preflight request, set the response to 200 OK.
                     */
                    if (req.getMethod().equals(PREFLIGHT_REQUEST)) {
                        resp.setStatus(200);
                        return;
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}