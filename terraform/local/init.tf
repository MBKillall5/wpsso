terraform {
  required_providers {
    virtualbox = {
      source  = "terra-farm/virtualbox"
      version = "0.2.2-alpha.1"
    }
  }
}

resource "virtualbox_vm" "control_node" {
  count     = 1
  name      = "control-node"
#  image     = "https://app.vagrantup.com/generic/boxes/ubuntu2104/versions/3.6.12/providers/virtualbox.box"
  image     = "${path.module}/image/ubuntu2104.box"
  cpus      = 1
  memory    = "512 mib"

  network_adapter {
     type           = "nat"
  }

  network_adapter {
    type           = "hostonly"
    host_interface = "VirtualBox Host-Only Ethernet Adapter"
  }
}

resource "virtualbox_vm" "node" {
  count     = 2
  name      = format("node-%02d", count.index + 1)
#  image     = "https://app.vagrantup.com/generic/boxes/ubuntu2104/versions/3.6.12/providers/virtualbox.box"
  image     = "${path.module}/image/ubuntu2104.box"
  cpus      = 1
  memory    = "512 mib"

  network_adapter {
     type           = "nat"
  }

  network_adapter {
    type           = "hostonly"
    host_interface = "VirtualBox Host-Only Ethernet Adapter"
  }

}

output "IPAddr_node1" {
  value = element(virtualbox_vm.control_node.*.network_adapter.0.ipv4_address, 1)
}