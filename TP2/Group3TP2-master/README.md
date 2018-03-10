# 8INF135/TP2 - Group3 - SecuRelay

### Group members
* Blackburn, Mathieu
* Haegman, Julien
* Tremblay, Sébastien

## Description

This server, named **SecuRelay** provides functionnalities of both South-Pole's server (listen for clear messages on a port and relay encrypted data to Ottawa) and Ottawa's server (listen for encrypted messages and diffuse it in clear on a multicast group). Therefore the server can be run twice with different set of parameters to exchange messages securely in both directions (from SP to Ottawa and from Ottawa to SP).

More over, as a convenient testing feature, the server also joins its own multicast group to intercept and display clear messages it has just decrypted from incoming encrypted messages.

Since each side running the server must know 3 different RSA keys (their own private key, public key, and the OTHER public key), these 3 keys (being the same public keys but a different private key) have been joined into 2 distinct RSA keyring files: _sp.keyring_ and _ottawa.keyring_. Each running server uses only one keyring, and therefore only knows both public keys and their own private key.

## Important note

Before version 1.9, Java had a restricted cryptography strengh policy by default, an unlimited strengh cyptography extension must be installed in order to use AES-256 keys. This extension is named the **Unlimited Strength Java(TM) Cryptography
Extension (JCE) Policy Files for the Java(TM) Platform** (a.k.a **JCE**). It is therefore important to follow the **JCE installation** section below in order to be able to run the Relay if you are running an older version of Java than 1.9.

Please note that once a server is started, there is no built-in closing mechanism. The server must either be closed using _Ctrl-C_ or by killing its associated process or session.

## JCE installation

You must first check out your java's version by executing the following command:
```
java -version
```
This should produce an output similar to the following one:
```
java version "1.8.0_121"
Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
Java HotSpot(TM) Client VM (build 25.121-b13, mixed mode)
```

The first line, `java version "1.X...."` is telling us the Java version that is used, where X should be either 6, 7, 8 or 9.

If X is 9, there is nothing more to do for this section.

Otherwise, for version 6, 7 or 8, it is necessary to install JCE. Older versions are not supported.

To install JCE_X_, follow these steps:

1. Identify the JRE's home directory (refered later as JRE_HOME) that will be used to execute the project by executing the following command: `which java`.

1. Replace _local_policy.jar_ and _US_export_policy.jar_ files from your `JRE_HOME/lib/security` folder by
the ones provided in the `$GIT_PROJECT_HOME/jce/jceX` sub-folder, where _X_ is the Java version number.
   * Alternatively, you can download jce files from one of the following links:
      * Java 6: http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html
      * Java 7: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
      * Java 8: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

## Compilation

#### Change current directory to Server sub-directory
`cd $GIT_PROJECT_HOME/Server`

**Make sur this directory contains an _src_ sub-directory and files _ottawa.keyring_ and _sp.keyring_**

#### Create the _output_ directory
`mkdir out`

#### Compile **SecuRelay** server
`javac -sourcepath src src/ca/uqac/inf135/group3/tp2/SecuRelay.java -d out`

#### Make sure **SecuRelay** is working properly
`java -cp out ca.uqac.inf135.group3.tp2.SecuRelay`

#### If JCE was not installed properly an error message will be displayed:
```
Unlimited Strength Java Cryptography Extension (JCE) is not installed.
See: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.htm
```

If you see this message, make sure you followed the **JCE installation** section correctly

