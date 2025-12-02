# DAI WORK 2 - client/serveur TCP : Puissance 4
![Static Badge](https://img.shields.io/badge/HEIG--VD-labo-red?logo=intellijidea)
![Maven](https://img.shields.io/badge/build-Maven-blue?logo=apachemaven)
![Java](https://img.shields.io/badge/java-21-orange?logo=openjdk)

## Introduction

## Table des matières
- [Introduction](#introduction)
- [Table des matières](#table-des-matières)
- [Clone et Build](#clone-et-build)
- [Protocole applicatif](#Protocole-applicatif)
- [Utilisation](#utilisation)
    - [Contexte](#contexte)
    - [Serveur](#serveur)
    - [Client](#client)
- [Docker](#docker)
    - [Publication sur GitHub Container Registry](#publication-sur-github-container-registry)
    - [Utiliser l'image depuis GitHub Container Registry](#utiliser-limage-depuis-github-container-registry)
- [Auteurs](#auteurs)

## Clone et Build
Les étapes suivantes vous permettent de cloner et builder le projet afin de pouvoir commencer à l'utiliser. Nous utilisons Maven comme gestionnaire de projet.

Cloner le repo
```
git clone git@github.com:QuentinMichon/dai-work-2.git
```

Entrer dans le dossier racine
```
cd work-1-BMP-cli/
```

#### Pour Linux / MacOS
Télécharger les dépendances
```sh
./mvnw dependency:go-offline
```
Générer une archive JAR
```sh
./mvnw clean package
```

#### Pour Windows
Télécharger les dépendances
```sh
./mvnw.cmd dependency:go-offline
```
Générer une archive JAR
```sh
./mvnw.cmd clean package
```

> [!NOTE]
>
>Si vous utilisez l'IDE Intellij IDEA, vous pouvez exécuter la configuration **Package application as JAR file** afin d'automatiser la création de l'archive. Ainsi que d'autres configurations permettant de lancer une instance de serveur ou client en local.

## Protocole applicatif
Nous avons défini un protocole applicatif client-serveur baptisé `Puissance4`.
La description du protocole applicatif est disponible [ici](protocol/README.md)

## Utilisation

### Contexte
L’objectif est de lancer un serveur en local afin d’y connecter deux clients. Ceux-ci peuvent ainsi jouer une partie de Puissance 4.
La partie est arbitrée par le serveur, tandis que les clients ne servent que d’interfaces utilisateur.
Actuellement, le serveur ne peut gérer qu’une seule partie à la fois. Si un troisième joueur tente de se connecter, il reçoit un message d’erreur indiquant que la partie est complète.
Une fois la partie terminée, les deux clients se déconnectent et le serveur est à nouveau prêt à accueillir deux nouveaux joueurs.

### Serveur

Pour lancer le serveur il suffit d'indiquer le `<type> : SERVER`. Le serveur prend l'adresse IP 0.0.0.0 et le port 4444.

```bash
// se trouver à la racine du projet /dai-work-2/
java -jar target/dai-work-2-1.0-SNAPSHOT.jar SERVER
```

Pour arrêter un serveur, il suffit de faire un `ctrl + c` dans le terminal.

### Client

Il est possible de lancer un client en local ou sur une autre machine et lui indiquer l'IP à atteindre via l'option `--hostname=<hostname>`. Par défaut, l'hostname est sur localhost.
L'option est utile si l'on veut joindre le serveur depuis une autre machine. Pour le développement en local, il est préférable de spécifier aucune adresse et garder celle par défaut.

```bash
// se trouver à la racine du projet /dai-work-2/
java -jar target/dai-work-2-1.0-SNAPSHOT.jar CLIENT
```

```bash
// se trouver à la racine du projet /dai-work-2/
java -jar target/dai-work-2-1.0-SNAPSHOT.jar CLIENT --hostname="192.168.1.32"
```

## Docker
Pour unifier l'environnement de développement et d'exécution, un Dockerfile a été mis en place afin que le client et
le serveur puissent être exécutés dans des conteneurs. Ceux-ci communiquent entre eux via un réseau Docker, que vous devrez démarrer manuellement.
Veuillez suivre les étapes ci-dessous dans l'ordre pour configurer correctement l'environnement.

> [!CAUTION]
> Vous devez absolument avoir le fichier .jar du projet ainsi : target/dai-work-2-1.0-SNAPSHOT.jar
>

> [!CAUTION]
> Vous devez avoir Docker déjà installé sur votre machine.
> [Lien d'installation officiel](https://docs.docker.com/engine/install/)
>

#### Créer l'image
```bash
docker build -t p4app .
```

#### Lancement du réseau

```bash
# création du réseau docker
docker network create p4network
```
Vous pouvez utiliser n'importe quel nom pour le réseau.

#### Lancement du serveur

```bash
#Lancement du serveur en arrière plan
docker run -d --network p4network --name server-p4 p4app:latest
```
Le nom du serveur sera utilisé par le client dans l'option `--hostname`.
Vous pouvez voir les logs avec la commande suivante :
```bash
# affiche les logs du serveur dans le terminal
docker logs -f server-p4
```

#### Lancement d'un client en mode itératif

```bash
# lance le client 1
docker run -it --rm --network p4network --name client1 p4app:latest CLIENT --hostname=server-p4
```
```bash
# lance le client 2
docker run -it --rm --network p4network --name client2 p4app:latest CLIENT --hostname=server-p4
```
Assurez-vous d’utiliser le nom correct du réseau pour l’option `--network`, ainsi que le nom du serveur défini précédemment pour l’option `--hostname`.
Et faites attention à ne pas utiliser deux fois le même nom de client pour l'option `--name`.

### Publication sur GitHub Container Registry
1. Créer l'image en local via Dockerfile
```bash
docker build -t p4app .
```

2. Créer un Token (classic) sur GitHub. Voir la [doc](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry) officielle pour générer son personal access token.
3. Se connecter à GitHub Container Registry
```bash
# Login to GitHub Container Registry
docker login ghcr.io -u <username>
```

4. Taguer l'image
```bash
docker tag p4app:latest ghcr.io/<username>/p4app:latest
```

5. Publier l'image
```bash
   docker push ghcr.io/<username>/p4app:latest
```

### Utiliser l'image depuis GitHub Container Registry
Vous pouvez récuperer l'image via la commande suivante :
```bash
docker pull ghcr.io/quentinmichon/p4app:latest
```

## Auteurs
- [Quentin Michon](https://github.com/QuentinMichon)
- [Gianni Bee](https://github.com/GinByte)

Avec l’aide de Copilot et ChatGPT 5 pour la rédaction des en-têtes de fonctions.