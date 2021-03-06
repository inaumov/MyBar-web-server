package mybar;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public final class CommonPaths implements InitializingBean {
    public static final String API_PATH = "http://localhost:8089/api/bar/v1/";

    @Autowired
    private Environment env;

    @Value("${http.protocol}")
    private String protocol;

    @Value("${http.host}")
    private String host;

    @Value("${http.port}")
    private int port;

    public CommonPaths() {
        super();
    }

    // API

    public final String getServerRoot() {
        if (port == 80) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port;
    }

    public final String getProtocol() {
        return protocol;
    }

    public final String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    //

    @Override
    public void afterPropertiesSet() {
        if (protocol == null || protocol.equals("${http.protocol}")) {
            protocol = Preconditions.checkNotNull(env.getProperty("http.protocol"));
        }
        if (host == null || host.equals("${http.host}")) {
            host = Preconditions.checkNotNull(env.getProperty("http.host"));
        }
        port = Preconditions.checkNotNull(env.getProperty("http.port", Integer.class));
    }

}
