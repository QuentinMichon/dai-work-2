# DAI WORK 2 - client/serveur TCP : Puissance 4"
![Static Badge](https://img.shields.io/badge/HEIG--VD-labo-red?logo=intellijidea)
![Maven](https://img.shields.io/badge/build-Maven-blue?logo=apachemaven)
![Java](https://img.shields.io/badge/java-21-orange?logo=openjdk)

## Protocole
La description du protocol applicatif est disponible [ici](protocol/README.md)

## Docker
Pour unifier l'environnement de développement et d'exécution, un Dockerfile a été mis en place afin que le client et 
le serveur puissent être exécutés dans des containers. Ceux-ci communiquent entre eux via un réseau Docker, que vous devrez démarrer manuellement. 
Veuillez suivre les étapes ci-dessous dans l'ordre pour configurer correctement l'environnement.

> [!CAUTION]
> Vous devez absolument avoir le fichier .jar du projet comme ceci target/dai-work-2-1.0-SNAPSHOT.jar
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
Le nom du serveur sera utilisé par le client dans l'option `--hostame`.
Vous pouvez voir les logs avec la commande suivante :
```bash
# affiche les logs du serveur dans le terminal
docker logs -f server-p4
```

#### Lancement d'un client en mode itrératif 

```bash
# lance le client 1
docker run -it --rm --network p4network --name client1 p4app:latest CLIENT --hostname=server-p4
```
```bash
# lance le client 2
docker run -it --rm --network p4network --name client2 p4app:latest CLIENT --hostname=server-p4
```
Assurez-vous d’utiliser le nom correct du réseau pour l’option `--network`, ainsi que le nom du serveur défini précédemment pour l’option `--hostname`.
Et faite attention à ne pas utiliser deux fois le même nom de client pour l'option `--name`.

## Auteurs
- [Quentin Michon](https://github.com/QuentinMichon)
- [Gianni Bee](https://github.com/GinByte)

Avec l’aide de Copilot et ChatGPT 5 pour la rédaction des en-têtes de fonctions.