# selenium-automation

Run large scale browser infrastructure using Aerokube Grid Router with SeleniumHQ official images or Aerokube Selenoid images on Amazon EC2 or locally.
It is designed to execute selenium based tests in different browsers with high parallelization.

#### Key features

- Supports running different browsers at same time.
- Scalable, in fully distributed mode offers router->hub->node->browser infrastructure (solves the single selenium hub limitations).
- Local or remote execution - able to run on localhost and/or in different AWS accounts.
- Dockerized - No more dealing with webdriver-browser compatibility issues.
- Supports different python interpreters across regions.
- Supports different AMIs for browser infrastructure.

#### TODO

- opera
- IE with Windows image support
- test no_router=false on localhost

## Prerequisites

### Development mode

* Docker
* Docker-compose

### Production mode

* Python > 2.5
* Ansible (latest)
* Remote code can be run with python2

  ```
  Amazon linux 2:
  sudo amazon-linux-extras install ansible2

  Pip:
  sudo yum update
  sudo yum install python3-pip
  OR
  sudo apt update
  sudo apt install python3-pip
  OR
  curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
  python3 get-pip.py
  THEN:
  pip3 install ansible
  ```
* Docker - installed via ansible
* Pip - installed via ansible

  - boto
  - docker(-py)

### Supported AMIs:

- Amazon Linux 2
- Ubuntu

## Run in development mode

Suitable for localhost execution while developing tests. Much simpler solution requiring only docker engine and docker-compose executable.

* Adjust browser types and versions in `docker/browsers.json` according to your needs.
* All browser docker images must be downloaded (docker pull) before execution, it can be done in run script itself: `run-selenoid-docker-compose.sh`
* Run the grid:

```bash
./run-selenoid-docker-compose.sh
```

* Redirect your tests to remote url: `webdriver.remote.url=http://localhost:4444/wd/hub` and specify your browser preference e.g.: `webdriver.driver=chrome`

## Run in production mode

### Providing AWS credentials vs. localhost execution

The following environment variables can be used: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION.
If not provided scripts will run on localhost.

#### Playbooks:

_**deploy-selenoid.yml**_ and _**deploy-seleniumhq-single.yml**_ are able to run with `single_hub_node=true` only.

_**deploy-eleniumhq-distributed.yml**_ is capable of running browser nodes on different hosts with `single_hub_node=false`.

#### Run Modes:

- `remote_run=true` - when AWS Credentials are provided.
  - if `no_router=true` - only single_hub_node is created as it set `single_hub_node=true` accordingly (hub_count, max_nodes_per_hub properties are ignored)
  - if `no_router=false` and `single_hub_node=true` - single_hub_node with remote router is created (hub_count, max_nodes_per_hub properties are ignored). Designed for remote execution only.
  - if `no_router=false` and `single_hub_node=false` - this results in fully distributed infrastructure. Designed for SeleniumHQ images only.
- `remote_run=false` - when AWS Credentials are not present in environment. It will set `single_hub_node=true` as it executes all locally.
  - TODO: test router / no router capabilities

Edit or add all necessary properties in:

- `vars/settings_<AWS_REGION>.yml` or `vars/settings_localhost.yml`
- `vars/selenoid.yml`

Or

Provide extra vars as command line option:

```bash
ansible-playbook deploy-selenoid.yml --extra-vars "version=1.23.45 other_variable=foo"
```

### Run clean-up tasks

use for removing instances in EC2

- __Clean-up instances defined in instances.txt:__

  ```
  ansible-playbook ec2-cleanup.yml
  ```
- __Clean-up all instances based on tag "Type":__

  ```
  ansible-playbook ec2-cleanup-all.yml
  ```

### Run Selenoid infrastructure:

Selenoid hub and nodes are deployed on single host.

Edit or add all necessary properties in:

- `vars/settings_<AWS_REGION>.yml` or `vars/settings_localhost.yml`
- `vars/selenoid.yml`

```bash
ansible-playbook deploy-selenoid.yml
```

