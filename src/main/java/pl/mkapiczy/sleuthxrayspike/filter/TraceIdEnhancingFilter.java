package pl.mkapiczy.sleuthxrayspike.filter;

import brave.Span;
import brave.Tracer;
import com.amazonaws.xray.entities.TraceHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(TRACING_FILTER_ORDER + 2)
class TraceIdEnhancingFilter extends GenericFilterBean {
    private static final String B3_TRACE_ID_HEADER = "X-B3-TraceId";

    private final Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        Span currentSpan = this.tracer.currentSpan();
        if (currentSpan == null) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader(B3_TRACE_ID_HEADER, currentSpan.context().traceIdString());
        if (httpServletResponse.containsHeader(TraceHeader.HEADER_KEY)) {
            String awsTraceId = httpServletResponse.getHeader(TraceHeader.HEADER_KEY);
            MDC.put(TraceHeader.HEADER_KEY, awsTraceId);
        }
        chain.doFilter(request, response);
    }

}