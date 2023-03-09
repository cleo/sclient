package com.cleo.labs.sclient.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.cleo.labs.sclient.CertUtils;
import com.cleo.labs.sclient.HarvestingSSLSocketFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "sclient", mixinStandardHelpOptions = true, version = "sclient 1.0.0",
description = "Cleo Sclient command line")
public class SclientCLI implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Option(names = {"-h", "--host"},
            paramLabel = "host",
            description = "host to connect to (or use --connect)",
            defaultValue = Option.NULL_VALUE)
    private String host;

    @Option(names = {"-p", "--port"},
            paramLabel = "int",
            description = "port to connect to (or use --connect)",
            defaultValue = Option.NULL_VALUE)
    private Integer port;

    @Option(names = {"-c", "--connect"},
            paramLabel = "host:port",
            description = "whom to connect to (default is localhost:443)",
            defaultValue = Option.NULL_VALUE)
    private String connect;

    @Option(names = {"-d", "--default"},
            description = "use default socket factory")
    private boolean dflt;

    @Option(names = {"--chain"},
            description = "print summary of chain")
    private boolean chain;

    @Option(names = {"--showcerts"},
            description = "show all server certificates in the chain")
    private boolean showcerts;

    @Option(names = {"-o", "--output"},
            paramLabel = "file",
            description = "output file",
            defaultValue = Option.NULL_VALUE)
    private File output;

    private PrintStream getOutput() {
        if (output==null) {
            return System.out;
        } else {
            try {
                return new PrintStream(output);
            } catch (FileNotFoundException e) {
                throw new ParameterException(spec.commandLine(),
                        "Option error: cannot create --output "+output.getName());
            }
        }
    }

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = {"-?", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    private void validate() {
        if ((host!=null || port!=null) && connect!=null) {
            throw new ParameterException(spec.commandLine(),
                    "Option error: do not specify --host or --port with --connect");
        }
        if (connect!=null && !HOST_PORT.matcher(connect).matches()) {
            throw new ParameterException(spec.commandLine(),
                    "Option error: --connect should match host:port");
        }
    }

    private static final Pattern HOST_PORT = Pattern.compile("(?<host>.*)(?::(?<port>\\d+))?");

    private String getHost() {
        if (host!=null) {
            return host;
        }
        if (connect!=null) {
            Matcher m = HOST_PORT.matcher(connect);
            if (m.matches()) {
                return m.group("host");
            }
        }
        return "localhost";
    }

    private int getPort() {
        if (port!=null) {
            return port;
        }
        if (connect!=null) {
            Matcher m = HOST_PORT.matcher(connect);
            if (m.matches() && m.group("port")!=null) {
                return Integer.valueOf(m.group("port"));
            }
        }
        return 443;
    }

    @Override
    public Integer call() throws Exception {
        validate();
        try {
            if (dflt) {
                SSLSocket s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(getHost(), getPort());
                s.startHandshake();
            } else {
                HarvestingSSLSocketFactory harvester = new HarvestingSSLSocketFactory();
                harvester.connect(getHost(), getPort());
                PrintStream out = getOutput();
                if (chain) {
                    out.println("---\nCertificate chain");
                    int i=0;
                    for (X509Certificate cert : harvester.getCerts()) {
                        out.println(String.format("%2d s:%s\n   i:%s\n   x:%s",
                                i,
                                cert.getSubjectX500Principal().getName(),
                                cert.getIssuerX500Principal().getName(),
                                cert.getNotAfter().toString()));
                        if (showcerts) {
                            out.println(CertUtils.export(harvester.getServer()));
                            
                        }
                        i++;
                    }
                } else {
                    out.println(CertUtils.export(harvester.getRoot()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) {
        int status = new CommandLine(new SclientCLI()).execute(args);
        System.exit(status);
    }

}