#### Otherwise, you should see the _Missing parameters._ error message followed by the SecuRelay's syntax:
```
Missing parameters.
Syntax: java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET [LISTEN_PORT [KEYRING_PREFIX [REMOTE[:PORT] [MULTICAST:[PORT]]]]]
 INTRA:PORT       : INTRA is the interface accessing the intranet
                    PORT is the port listening for clear messages
 NET              : The interface accessing the internet
 LISTEN_PORT      : (Optional) Port number on which to listen for encrypted incoming connexions (Default: 8080)
 KEYRING_PREFIX   : (Optional) file name (without '.keyring' extension) to load as RSA keyring. Use either sp or ottawa. (Default: sp)
 REMOTE[:PORT]    : (Optional) REMOTE hostname where encrypted message should be sent (Default: rpiexplorer.io)
                    (Optional) PORT number where encrypted message should be sent (Default: 8080)
 MULTICAST[:PORT] : (Optional) MULTICAST group address where decrypted message should be sent (Default: 239.255.1.1)
                    (Optional) PORT number for multicast group where decrypted message should be sent (Default: 6666)
```

#### If you see that message, you are all good.

## Application syntax

The aplication has the following syntax:
```
Syntax: java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET [LISTEN_PORT [KEYRING_PREFIX [REMOTE[:PORT] [MULTICAST:[PORT]]]]]
 INTRA:PORT       : INTRA is the interface accessing the intranet
                    PORT is the port listening for clear messages
 NET              : The interface accessing the internet
 LISTEN_PORT      : (Optional) Port number on which to listen for encrypted incoming connexions (Default: 8080)
 KEYRING_PREFIX   : (Optional) file name (without '.keyring' extension) to load as RSA keyring. Use either sp or ottawa. (Default: sp)
 REMOTE[:PORT]    : (Optional) REMOTE hostname where encrypted message should be sent (Default: rpiexplorer.io)
                    (Optional) PORT number where encrypted message should be sent (Default: 8080)
 MULTICAST[:PORT] : (Optional) MULTICAST group address where decrypted message should be sent (Default: 239.255.1.1)
                    (Optional) PORT number for multicast group where decrypted message should be sent (Default: 6666)
```

* The **INTRA:PORT** parameter is mandatory. That's the interface and port combination that will be used to listen for incoming clear messages from the intranet. (The interface is the one connected to the intranet network.)

* The **NET** parameter is mandatory. That's the interface used to communicate with the "outside" of the local network (the one connected to the Internet.)

* **LISTEN_PORT** is optional. It specifies on which port number to listen for incoming encrypted messages. This parameter is only the port number: the interface that will be used to open the listening socket is **NET** parameter. (_i.e._ the server will open the **NET:LISTEN_PORT** to listen for encrypted messages incoming from the remote server). If this parameter is ommited, the server will listen on **NET**:8080 which might already be opened by another application. If this is the case, an _Unable to open local server socket on interface '**NET**' and port **LISTEN_PORT**._ error will be displayed, and the server won't be able to receive incoming encrypted connecion. However, it will still be able to listen for clear messages on **INTRA:PORT** and relay encrypted messages.

* **KEYRING_PREFIX** is optional. It's the RSA keyring file name's prefix to use for encryption operations. There are only 2 provided keyrings: _sp.keyring_ and _ottawa.keyring_. Therefore, the only possible values here are **sp** and **ottawa** depending if we want to run the relay on the South-Pole's (sp) or the Ottawa's (ottawa) server. 

* **REMOTE[:PORT]** is optional. It specifies the target destination of encrypted messages. By default, messages will be sent to _rpiexplorer.io:8080_. If **REMOTE** is present and **:PORT** is ommited, encrypted messages will be sent to **REMOTE:8080**.

* **MULTICAST[:PORT]** is optional. It specifies the multicast group on which to send decrypted messages received on **NET:LISTEN_PORT** port. The interface **INTRA** from the first **INTRA:PORT** parameter will be use to send the multicast diffusion. By default, decrypted messages will be diffused to _239.255.1.1:6666_. If **MULTICAST** is present and **:PORT** is ommited, messages will be diffused to **MULTICAST:6666**.

**NOTE :** If an optional parameter is supplied, all previous parameters must also be supplied. (_i.e._ in order to specify the **REMOTE** parameter, **LISTEN_PORT** and **KEYRING_PREFIX** must be supplied too).

## Testing SecuRelay

### Testing the South-Pole's relay

