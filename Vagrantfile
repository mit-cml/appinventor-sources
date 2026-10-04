# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.

Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/bionic64"

  config.vm.boot_timeout = 400

  config.vm.provider "virtualbox" do |v|
    v.name = "ForAppinventor2-bionic64"
    v.memory = "4096"
    v.customize ["modifyvm", :id, "--usb", "on"]
    # fix for slow network
    v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    v.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
    v.customize ["modifyvm", :id, "--nictype1", "virtio"]
  end

  config.vm.provider :libvirt do |libvirt|
    config.vm.box = "generic/ubuntu1804"
    # By default, './' is synced onto '/vagrant' with NFS, which does not work for debian family images for some reason;
    # hence we use rsync. There are a few broken links in the source tree that will fail the default rsync syncing sche-
    # me, so we give explicit rsync args and omit "--copy-links".
    config.vm.synced_folder './', '/vagrant', type: 'rsync', rsync__args: ["--verbose", "--archive", "--update"]
  end

  config.vm.provision :shell, path: "bootstrap.sh"

  config.vm.network :forwarded_port, guest: 8888, host: 8888
  config.vm.network :forwarded_port, guest: 9876, host: 9876
  config.vm.network :forwarded_port, guest: 9990, host: 9990

end