### Run SeleniumHQ single-node infrastructure:

Selenium hub and browser nodes are deployed on a single host. Browser container nodes are linked to hub container on single host.

Edit all necessary properties in:

- `vars/settings_<AWS_REGION>.yml` or `vars/settings_localhost.yml`
- `vars/seleniumhq.yml`

```bash
ansible-playbook deploy-seleniumhq-single.yml
```

### Run SeleniumHQ multi-node infrastructure:

Selenium hub and browser nodes are deployed on separate hosts. Browser images on nodes run on different ports.

Edit all necessary properties in:

- `vars/settings_<AWS_REGION>.yml` or `vars/settings_localhost.yml`
- `vars/seleniumhq.yml`

```bash
ansible-playbook deploy-seleniumhq-distributed.yml
```

### Inventory Examples

**no_router=false**
&&
**single_hub_node=true**

```yaml
grid_router_groups:
-   name: grid_group_01
    hub_host: hub3
    node_host_list:
    -   hub3
-   name: grid_group_02
    hub_host: hub2
    node_host_list:
    -   hub2
-   name: grid_group_03
    hub_host: hub1
    node_host_list:
    -   hub1

grid_router_regions:
-   name: us-west-1
    hosts:
    -   name: hub3
        port: 4444
        browser_count: 10
    -   name: hub2
        port: 4444
        browser_count: 10
    -   name: hub1
        port: 4444
        browser_count: 10

grid_router_browsers:
-   defaultVersion: '75.0'
    name: firefox
    versions:
    - '75.0'
-   defaultVersion: '81.0'
    name: chrome
    versions:
    - '81.0'
```

**no_router=true** (single_hub_node=true)

```yaml
grid_router_groups:
-   name: grid_group_01
    hub_host: hub3
    node_host_list:
    -   hub3

grid_router_regions:
-   name: us-west-1
    hosts:
    -   name: hub3
        port: 4444
        browser_count: 10

grid_router_browsers:
-   defaultVersion: '75.0'
    name: firefox
    versions:
    - '75.0'
-   defaultVersion: '81.0'
    name: chrome
    versions:
    - '81.0'
```

**no_router=false**
&&
**single_hub_node=false**

```yaml
grid_router_groups:
-   name: grid_group_01
    hub_host: hub3
    node_host_list:
    -   node11
    -   node10
    -   node13
    -   node12
    -   node15
-   name: grid_group_02
    hub_host: hub2
    node_host_list:
    -   node14
    -   node9
    -   node8
    -   node1
    -   node3
-   name: grid_group_03
    hub_host: hub1
    node_host_list:
    -   node2
    -   node5
    -   node4
    -   node7
    -   node6

grid_router_regions:
-   name: us-west-1
    hosts:
    -   name: hub3
        port: 4444
        browser_count: 50
    -   name: hub2
        port: 4444
        browser_count: 50
    -   name: hub1
        port: 4444
        browser_count: 50

grid_router_browsers:
-   defaultVersion: '75.0'
    name: firefox
    versions:
    - '75.0'
-   defaultVersion: '81.0'
    name: chrome
    versions:
    - '81.0'
```

### Single Hub / Router URL

After successful deployment should be exposed in `selenium_hub_host.txt` file

### To test grid inventory yaml creation

```ansible-playbook deploy-selenoid.yml -v --skip-tags "ec2,config"```

### Test file with static host inventory

located in `Ansible/inventory/hosts.yml`

In order to use it uncomment last line in `ansible.cfg`

## UI and useful endpoints

__selenoid-ui:__ http://hostname:8080/

__selenoid:__ http://hostname:4444/status

__selenium hub:__ http://hub_hostname:4444/console

## Releases

__Selenoid Browser images:__ https://aerokube.com/selenoid/latest/#_browser_image_information

__Selenoid:__ https://github.com/aerokube/selenoid/releases

__SeleniumHQ:__ https://github.com/SeleniumHQ/docker-selenium/releases

## Reference guides

https://aerokube.com/selenoid/latest/
