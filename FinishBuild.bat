call mvn clean package assembly:single

ren "target\PluginStats-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "MineStats.jar"

copy .\target\MineStats.jar .\
