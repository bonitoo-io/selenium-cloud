import groovy.json.JsonOutput

pipeline {
    agent { label 'master' }
    options {
        timestamps()
        timeout(activity: false, time: 180)
    }
    stages {
        stage('Preparation') { // for display purposes
            steps {
                cleanWs()
                retry(3) {
                    git([url: 'https://github.com/bonitoo-io/selenium-cloud.git', branch: 'main'])
                }
                configFileProvider([configFile(fileId: 'configFileId', targetLocation: 'Ansible/vars/')]) {
                }
            }
        }
        stage('SeleniumGridStart') {
            steps {
                script {
                    if (isUnix()) {
                        def data = [
                                hub_instance_type    : params.HUB_INSTANCE_TYPE,
                                hub_count            : params.HUB_COUNT.toInteger(),
                                selenoid_limit       : params.BROWSER_COUNT.toInteger(),
                                tag_type             : params.TAG_TYPE,
                                request_spot_instance: params.REQUEST_SPOT_INSTANCE,
                                no_router            : params.NO_ROUTER
                        ]
                        def data_json = JsonOutput.toJson(data)
                        withAWS(credentials: 'service-user', region: 'eu-central-1') {
                            ansiblePlaybook(
                                    credentialsId: 'amazon-ec2-user',
                                    extras: "-v --extra-vars \'${data_json}\'",
                                    installation: 'ansible',
                                    disableHostKeyChecking: true,
                                    playbook: 'Ansible/deploy-selenoid.yml'
                            )
                        }
                    } else {
                        echo 'This can be run on linux system only'
                    }
                    def tasks = [:]
                    def seleniumHubHost = readFile 'selenium_hub_host.txt'
                    def browsers = "${BROWSERS}".tokenize(",")
                    echo "${browsers}"
                    for (int i = 0; i < browsers.size(); i++) {
                        def index = i
                        String BROWSER = browsers.get(i)
                        stage("selenium-tests ${BROWSER}") {
                            tasks["task_${BROWSER}"] = {
                                build job: 'selenium-tests', parameters: [
                                        string(name: 'HUB_HOST', value: "${seleniumHubHost.trim()}"),
                                        string(name: 'BROWSER', value: "${BROWSER}"),
                                        string(name: 'PARAMS', value: "${PARAMS}"),
                                        string(name: 'dummy', value: "${index}")
                                ], propagate: false
                            }

                        }
                    }

                    parallel tasks
                    sleep 10
                }
            }
        }
        stage('SeleniumGridStop') {
            steps {
                script {
                    if (isUnix()) {
                        def data = [
                                request_spot_instance: params.REQUEST_SPOT_INSTANCE
                        ]
                        def data_json = JsonOutput.toJson(data)
                        withAWS(credentials: 'service-user', region: 'eu-central-1') {
                            ansiblePlaybook(
                                    credentialsId: 'amazon-ec2-user',
                                    extras: "-v --extra-vars \'${data_json}\'",
                                    installation: 'ansible',
                                    disableHostKeyChecking: true,
                                    playbook: 'Ansible/ec2-cleanup.yml'
                            )
                        }
                    } else {
                        echo 'This can be run on linux system only'
                    }
                }
            }
        }
        stage('Results') {
            steps {
                archiveArtifacts '**/*.txt,**/inventory.yml'
            }
        }
    }
    post {
        always {
            discordSend(
                    link: ${env.BUILD_URL},
                    result: ${currentBuild.currentResult},
                    title: ${env.JOB_NAME},
                    webhookURL: "https://discord.com/api/webhooks/"
            )
        }
    }
}