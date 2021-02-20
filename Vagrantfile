# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "generic/ubuntu1804"

  config.vm.network "private_network", type: "dhcp"

  # For two way synchronization you might want to try `type: "virtualbox"`
  config.vm.synced_folder ".", "/vagrant", type: "rsync"

  config.vm.define "dbserver", primary: true do |server|
    server.vm.network "forwarded_port", guest: 3306, host: 3306
    server.vm.network "private_network", ip: "192.168.20.2"
    server.vm.provider "virtualbox" do |vb|
      vb.memory = "1024"
    end
    server.vm.hostname = "dbserver"
    server.vm.provision "shell", privileged: false, inline: <<-SHELL
    	# https://gist.github.com/csotomon/fe2bde0f9b76e53896294d64ac3b54d5
    	# Updating packages
	sudo apt-get update

	# ---------------------------------------
	#          MySQL Setup
	# ---------------------------------------

	# Setting MySQL root user password root/root
	sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password root'
	sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root'


	# Installing packages
	sudo apt-get install -y mysql-server mysql-client

	# Allow External Connections on your MySQL Service
	sudo sed -i -e 's/bind-addres/#bind-address/g' /etc/mysql/mysql.conf.d/mysqld.cnf
	sudo sed -i -e 's/skip-external-locking/#skip-external-locking/g' /etc/mysql/mysql.conf.d/mysqld.cnf
	sudo mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root'; FLUSH privileges;"
	sudo service mysql restart
	# create client database
	sudo mysql -u root -proot -e "CREATE DATABASE minitwit;"
    SHELL
  end

  config.vm.define "webserver", primary: true do |server|
    server.vm.network "private_network", ip: "192.168.20.3"
    # server.vm.network "forwarded_port", guest: 5000, host: 5000
    server.vm.provider "virtualbox" do |vb|
      vb.memory = "1024"
    end
    server.vm.hostname = "webserver"
    server.vm.provision "shell", privileged: false, inline: <<-SHELL
    	echo "CLONING REPOSITORY"
	sudo git clone https://github.com/JesperFalkenberg/devops2021.git -b feature/deploy
	echo "INSTALLING MAVEN"
	sudo apt install maven -y
	cd devops2021/java-itu-minitwit/
	echo "BUILDING PROJECT"
	sudo mvn package
	cd target
	echo "EXECUTING JAR"
	sudo java -jar java-itu-minitwit-1.0-SNAPSHOT.jar
    SHELL
  end

  config.vm.provision "shell", privileged: false, inline: <<-SHELL
    sudo apt-get update
  SHELL
end

