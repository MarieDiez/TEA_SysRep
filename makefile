server_class := Traitement ServeurHttps

all: help server
	cd build && java -jar ServeurHttps.jar

%: src/%.java
	javac -cp "bin/." -d bin $?

server: $(server_class)
	cd bin && jar cfe ../build/ServeurHttps.jar ServeurHttps $(addsuffix .class, $(server_class))

clean:
	rm -f build/*.jar
	rm -f bin/*.class

help:
	@echo "Pour lancer le serveur : make"
	@echo "Pour lancer le client : 'https://localhost:1234/addition.html'"
	@echo ""