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

            stage('Run Docker Container') {
                steps {
                    sh """
                    docker rm -f demo-container || true
                    docker run -d --name demo-container -p 8081:80 ${config.imageName}:${config.tag}
                    sleep 5
                    docker ps
                    docker rm -f demo-container
                    """
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

