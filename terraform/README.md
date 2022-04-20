sudo ifconfig enp0s17 up
sudo dhclient enp0s17 -v

#login in node-4 (control)

#copy vagrant.key into the control node
sftp> put  terraform/local/image/keys/vagrant.key ~/.ssh/id_rsa
sftp> chmod 600 .ssh/id_rsa

ssh -i ./keys/vagrant.key vagrant@xxx


#install ansible
sudo apt update
sudo add-apt-repository --yes --update ppa:ansible/ansible
sudo apt install ansible

#edit the inventory
sudo nano /etc/ansible/hosts
[wp-nodes]
192.168.56.125
[keycloack]
192.168.56.126


#edit /etc/ansible/ansible.cfg
[defaults]
host_key_checking = False

#check connectivity
vagrant@ubuntu2110:~$ ansible all -m ping
[WARNING]: Invalid characters were found in group names but not replaced, use
-vvvv to see details
192.168.56.125 | SUCCESS => {
    "ansible_facts": {
        "discovered_interpreter_python": "/usr/bin/python3"
    },
    "changed": false,
    "ping": "pong"
}
192.168.56.126 | SUCCESS => {
    "ansible_facts": {
        "discovered_interpreter_python": "/usr/bin/python3"
    },
    "changed": false,
    "ping": "pong"
}


#enable NAT on all nodes
ansible all -a "sudo dhclient enp0s17 -v"

cd ~
git clone https://github.com/do-community/ansible-playbooks.git
cd ansible-playbooks
cd wordpress-lamp_ubuntu1804
nano vars/default.yml
:~/ansible-playbooks/wordpress-lamp_ubuntu1804$ ansible-playbook playbook.yml -l wp-nodes