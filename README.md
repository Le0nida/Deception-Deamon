# Configurazione

In questo file viene spiegato come avviare i progetti Deception-Deamon e Deception-Client. E' necessario avere Docker per l'avvio del database e maven per i progetti Spring, altrimenti è possibile utilizzare Docker per ogni componente.


# Avvio completo tramite Docker
Una volta scaricati entrambi i progetti in due cartelle distinte, è sufficiente lanciare il file docker-compose.yml all'interno del progetto Deception-Deamon

# Avvio tramite Docker e Maven

## Avvio del database
Da terminale, lanciare i seguenti comandi:

```docker pull mysql:latest```

```docker run -d --name containerMySql -e MYSQL_ROOT_PASSWORD=cybersecdcg -e MYSQL_DATABASE=fakeDataDb -p 3306:3306 mysql:latest```


In questo modo verrà avviato un container con all'interno un database mySql.


## Avvio del demone e del client

Per entrambi i progetti, lanciare le task ```mvn clean``` e ```mvn install``` ed avviarli.

Il demone si avvierà sulla porta 8076, mentre il cliente sulla porta 8075. 

NOTA: per entrambi i progetti sono necessarie delle variabili d'ambiente da inserire (si possono trovare nel file docker-compose.yml (sostiture i nomi delle component con localhost)


### Login nel client

E' possibile accedere tramite le credenziali admin admin o user user (oppure aggiungerne nel file credentials.txt)
