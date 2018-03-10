# 8INF135/Projet - Group3 - RESTodo

### Membres du groupe
* Blackburn, Mathieu
* Haegman, Julien
* Tremblay, Sébastien

## Description

Ce projet est divisé en 3 applications. La première est une application web de gestion d'authentification implémentant le protocole OAuth2.
Cette application se nomme **GOASP** (pour Group3 OAuth2 Security Provider). La seconde est une modification de l'API REST (nommée **RESTodo**), qui avait été développée pour le TP3. Elle a été modifiée pour déléguer la gestion des utilisateurs à **GOASP**.
Ceux deux premières applications ont été développées en utilisant le même framework _"maison"_ développé au départ pour le TP3 et amélioré pour ce projet.
La dernière application, une application web Angular JS de type "Single Page Application", nommée simplement **RESTodo FrontEnd** est une amélioration de l'application développée au départ pour le TP3.

**GOASP** et **RESTodo** ont été développés en Java SE et requiert Java 1.8 ou plus récent. De plus, un serveur nginx est nécessaire à l'exécution de ce projet puisque celui-ci assure la communication chiffrée en TLS via le protocole HTTPS, le relais des demandes de connexions pour **GOASP** et **RESTodo** aux bonnes applications selon l'URL demandée, ainsi que le service des fichiers statiques du **RESTodo FrontEnd**.

## Compilation (et préparation nginx)

#### Copier le fichier de configuration nginx et les certificats SSL à utiliser dans /etc/nginx
**ATTENTION:** Le fichier de configuration se nomme _nginx.conf_ et écrasera celui déjà présent dans le dossier de configuration.
`sudo cp $GIT_PROJECT_HOME/nginx/* /etc/nginx`

#### Copier l'application RESTodo FrontEnd dans le dossier de fichiers statiques (tel que configuré dans le fichier _nginx.conf_
```
mkdir -p /srv/group3
rm /srv/group3/*
cp $GIT_PROJECT_HOME/client/* /srv/group3
```

#### Changer le répertoire courant pour le dossier racine du projet
`cd $GIT_PROJECT_HOME`

**S'assurer que ce répertoire contient au moins les sous-répertoires _src_ ($GIT_PROJECT_HOME/src), _libs_ ($GIT_PROJECT_HOME/libs) et _res_ ($GIT_PROJECT_HOME/res)**
```
$ ls -l
total 0
drwxr-xr-x 2 sebas sudo 105 Dec 13 16:14 libs
drwxr-xr-x 3 sebas sudo  17 Dec 13 16:14 res
drwxr-xr-x 3 sebas sudo  15 Dec 13 16:14 src
```

#### Créer le dossier _out_
`mkdir out`

#### Compiler l'application **RESTodoApp**
`javac -cp libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar -sourcepath src src/ca/uqac/inf135/group3/project/RESTodoApp.java -d out`

#### Compiler l'application **GoaspApp**
`javac -cp libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar -sourcepath src src/ca/uqac/inf135/group3/project/GoaspApp.java -d out`

#### Vous assurer que **RESTodoApp** a été compilé et fonctionne correctement en tentant une exécution en mode de test
`java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.RESTodoApp test`

#### Vous devriez obtenir ce message:
```
Executing RESTodo in test mode

Testing RESTodo database...
Creating or loading RESTodo database...
RESTodo database loaded: restodo.sqlite

Database OK


Testing port number...
No port specified, using default port: 8080

Testing RESTodo route server...
Preparing RESTodo route server...

Server prepared

Server started on port 8080.
Server started successfully
Server stopped successfully

Performing additional RESTodo specific tests...


End of test mode. Exiting.
```

Si vous voyez ce message exact, c'est que l'application fonctionne correctement. Sinon, assurez-vous que les fichiers _ormlite-core-5.0.jar_, _ormlite-jdbc-5.0.jar_ et _sqlite-jdbc-3.21.0.jar_ existent bien dans le sous-dossier _libs_ et que vous utiliez Java SE 1.8 ou plus:
```
$ ls -l libs/*
-rw-r--r-- 1 sebas sudo  325309 Dec  6 11:00 libs/ormlite-core-5.0.jar
-rw-r--r-- 1 sebas sudo   75088 Dec  7 15:36 libs/ormlite-jdbc-5.0.jar
-rw-r--r-- 1 sebas sudo 6672489 Dec  7 14:30 libs/sqlite-jdbc-3.21.0.jar
$ java -version
java version "1.8.0_151"
Java(TM) SE Runtime Environment (build 1.8.0_151-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.151-b12, mixed mode)
```

