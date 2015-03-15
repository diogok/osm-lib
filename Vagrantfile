# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network "private_network", ip: "192.168.50.35"

  config.vm.provider "virtualbox" do |v|
    v.memory = 2048
    v.cpus = 2
  end

  config.vm.provision "docker" do |d|
    d.run "cncflora/elasticsearch", name: "elasticsearch",args: "-p 9200:9200"
    d.run "cncflora/couchdb", name: "couchdb", args: "-p 5984:5984 -p 9001:9001 --link elasticsearch:elasticsearch -v /var/couchdb:/var/lib/couchdb:rw"
  end
                                                
  config.vm.provision :shell, inline: <<SCRIPT
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
    add-apt-repository ppa:webupd8team/java
    apt-get update && aptitude safe-upgrade -y && aptitude install curl git tmux vim htop oracle-java8-installer -y
    wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
    chmod +x /usr/bin/lein
SCRIPT

end

