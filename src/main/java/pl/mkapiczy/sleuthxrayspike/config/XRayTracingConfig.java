package pl.mkapiczy.sleuthxrayspike.config;

import brave.propagation.Propagation;
import brave.propagation.aws.AWSPropagation;
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import zipkin2.Span;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.xray_udp.XRayUDPReporter;

import static org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER;

@Configuration
@Profile("!local")
public class XRayTracingConfig {

    @Bean
    public Propagation.Factory propagationFactory() {
        return AWSPropagation.FACTORY;
    }

    @Bean
    public Reporter<Span> spanReporter() {
        return XRayUDPReporter.create();
    }

    @Bean
    public FilterRegistrationBean awsXRayFilter(@Value("${spring.application.name}") String tracingName) {
        /*
        AWS X-Ray uses AWS_XRAY_TRACING_NAME env variable to identify the service.
        The tracingName value set here is only used as fallback.
        */
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        AWSXRayServletFilter awsxRayServletFilter = new AWSXRayServletFilter(tracingName);
        registrationBean.setFilter(awsxRayServletFilter);
        registrationBean.setOrder(TRACING_FILTER_ORDER + 1);
        return registrationBean;
    }
}