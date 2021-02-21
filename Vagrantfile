# -*- mode: ruby -*-
# vi: set ft=ruby :

# Since the webserver needs the IP of the DB server the two have to be started 
# in the right order and with storing the IP of the latter on the way:
#
# $ rm db_ip.txt | vagrant up | python store_ip.py

$ip_file = "db_ip.txt"

Vagrant.configure("2") do |config|
    config.vm.box = 'digital_ocean'
    config.vm.box_url = "https://github.com/devopsgroup-io/vagrant-digitalocean/raw/master/box/digital_ocean.box"
    config.ssh.private_key_path = '~/.ssh/id_rsa'
    config.vm.synced_folder ".", "/vagrant", type: "rsync"
  

    config.vm.define "webserver", primary: false do |server|
  
      server.vm.provider :digital_ocean do |provider|
        provider.ssh_key_name = ENV["SSH_KEY_NAME"]
        provider.token = ENV["DIGITAL_OCEAN_TOKEN"]
        provider.image = 'ubuntu-18-04-x64'
        provider.region = 'fra1'
        provider.size = 's-1vcpu-1gb'
        provider.privatenetworking = true
      end

      server.vm.hostname = "webserver"

      server.vm.provision "shell", inline: <<-SHELL
        
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
	
	sudo mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root'; FLUSH privileges;"
	sudo service mysql restart
	# create client database
	sudo mysql -u root -proot -e "CREATE DATABASE minitwit;"
        

        echo "CLONING REPOSITORY"
	sudo git clone https://github.com/JesperFalkenberg/devops2021.git -b feature/deploy
	echo "INSTALLING MAVEN"
	sudo apt install maven -y
	cd devops2021/java-itu-minitwit/
	echo "BUILDING PROJECT"
	sudo mvn package
	cd target
	
	sudo java -jar java-itu-minitwit-1.0-SNAPSHOT.jar

        echo "================================================================="
        echo "=                            DONE                               ="
        echo "================================================================="
        echo "Navigate in your browser to:"
        THIS_IP=`hostname -I | cut -d" " -f1`
        echo "http://${THIS_IP}:4567"
      SHELL
    end
    config.vm.provision "shell", privileged: false, inline: <<-SHELL
      sudo apt-get update
    SHELL

  end