#### Vous assurer que **GoaspApp** a été compilé et fonctionne correctement en tentant une exécution en mode de test
`java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.GoaspApp test`

#### Vous devriez obtenir ce message:
```
Executing GOASP in test mode

Testing GOASP database...
Creating or loading GOASP database...
GOASP database loaded: goasp.sqlite

Registering RESTodo FrontEnd app if not already registered....
App not registered, registering it with hardcoded id: ytuCRsoQmqTVsGzsaXo818p0TPq1lElI_-3iPgZcR2pXnJtaBds_wtF1wu53MswZ
Let's assume the app was registered with GOASP before ID was put in client app.
App registered successfully

Database OK


Testing port number...
No port specified, using default port: 8081

Testing GOASP route server...
Preparing GOASP route server...

Server prepared

Server started on port 8081.
Server started successfully
Server stopped successfully

Performing additional GOASP specific tests...

Testing STSP templates
Template 'errors/generic_error_page.html' is OK.
Template 'goasp/base.html' is OK.
Template 'goasp/login_form.html' is OK.
Template 'goasp/register_form.html' is OK.
Template 'goasp/request_form.html' is OK.


End of test mode. Exiting.
```

## Syntaxe de l'application RESTodoApp

L'aplication utilise la syntaxe suivante:
```
java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.RESTodoApp [test] [LISTEN_PORT]
```

* **test** est optionnel. Inserez la chaîne "test" comme premier paramètre pour exécuter **RESTodo** en mode de test et valider que la base de données est accessible et qu'il est possible de démarrer le server sur le port désiré. Vous pouvez également spécifier **LISTEN_PORT** pour vérifier le démarrage du serveur sur un port spécifique.

* **LISTEN_PORT** est optionnel. Il spécifie sur quel port écouter pour les connections entrant vers notre REST API. Les requêtes sont redirigées depuis le serveur nginx. Si vous devez spécifier un port personnalisé ici, assurerez vous d'ajuster le fichier de configuration de nginx en conséquence (_$GIT_PROJECT_HOME/nginx/nginx.conf_, plus spécifiquement, la ligne 25: `proxy_pass http://localhost:8080/api;`). Par défaut, **RESTodo** écoutera sur le port **8080**.

## Syntaxe de l'application GoaspApp

L'aplication utilise la syntaxe suivante:
```
java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.GoaspApp [test] [LISTEN_PORT]
```

* **test** est optionnel. Inserez la chaîne "test" comme premier paramètre pour exécuter **GOASP** en mode de test et valider que la base de données est accessible et qu'il est possible de démarrer le server sur le port désiré. Vous pouvez également spécifier **LISTEN_PORT** pour vérifier le démarrage du serveur sur un port spécifique.

* **LISTEN_PORT** est optionnel. Il spécifie sur quel port écouter pour les connections entrant vers le serveur d'authentification. Les requêtes sont redirigées depuis le serveur nginx. Si vous devez spécifier un port personnalisé ici, assurerez vous d'ajuster le fichier de configuration de nginx en conséquence (_$GIT_PROJECT_HOME/nginx/nginx.conf_, plus spécifiquement, la ligne 29: `proxy_pass http://localhost:8081/goasp/;`). Par défaut, **GOASP** écoutera sur le port **8081**.

## Tester le projet

**NOTE IMPORTANTE:** L'application assume qu'elle sera exécutée sur le serveur _valereplantevin.fr_ [192.99.4.152]. En effet, group3.stremblay.com pointe vers cette machine et **RESTodo FrontEnd** utilise ce domaine pour contacter autant **GOASP** que l'API **RESTodo**. De même, le serveur web **RESTodo** effectue la validation du token en contactant **GOASP** par le protocole HTTPS sur ce même domaine.

### Étape 1: exécuter l'application GOASP
```
java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.GoaspApp
```

### Étape 2: exécuter l'application RESTodo
```
java -cp out:libs/ormlite-core-5.0.jar:libs/ormlite-jdbc-5.0.jar:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.project.RESTodoApp
```

### Étape 3: démarrer le serveur nginx
```
sudo nginx
```

### Étape 4: ouvrir un navigateur et accéder à l'addresse suivante:
```
https://group3.stremblay.com/
```

