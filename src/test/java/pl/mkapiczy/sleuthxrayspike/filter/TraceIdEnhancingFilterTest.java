package pl.mkapiczy.sleuthxrayspike.filter;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.amazonaws.xray.entities.TraceHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceIdEnhancingFilterTest {
    private static final String B3_TRACE_ID_HEADER = "X-B3-TraceId";
    private static final String AWS_TRACE_ID = "AWS_TRACE_ID";
    private final long TRACE_ID_LONG = 123l;

    @Mock
    private Tracer tracer;

    @Test
    public void shouldAddXB3TraceIdHeaderToResponse() throws IOException, ServletException {
        TraceContext context = TraceContext.newBuilder()
                .spanId(1l)
                .traceId(TRACE_ID_LONG)
                .build();

        Span span = mock(Span.class);
        when(span.context()).thenReturn(context);
        when(tracer.currentSpan()).thenReturn(span);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        doNothing().when(servletResponse).setHeader(anyString(), anyString());
        FilterChain filterChain = mock(FilterChain.class);

        new TraceIdEnhancingFilter(tracer).doFilter(servletRequest, servletResponse, filterChain);

        ArgumentCaptor<String> headerKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(servletResponse).setHeader(headerKeyCaptor.capture(), headerValueCaptor.capture());
        assertEquals(B3_TRACE_ID_HEADER, headerKeyCaptor.getValue());
        assertEquals(context.traceIdString(), headerValueCaptor.getValue());
    }

    @Test
    public void shouldAddAwsTraceIdToMDC() throws IOException, ServletException {
        TraceContext context = TraceContext.newBuilder()
                .spanId(1l)
                .traceId(TRACE_ID_LONG)
                .build();

        Span span = mock(Span.class);
        when(span.context()).thenReturn(context);
        when(tracer.currentSpan()).thenReturn(span);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletResponse.containsHeader(TraceHeader.HEADER_KEY)).thenReturn(true);
        when(servletResponse.getHeader(TraceHeader.HEADER_KEY)).thenReturn(AWS_TRACE_ID);
        doNothing().when(servletResponse).setHeader(anyString(), anyString());
        FilterChain filterChain = mock(FilterChain.class);

        new TraceIdEnhancingFilter(tracer).doFilter(servletRequest, servletResponse, filterChain);

        assertThat(MDC.get(TraceHeader.HEADER_KEY)).isEqualTo(AWS_TRACE_ID);
    }
}