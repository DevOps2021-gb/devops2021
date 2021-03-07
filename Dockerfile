FROM ubuntu:18.04
# Get secret arguments
ARG CONN_STRING
ARG DB_USER
ARG DB_PW
ENV CONN_STRING_VAR=${CONN_STRING}
ENV DB_USER_VAR=${DB_USER}
ENV DB_PW_VAR=${DB_PW}

# Updating packages
RUN apt-get update --fix-missing

RUN apt-get -y install git
RUN echo "CLONING REPOSITORY"
RUN git clone https://github.com/DevOps2021-gb/devops2021.git -b feature/CD/DB
RUN echo "INSTALLING MAVEN"
RUN apt install maven -y
WORKDIR "/devops2021/java-itu-minitwit"
RUN echo "BUILDING PROJECT"
RUN mvn package
WORKDIR "/devops2021/java-itu-minitwit/target"

EXPOSE 4567
CMD java -jar java-itu-minitwit-1.0-SNAPSHOT.jar ${CONN_STRING_VAR} ${DB_USER_VAR} ${DB_PW_VAR}
