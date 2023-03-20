# README #

When working with TLS connections, it is typically necessasry to ensure that
the TLS client can validate the authentcitiy of the TLS server based on the
X.509 certificate presented by the server.
In Java applications, if the Java runtime is not properly configured, you
may experience the dreaded `javax.net.ssl.SSLHandshakeException`:

> ```
> javax.net.ssl.SSLHandshakeException: PKIX path building failed:
>     sun.security.provider.certpath.SunCertPathBuilderException:
>     unable to find valid certification path to requested target
>	at sun.security.ssl.Alert.createSSLException(Alert.java:131)
>   ...
> ```
	
In order for the TLS connection to succeed, two fundamental requirements must be met:

1. The server certificate must be _trusted_, either explicitly, or more typically, through a chain of certificate issuers up to a _trusted root_, and
2. The server certificate must be named in a way that matches the intended target, typically by hostname.

The _sclient_ utility helps determine the appropriate _trusted root_ for a TLS server target.
It is inspired by the [openssl s_client](https://www.openssl.org/docs/manmaster/man1/openssl-s_client.html)
command, which can display the certificate chain presented by a TLS server during the
connection handshake.
By importing the appropriate root into the list of trusted roots, requirement #1 can be satisfied.

Download sclient from [https://github.com/cleo/sclient/releases/download/1.0.0/sclient](https://github.com/cleo/sclient/releases/download/1.0.0/sclient), for example:

```
wget https://github.com/cleo/sclient/releases/download/1.0.0/sclient
chmod a+x sclient
```

## `sclient` Usage

The `sclient` tool is packaged as an executable Java `jar`  
`java -jar sclient-1.0.0-cli.jar`  
which is also wrapped with a launcher script that makes it directly executable  
`./sclient-1.0.0-cli.jar`.

Type `./sclient-1.0.0-cli.jar --help` for a summary of command line usage:

```
Usage: sclient [-?dV] [--chain] [--showcerts] [-c=host:port] [-h=host]
               [-o=file] [-p=int]
Cleo Sclient command line
  -?, --help                display this help message
  -c, --connect=host:port   whom to connect to (default is localhost:443)
      --chain               print summary of chain
  -d, --default             use default socket factory
  -h, --host=host           host to connect to (or use --connect)
  -o, --output=file         output file
  -p, --port=int            port to connect to (or use --connect)
      --showcerts           show all server certificates in the chain
  -V, --version             display version info
```

Use the `--connect host:port` option to specify the TLS server.
`:port` is optional and defaults to `:443`, the default https port.
`--host` and `--port` can be used as an alternative to `--connect`.

There are three basic usages:

### Testing a connection

To test the condfiguration of your `java` environment vis-a-vis connection to
a TLS server, use `--default` or `-d`:

```
./sclient-1.0.0-cli.jar --connect host:port -d
```

If the connection succeeds, the command returns silently and the return code is `0`.
If there is a certificate trust problem, the `SSLHandshakeException` stack trace is
displayed and the return code is `1`.

```
javax.net.ssl.SSLHandshakeException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.ssl.Alert.createSSLException(Alert.java:131)
	at sun.security.ssl.TransportContext.fatal(TransportContext.java:324)
	at sun.security.ssl.TransportContext.fatal(TransportContext.java:267)
	at sun.security.ssl.TransportContext.fatal(TransportContext.java:262)
	at sun.security.ssl.CertificateMessage$T12CertificateConsumer.checkServerCerts(CertificateMessage.java:654)
	at sun.security.ssl.CertificateMessage$T12CertificateConsumer.onCertificate(CertificateMessage.java:473)
	at sun.security.ssl.CertificateMessage$T12CertificateConsumer.consume(CertificateMessage.java:369)
	at sun.security.ssl.SSLHandshake.consume(SSLHandshake.java:377)
	at sun.security.ssl.HandshakeContext.dispatch(HandshakeContext.java:444)
	at sun.security.ssl.HandshakeContext.dispatch(HandshakeContext.java:422)
	at sun.security.ssl.TransportContext.dispatch(TransportContext.java:182)
	at sun.security.ssl.SSLTransport.decode(SSLTransport.java:152)
	at sun.security.ssl.SSLSocketImpl.decode(SSLSocketImpl.java:1383)
	at sun.security.ssl.SSLSocketImpl.readHandshakeRecord(SSLSocketImpl.java:1291)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:435)
	at com.cleo.labs.sclient.cli.SclientCLI.call(SclientCLI.java:126)
	at com.cleo.labs.sclient.cli.SclientCLI.call(SclientCLI.java:24)
	at picocli.CommandLine.executeUserObject(CommandLine.java:2041)
	at picocli.CommandLine.access$1500(CommandLine.java:148)
	at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2461)
	at picocli.CommandLine$RunLast.handle(CommandLine.java:2453)
	at picocli.CommandLine$RunLast.handle(CommandLine.java:2415)
	at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2273)
	at picocli.CommandLine$RunLast.execute(CommandLine.java:2417)
	at picocli.CommandLine.execute(CommandLine.java:2170)
	at com.cleo.labs.sclient.cli.SclientCLI.main(SclientCLI.java:155)
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:456)
	at sun.security.validator.PKIXValidator.engineValidate(PKIXValidator.java:323)
	at sun.security.validator.Validator.validate(Validator.java:271)
	at sun.security.ssl.X509TrustManagerImpl.validate(X509TrustManagerImpl.java:315)
	at sun.security.ssl.X509TrustManagerImpl.checkTrusted(X509TrustManagerImpl.java:223)
	at sun.security.ssl.X509TrustManagerImpl.checkServerTrusted(X509TrustManagerImpl.java:129)
	at sun.security.ssl.CertificateMessage$T12CertificateConsumer.checkServerCerts(CertificateMessage.java:638)
	... 21 more
Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.provider.certpath.SunCertPathBuilder.build(SunCertPathBuilder.java:141)
	at sun.security.provider.certpath.SunCertPathBuilder.engineBuild(SunCertPathBuilder.java:126)
	at java.security.cert.CertPathBuilder.build(CertPathBuilder.java:280)
	at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:451)
	... 27 more
```

### Displaying a certifiate chain

To display the certificate chain presented by a TLS server, use `--chain`:

```
./sclient-1.0.0-cli.jar --connect cleointegration.cloud --chain
```

Displays the Subject `s:` and Issuer `i:` names
and expration date `x:` for each certificate presented.
It is typical for the root certificate to be self-signed, so the
Subject `s:` and Issuer `i:` names will be the same.

```
---
Certificate chain
 0 s:CN=cleointegration.cloud,O=Cleo Communications  LLC,ST=Illinois,C=US
   i:CN=Sectigo RSA Organization Validation Secure Server CA,O=Sectigo Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Tue May 23 18:59:59 CDT 2023
 1 s:CN=Sectigo RSA Organization Validation Secure Server CA,O=Sectigo Limited,L=Salford,ST=Greater Manchester,C=GB
   i:CN=USERTrust RSA Certification Authority,O=The USERTRUST Network,L=Jersey City,ST=New Jersey,C=US
   x:Tue Dec 31 17:59:59 CST 2030
 2 s:CN=USERTrust RSA Certification Authority,O=The USERTRUST Network,L=Jersey City,ST=New Jersey,C=US
   i:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Sun Dec 31 17:59:59 CST 2028
 3 s:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   i:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Sun Dec 31 17:59:59 CST 2028
```

If you include `--showcerts`, the certificates themselves will be included in the output.

```
./sclient-1.0.0-cli.jar --connect cleointegration.cloud --chain --showcerts
```

Displays:

```
---
Certificate chain
 0 s:CN=cleointegration.cloud,O=Cleo Communications  LLC,ST=Illinois,C=US
   i:CN=Sectigo RSA Organization Validation Secure Server CA,O=Sectigo Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Tue May 23 18:59:59 CDT 2023
-----BEGIN CERTIFICATE-----
MIIHoTCCBomgAwIBAgIRAPhGD4CNB6AAz3DOB79HxogwDQYJKoZIhvcNAQELBQAw
etc.etc.etc.
-----END CERTIFICATE-----
 1 s:CN=Sectigo RSA Organization Validation Secure Server CA,O=Sectigo Limited,L=Salford,ST=Greater Manchester,C=GB
   i:CN=USERTrust RSA Certification Authority,O=The USERTRUST Network,L=Jersey City,ST=New Jersey,C=US
   x:Tue Dec 31 17:59:59 CST 2030
-----BEGIN CERTIFICATE-----
MIIHoTCCBomgAwIBAgIRAPhGD4CNB6AAz3DOB79HxogwDQYJKoZIhvcNAQELBQAw
etc.etc.etc.
-----END CERTIFICATE-----
 2 s:CN=USERTrust RSA Certification Authority,O=The USERTRUST Network,L=Jersey City,ST=New Jersey,C=US
   i:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Sun Dec 31 17:59:59 CST 2028
-----BEGIN CERTIFICATE-----
MIIHoTCCBomgAwIBAgIRAPhGD4CNB6AAz3DOB79HxogwDQYJKoZIhvcNAQELBQAw
etc.etc.etc.
-----END CERTIFICATE-----
 3 s:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   i:CN=AAA Certificate Services,O=Comodo CA Limited,L=Salford,ST=Greater Manchester,C=GB
   x:Sun Dec 31 17:59:59 CST 2028
-----BEGIN CERTIFICATE-----
MIIHoTCCBomgAwIBAgIRAPhGD4CNB6AAz3DOB79HxogwDQYJKoZIhvcNAQELBQAw
etc.etc.etc.
-----END CERTIFICATE-----
```

> ### IMPORTANT NOTE
> Certificates are typically issued by a chain of certificate authorities,
> each higher level CA signing the certificate of the issuer below it
> (and including properties in the certificate that properly delegate
> the authority to issue certificates to it).
> When trusting a certificate to satisfy the validation rules, you
> may trust _any certificate in the chain_, from the specific TLS server
> certificate all the way up to the root.
> 
> Any will work, but it is strongly advised to **trust the _root_ of the
> chain**, not lower level issuers and **certainly not the TLS server certificate
> itself**&mdash;this typically ensures that you will not need to maintain the
> trust for the longest time. TLS server certificates are rotated usually on
> an annual basis, but sometimes even quarterly or monthly.

### Exporting the root certificate

In order to allow you to easily update your environment to include the correct
root certificate in the list of trusted roots, the `sclient` tool provides
an option to output the root certificate as a standard encoded file:

```
./slicent --connect host:port --output myroot.cer
```

The output certificate file can then be imported into the trusted roots,
for example, using `keytool` for your Java environment:

```
keytool -import -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit \
  -alias myroot -file myroot.cer
```

(It isn't a great security practice to include `-storepass` on the command line, and
if you leave it off `keytool` will prompt you for it. But it isn't a great practice to
leave the keystore password as its default `changeit` either.)

After importing the root into `cacerts`, a subsequent test using `-d` should succeed:

```
if ./sclient-1.0.0-cli.jar --connect host:port -d 2> /dev/null; then echo "worked"; else echo "failed"; fi
```

You can remove the certificate from the trusted roots using the alias you assigned
with `-import`:

```
keytool -delete -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit \
  -alias myroot
```

## Reference

The rules for validating TLS server certificates are described in IETF RFC standards.

The process for validating the relationship between the Operator’s certificate and the Certificate Authority’s root certificate is called “Certificate Path Validation” and is defined by IETF in [RFC 5280 "PKIX" Section 6 "Certification Path Validation"](https://www.rfc-editor.org/rfc/rfc5280#section-6).

The rules for encoding a DNS hostname in a TLS certificate and for verifying certificates correctly are described in [RFC 6125 “Representation and Verification of Domain-Based Application Service Identity within Internet Public Key Infrastructure Using X.509 (PKIX) Certificates in the Context of Transport Layer Security (TLS)” Section 6 “Verifying Service Identity”](https://www.rfc-editor.org/rfc/rfc6125#section-6).
