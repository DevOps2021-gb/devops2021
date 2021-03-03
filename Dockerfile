FROM ubuntu:18.04

# Updating packages
RUN apt-get update --fix-missing

# ---------------------------------------
#          Repository Setup
# ---------------------------------------

RUN apt-get -y install git
RUN echo "CLONING REPOSITORY"
RUN git clone https://github.com/DevOps2021-gb/devops2021.git -b deploy/actions
RUN echo "INSTALLING MAVEN"
RUN apt install maven -y
WORKDIR "/devops2021/java-itu-minitwit"
RUN echo "BUILDING PROJECT"
RUN mvn package
WORKDIR "/devops2021/java-itu-minitwit/target"

EXPOSE 4567

ENTRYPOINT ["java", "-jar", "java-itu-minitwit-1.0-SNAPSHOT.jar"]