package com.cleo.labs.sclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HarvestingX509TrustManager implements X509TrustManager {

    private Consumer<X509Certificate[]> registry;

    public HarvestingX509TrustManager(Consumer<X509Certificate[]> registry) {
        this.registry = registry;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // do nothing
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        registry.accept(chain);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public static TrustManager[] trustManager(Consumer<X509Certificate[]> registry) {
        return new TrustManager[] { new HarvestingX509TrustManager(registry) };
    }

}
