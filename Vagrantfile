# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  # config.vm.box = "base"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider "virtualbox" do |vb|
  #   # Display the VirtualBox GUI when booting the machine
  #   vb.gui = true
  #
  #   # Customize the amount of memory on the VM:
  #   vb.memory = "1024"
  # end
  #
  # View the documentation for the provider you are using for more
  # information on available options.
  config.vm.define "dev" do |dev|
    dev.vm.box = "ubuntu/trusty64"
    dev.vm.provider :virtualbox do |vb|
      vb.name = "information_palace_dev"
      vb.gui = false
      vb.memory = "8192"
      vb.cpus = 2
      vb.customize ["modifyvm", :id, "--nictype1", "virtio"]
    end
    dev.vm.network "private_network", ip: "192.168.9.24"
  end

  config.vm.define "awsdemo", autostart: false do |demo|
    demo.vm.box = "dummy"
    rsync_excludes = ["data/", ".git/", ".gitignore/", "infop-static/", "information_palace/", "output/", "**/.idea/", "**/.sbtserver/", "**/target/", "**/logs/"]
    rsync_args = ["--verbose", "--archive", "--delete", "-z"]
    demo.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: rsync_excludes, rsync__agrs: rsync_args

    demo.vm.provider :aws do |aws, override|
      aws.access_key_id = ENV['AWS_ACCESS_KEY_ID']
      aws.secret_access_key = ENV['AWS_SECRET_ACCESS_KEY']
      aws.keypair_name = "all_purpose"

      aws.ami = "ami-fce3c696"

      aws.instance_type = "m4.large"
      aws.elastic_ip = true
      aws.region = "us-east-1"
      aws.security_groups = ["sg-0ca04c74"]
      aws.subnet_id = "subnet-f1e550a8"
      aws.tags = {
        Name: "Vagrant - Information Palace"
      }
      aws.terminate_on_shutdown = false

      aws.block_device_mapping = [{"DeviceName" => "/dev/sda1", "Ebs.VolumeSize" => 20}]

      override.ssh.username = "ubuntu"
      override.ssh.private_key_path = "all_purpose.pem"
    end
  end


  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL

  config.omnibus.chef_version = "12.7.2"
  config.berkshelf.enabled = true
  config.vm.provision "dependencies", :type => "chef_solo" do |chef|
    chef.node_name = "infop-master"
    chef.nodes_path = "nodes"
    chef.roles_path = "roles"
    chef.cookbooks_path = "cookbooks"

    chef.add_role "infop_master"
  end

  config.vm.provision "batch_cluster", :type => "chef_solo", :run => "always" do |chef|
    chef.node_name = "infop-master"
    chef.nodes_path = "nodes"
    chef.roles_path = "roles"
    chef.cookbooks_path = "cookbooks"
    chef.add_recipe "batch_cluster"
  end

  config.vm.provision "expo", :type => "chef_solo", :run => "always" do |chef|
    chef.node_name = "infop-master"
    chef.nodes_path = "nodes"
    chef.roles_path = "roles"
    chef.cookbooks_path = "cookbooks"
    chef.add_recipe "expo"
  end

end
