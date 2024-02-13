# Configurazione

In questo file viene spiegato come avviare i progetti Deception-Deamon e Deception-Client. E' necessario avere Docker per l'avvio del database e maven per i progetti Spring.


## Avvio del database
Da terminale, lanciare i seguenti comandi:

```docker pull mysql:latest```

```docker run -d --name containerMySql -e MYSQL_ROOT_PASSWORD=cybersecdcg -e MYSQL_DATABASE=fakeDataDb -p 3306:3306 mysql:latest```


In questo modo verrà avviato un container con all'interno un database mySql.


## Avvio del demone e del client

Per entrambi i progetti, lanciare le task ```mvn clean``` e ```mvn install``` ed avviarli.

Il demone si avvierà sulla porta 8076, mentre il cliente sulla porta 8075. 


### Login nel client

E' possibile accedere tramite le credenziali admin admin o user user (oppure aggiungerne nel file credentials.txt)


## Avvio RestApi Server generato 
Per ora, non essendo hostati su cloud esterno il demone ed il client (ma tutto in locale), bisogna avviere il server generato NON da docker.