To test **SecuRelay** on the South-Pole side of the connection, it can simply be executed by running the following command:
```
java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET
```

**WARNING :** If the server is executed on the same machine as the test application that will recieve encrypted message (i.e. _rpiexplorer.io:8080_), there might be a port conflict since the server will try to open a listener on the **NET:8080** port. If the port is already in use, the server will display an error, and the encrypted message receiving option will simply be disabled while the clear message functionnality will continue to work properly.

However, as explained in the **Application syntax** section, a different listening port for the encrypted messages can be specified by adding a third parameter **LISTEN_PORT**. For example, to listen on port **8888** instead of the default port **8080**, use the following syntax:
```
java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET 8888
```

### Testing the Ottawa's relay

To test **SecuRelay** on the Ottawa's side of the connection, it must be executed on the server corresponding to the **REMOTE** parameter used to execute the South-Pole's server. It is mandatory to add, at least, the **LISTEN_PORT** and **KEYRING_PREFIX** parameters. **LISTEN_PORT** _should_ be 8080 (actually, it must match the **REMOTE**'S **PORT** parameter used to execute the South-Pole's server). The **KEYRING_PREFIX** parameter **MUST** be _ottawa_ in order to use Ottawa's RSA keyring. For example, if the South-Pole server was executed with the default **REMOTE[:PORT]** parameter value, the server should be executed with the following syntax:
```
java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET 8080
```

### Testing both servers on the same machine with a single network interface

To test the communication between both servers, you will need 3 distinct consoles.

#### Starting South-Pole server
In _Console#1_, start South-Pole server using the following command:
```
java -cp out ca.uqac.inf135.group3.tp2.SecuRelay localhost:1234 localhost 5432 sp localhost:4321
```

#### Starting Ottawa server
In _Console#2_, start Ottawa server using the following command:
```
java -cp out ca.uqac.inf135.group3.tp2.SecuRelay localhost:2345 localhost 4321 ottawa localhost:5432
```

#### Sending a message from South-Pole to Ottawa 
In _Console#3_, send a clear message (e.g. "This is a test clear message.") to **localhost:1234** using your favorite tool. You can also use the provided message sender included in the server using the following command:
```
java -cp out ca.uqac.inf135.group3.tp2.SendMessage localhost 1234 "This is a test clear message."
```

#### Validating that the message was successfully sent and received

_Console#1_ (South-Pole) should now show the following lines:
```
Received an incoming local clear message: 29 bytes
Sent 1053 encrypted bytes to remote host localhost:4321.
```

While _Console#2_ (Ottawa) should show the following lines:
```
Received an incoming remote encrypted message: 1053 bytes
Successfully decrypted a 29 bytes message, sending it to multicast group...
```

Since we didn't specified a custom multicast group for either server, they both listen to the same default multicast group (_239.255.1.1:6666_). Therefore, both _Console#1_ and _Console#2_ should also show the following line:
```
Message received on multicast group: This is a test clear message.
```

#### Sending a message from Ottawa to South-Pole (communication in the other way)
In _Console#3_, send a clear message (e.g. "Ottawa is contacting South-Pole.") to **localhost:2345** using your favorite tool. You can also use the provided message sender included in the server using the following command:
```
java -cp out ca.uqac.inf135.group3.tp2.SendMessage localhost 2345 "Ottawa is contacting South-Pole."
```

#### Validating that the message was successfully sent and received

_Console#2_ (Ottawa) should now show the following lines:
```
Received an incoming local clear message: 32 bytes
Sent 1056 encrypted bytes to remote host localhost:5432.
```

While _Console#1_ (South-Pole) should show the following lines:
```
Received an incoming remote encrypted message: 1056 bytes
Successfully decrypted a 32 bytes message, sending it to multicast group...
```

Since we didn't specified a custom multicast group for either server, they both listen to the same default multicast group (_239.255.1.1:6666_). Therefore, both _Console#1_ and _Console#2_ should also show the following line:
```
Message received on multicast group: Ottawa is contacting South-Pole.
```
