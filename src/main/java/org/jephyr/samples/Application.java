package org.jephyr.samples;

import co.paralleluniverse.xst.ExtendedStackTrace;
import co.paralleluniverse.xst.ExtendedStackTraceElement;
import org.jephyr.continuation.easyflow.ContinuationImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

@SpringBootApplication
public class Application {

    @Bean
    TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setProtocol("org.apache.coyote.http11.Http11Protocol");
        return factory;
    }

    public static void main(String[] args) throws Exception {
        ContinuationImpl.setUnsuspendableErrorListener(unsuspendableError -> {
            StringBuilder sb = new StringBuilder();
            ExtendedStackTrace stackTrace = ExtendedStackTrace.here();
            for (ExtendedStackTraceElement stackTraceElement : stackTrace) {
                Member member = stackTraceElement.getMethod();
                String descriptor;
                if (member instanceof Constructor) {
                    descriptor = getDescriptor((Constructor) member);
                } else {
                    descriptor = getDescriptor((Method) member);
                }
                sb.append('\\').append('\n').append('|').append('^')
                        .append(stackTraceElement.getClassName().replace('.', '/').replace("$", "\\\\$"))
                        .append("\\\\.").append(stackTraceElement.getMethodName().replace("$", "\\\\$"))
                        .append(descriptor.replace("(", "\\\\(").replace(")", "\\\\)").replace("$", "\\\\$")
                                .replace("[", "\\\\[")).append('$');
            }
            System.err.println(sb);
        });
        SpringApplication.run(Application.class, args);
    }

    private static String getDescriptor(Constructor<?> constructor) {
        StringBuffer sb = new StringBuffer().append('(');
        for (Class<?> parameter : constructor.getParameterTypes()) {
            appendDescriptor(sb, parameter);
        }
        return sb.append(')').append('V').toString();
    }

    private static String getDescriptor(Method method) {
        StringBuffer sb = new StringBuffer().append('(');
        for (Class<?> parameter : method.getParameterTypes()) {
            appendDescriptor(sb, parameter);
        }
        sb.append(')');
        appendDescriptor(sb, method.getReturnType());
        return sb.toString();
    }

    private static void appendDescriptor(StringBuffer sb, Class<?> type) {
        while (true) {
            if (type.isPrimitive()) {
                char c;
                if (type == Void.TYPE) {
                    c = 'V';
                } else if (type == Boolean.TYPE) {
                    c = 'Z';
                } else if (type == Character.TYPE) {
                    c = 'C';
                } else if (type == Byte.TYPE) {
                    c = 'B';
                } else if (type == Short.TYPE) {
                    c = 'S';
                } else if (type == Integer.TYPE) {
                    c = 'I';
                } else if (type == Float.TYPE) {
                    c = 'F';
                } else if (type == Long.TYPE) {
                    c = 'J';
                } else {
                    c = 'D';
                }
                sb.append(c);
                return;
            } else if (type.isArray()) {
                sb.append('[');
                type = type.getComponentType();
            } else {
                sb.append('L');
                String name = type.getName();
                for (int i = 0, n = name.length(); i < n; i++) {
                    char c = name.charAt(i);
                    sb.append(c == '.' ? '/' : c);
                }
                sb.append(';');
                return;
            }
        }
    }
}
