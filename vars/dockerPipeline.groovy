def call(Map config = [:]) {

    pipeline {
        agent any

        stages {

            stage('Clone Repository') {
                steps {
                    git config.gitUrl
                }
            }

            stage('Build Docker Image') {
                steps {
                    sh "docker build -t ${config.imageName}:${config.tag} ."
                }
            }

            stage('Push Docker Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: config.credentialsId,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        docker push ${config.imageName}:${config.tag}
                        """
                    }
                }
            }
        }
    }
}
