FROM ubuntu:18.04
# Get secret arguments
ARG DB_CONN_STRING
ARG DB_USERNAME
ARG DB_PASSWORD
ARG DB_TEST_CONN_STRING
ENV DB_CONN_STRING_VAR=${DB_CONN_STRING}
ENV DB_USERNAME_VAR=${DB_USERNAME}
ENV DB_PASSWORD_VAR=${DB_PASSWORD}
ENV DB_TEST_CONN_STRING_VAR=${DB_TEST_CONN_STRING}

# Updating packages
RUN apt-get update --fix-missing

RUN apt-get -y install git
RUN echo "CLONING REPOSITORY"
RUN git clone https://github.com/DevOps2021-gb/devops2021.git
RUN echo "INSTALLING MAVEN"
RUN apt install maven -y
WORKDIR "/devops2021/java-itu-minitwit"
RUN echo "BUILDING PROJECT"
RUN mvn -B package --file pom.xml -DDB_USER=${ DB_USERNAME_VAR } -DDB_PASSWORD=${ DB_PASSWORD_VAR } -DDB_TEST_CONNECTION_STRING=${ DB_TEST_CONN_STRING_VAR }
WORKDIR "/devops2021/java-itu-minitwit/target"

EXPOSE 4567
CMD java -jar java-itu-minitwit-1.0-SNAPSHOT.jar ${DB_CONN_STRING_VAR} ${DB_USERNAME_VAR} ${DB_PASSWORD_VAR}
