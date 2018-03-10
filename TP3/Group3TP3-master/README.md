# 8INF135/TP3 - Group3 - RESTodo

### Membres du groupe
* Blackburn, Mathieu
* Haegman, Julien
* Tremblay, Sébastien

## Description

Cet API REST, nommé **RESTodo**, fourni un API relié à la gestion d'utilisateurs et de listes de choses à faire.
L'application est développée en Java SE et requiert Java 1.8 ou plus récent.


## Compilation (et préparation nginx)

#### Copier le fichier de configuration nginx et les certificats SSL à utiliser dans /etc/nginx
`sudo cp $GIT_PROJECT_HOME/group3.* /etc/nginx`

#### S'assurer que le dossier '/srv/group3' existe
```
sudo mkdir /srv
sudo mkdir /srv/group3
```

#### Copier le front end dans '/srv/group3'
`sudo cp $GIT_PROJECT_HOME/client/* /srv/group3`

#### Changer le répertoire courant pour 'src'
`cd $GIT_PROJECT_HOME/src`

**S'assurer que ce répertoire contient les sous-répertoires _src_ ($GIT_PROJECT_HOME/src/src) et _libs_ ($GIT_PROJECT_HOME/src/libs) **
```
$ ls -l
total 20
drwxr-xr-x 2 sebas sudo    35 Nov 22 22:28 libs
drwxr-xr-x 3 sebas sudo    23 Nov 22 22:28 src
```

#### Créer le dossier _out_
`mkdir out`

#### Compiler l'application **RESTodo** server
`javac -cp libs/sqlite-jdbc-3.21.0.jar -sourcepath src src/ca/uqac/inf135/group3/tp3/RESTodo.java -d out`

#### S'assurer que **RESTodo** a été compilée et fonctionne correctement en tentant une exécution en mode de test
`java -cp out:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.tp3.RESTodo test`

#### Vous devriez obtenir ce message:
```
Executing RESTodo test mode

Testing database...
Database OK
Listing existing users and todos:


Testing port number...
No port specified, using default port: 8080

Testing REST API server...
Server started successfully
Server stopped successfully

End of test mode. Exiting.
```

Si vous voyez ce message exact, c'est que l'application fonctionne correctement. Sinon, assurez-vous que le fichier _libs/sqlite-jdbc-3.21.0.jar_ existe et que vous utiliez Java SE 1.8 ou plus:
```
$ ls -l libs/*
-rw-r--r-- 1 sebas sudo 6672489 Nov 16 12:38 libs/sqlite-jdbc-3.21.0.jar
$ java -version
java version "1.8.0_151"
Java(TM) SE Runtime Environment (build 1.8.0_151-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.151-b12, mixed mode)
```

## Syntaxe de l'application

L'aplication utilise la syntaxe suivante:
```
java -cp out:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.tp3.RESTodo [test] [LISTEN_PORT]
```

* **test** est optionnel. Inserez la chaîne "test" comme premier paramètre pour exécuter **RESTodo** en mode de test et valider que la base de données est accessible et qu'il est possible de démarrer le server sur le port désiré. Vous pouvez également spécifier **LISTEN_PORT** pour vérifier le démarrage du serveur sur un port spécifique.

* **LISTEN_PORT** est optionnel. Il spécifie sur quel port écouter pour les connections entrant vers notre REST API. Les requêtes sont redirigées depuis le serveur nginx. Si vous devez spécifier un port personnalisé ici, assurerez vous d'ajuster le fichier de configuration de nginx en conséquence (_group3.valereplantevin.fr_, plus spécifiquement, la ligne 25: `proxy_pass http://localhost:8080/api/;`). Par défaut, **RESTodo** écoutera sur le port **8080**.

## Tester RESTodo

### Étape 1: exécuter l'application RESTodo
```
java -cp out:libs/sqlite-jdbc-3.21.0.jar ca.uqac.inf135.group3.tp3.RESTodo
```

### Étape 2: démarrer le serveur nginx
```
sudo nginx -c /etc/nginx/group3.valereplantevin.fr
```

### Étape 3: utiliser n'importe quel client REST pour tester l'API en utilisant ceci comme base d'URL:
```
https://group3.stremblay.com/api/...
```

### Étape 4: utiliser le front end pour tester l'API
Ouvrir un navigateur et accéder à cette adresse:
```
https://group3.stremblay.com/
```


## Génération du certificat SSL signés Let's Encrypt
Pour générer le certificat SSL signé par Let's Encrypt, nous avons effectué (dans les grandes lignes) ces différentes actions:

1. Configuré le domaine `group3.stremblay.com` pour pointer vers un serveur linux (CentOS) que nous contrôlons.
1. Sur ce serveur, téléchargé `certbot-auto` avec _wget_.
1. Exécuté `certbot-auto` pour insaller et configurer  `certbot`.
1. Exécuté `certbot` en mode `certonly` pour n'obtenir que les certificats, sans configurer de serveur.
1. Nous avons obtenus ces fichiers:
   * cert1.pem
   * chain1.pem
   * fullchain1.pem
   * privkey1.pem
1. Nous avons copié `fullchain1.pem` dans le dossier de configuration de nginx et l'avons nommé `group3.stremblay.com.crt`.
1. Nous avons copié `privkey1.pem` dans le dossier de configurations de nginx et l'avons nommé `group3.stremblay.com.key`.
1. Nous avons configuré nginx pour utiliser ces fichiers _.crt_ et _.key_ comme fichiers `ssl_certificate` et `ssl_certificate_key` respectivement.
1. Nous avons reconfiguré le domaine `group3.stremblay.com` pour pointer vers l'adresse loopback `127.0.0.1`
1. Nous avons pus effectuer nos tests d'API REST et de serveur nginx directement avec l'adresse `https://group3.stremblay.com/...` sur nos machines locales.
1. Nous avons reconfiguré le domaine `group3.stremblay.com` pour pointer vers `group3.valereplantevin.fr` en vue de l'évaluation.
