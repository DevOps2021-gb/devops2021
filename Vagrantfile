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
  
    config.vm.define "dbserver", primary: true do |server|
      server.vm.provider :digital_ocean do |provider|
        provider.ssh_key_name = ENV["SSH_KEY_NAME"]
        provider.token = ENV["DIGITAL_OCEAN_TOKEN"]
        provider.image = 'ubuntu-18-04-x64'
        provider.region = 'fra1'
        provider.size = 's-1vcpu-1gb'
        provider.privatenetworking = true
      end
  
      server.vm.hostname = "dbserver"

      server.trigger.after :up do |trigger|
        trigger.info =  "Writing dbserver's IP to file..."
        trigger.ruby do |env,machine|
          remote_ip = machine.instance_variable_get(:@communicator).instance_variable_get(:@connection_ssh_info)[:host]
          File.write($ip_file, remote_ip)
        end 
      end

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

		# Allow External Connections on your MySQL Service
		sudo sed -i -e 's/bind-addres/#bind-address/g' /etc/mysql/mysql.conf.d/mysqld.cnf
		sudo sed -i -e 's/skip-external-locking/#skip-external-locking/g' /etc/mysql/mysql.conf.d/mysqld.cnf
		sudo mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root'; FLUSH privileges;"
		sudo service mysql restart
		# create client database
		sudo mysql -u root -proot -e "CREATE DATABASE minitwit;"
      SHELL
    end

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

      server.trigger.before :up do |trigger|
        trigger.info =  "Waiting to create server until dbserver's IP is available."
        trigger.ruby do |env,machine|
          ip_file = "db_ip.txt"
          while !File.file?($ip_file) do
            sleep(1)
          end
          db_ip = File.read($ip_file).strip()
          puts "Now, I have it..."
          puts db_ip
        end 
      end

      server.trigger.after :provision do |trigger|
        trigger.ruby do |env,machine|
          File.delete($ip_file) if File.exists? $ip_file
        end 
      end

      server.vm.provision "shell", inline: <<-SHELL
        export DB_IP=`cat /vagrant/db_ip.txt`
        echo $DB_IP

        echo "CLONING REPOSITORY"
	sudo git clone https://github.com/JesperFalkenberg/devops2021.git -b feature/deploy
	echo "INSTALLING MAVEN"
	sudo apt install maven -y
	cd devops2021/java-itu-minitwit/
	echo "BUILDING PROJECT"
	sudo mvn package
	cd target
	echo "EXECUTING JAR"
	sudo java -jar java-itu-minitwit-1.0-SNAPSHOT.jar $DB_IP

        echo $DB_IP

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
