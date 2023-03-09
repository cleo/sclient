package com.cleo.labs.sclient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

public class HarvestingSSLSocketFactory implements Consumer<X509Certificate[]> {

    private Map<String,X509Certificate> map;
    private String peer;
    private SSLContext sslContext;
    private boolean harvested = false;

    public HarvestingSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[] {new HarvestingX509TrustManager(this)}, null);
    }

    public void connect(String host, int port) throws UnknownHostException, IOException {
        SSLSocket s = (SSLSocket) sslContext.getSocketFactory().createSocket(host, port);
        //s.startHandshake();
        peer = s.getSession().getPeerPrincipal().getName();
    }

    public String getPeer() {
        if (!harvested) {
            throw new IllegalStateException("no certificate chain harvested");
        }
        return peer;
    }

    public X509Certificate getRoot() {
        if (!harvested) {
            throw new IllegalStateException("no certificate chain harvested");
        }
        if (!map.containsKey(peer)) {
            throw new IllegalStateException("peer certificate not found");
        }
        X509Certificate result = map.get(peer);
        String subject = peer;
        String issuer = result.getIssuerX500Principal().getName();
        while (!issuer.equals(subject) && map.containsKey(issuer)) {
            result = map.get(issuer);
            subject = issuer;
            issuer = result.getIssuerX500Principal().getName();
        }
        return result;
    }

    public X509Certificate getServer() {
        if (!harvested) {
            throw new IllegalStateException("no certificate chain harvested");
        }
        if (!map.containsKey(peer)) {
            throw new IllegalStateException("peer certificate not found");
        }
        return map.get(peer);
    }

   public Collection<X509Certificate> getCerts() {
        return map.values();
    }

    @Override
    public void accept(X509Certificate[] chain) {
        this.harvested = true;
        map = new LinkedHashMap<>(chain.length);
        for (X509Certificate cert : chain) {
            map.put(cert.getSubjectX500Principal().getName(), cert);
        }
    }

}
