FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD auction-0.0.1-SNAPSHOT.jar auction-0.0.1-SNAPSHOT.jar
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-javaagent:/data/skywalking-agent.jar","-Dskywalking.agent.service_name=auction","-Dskywalking_config=/data/agent.config","-Dskywalking.collector.backend_service=172.20.0.1:30335","-jar","/auction-0.0.1-SNAPSHOT.jar"]
