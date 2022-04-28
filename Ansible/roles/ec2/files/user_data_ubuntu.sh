#!/usr/bin/env bash
#sudo apt-get install -y unzip software-properties-common
#sudo apt-add-repository ppa:ansible/ansible
#sudo apt-get update
#sudo apt-get install -y ansible
sudo apt-get remove unattended-upgrades
sudo sed -i 's/APT::Periodic::Update-Package-Lists "1"/APT::Periodic::Update-Package-Lists "0"/g' /etc/apt/apt.conf.d/10periodic
sudo apt-get update
sudo apt-get install python-apt -y
sudo apt-get install python-pip -y
sudo /usr/bin/python -m pip install boto boto3