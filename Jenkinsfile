def runCommand(String unixCommand, String windowsCommand = unixCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}

def captureCommand(String unixCommand, String windowsCommand) {
    if (isUnix()) {
        return sh(script: unixCommand, returnStdout: true).trim()
    }
    return bat(script: "@${windowsCommand}", returnStdout: true).trim()
}

pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven_3_9'
    }

    options {
        timeout(time: 40, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    parameters {
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true, description: 'Run browser journeys using the Compose Selenium service.')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'On main only, publish the tested image to Docker Hub.')
        string(name: 'APP_HOST_PORT', defaultValue: '8080', description: 'Host loopback port for local deployment; valid range 1024–65535.')
    }

    environment {
        IMAGE_NAME = 'nutrisync'
        IMAGE_TAG = 'unset'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = captureCommand('git rev-parse --short=12 HEAD', 'git rev-parse --short=12 HEAD')
                }
            }
        }

        stage('Validate deployment parameters') {
            steps {
                script {
                    if (!(params.APP_HOST_PORT ==~ /[0-9]{1,5}/)) {
                        error('APP_HOST_PORT must be a numeric TCP port between 1024 and 65535.')
                    }
                    int configuredPort = params.APP_HOST_PORT.toInteger()
                    if (configuredPort < 1024 || configuredPort > 65535) {
                        error('APP_HOST_PORT must be between 1024 and 65535.')
                    }
                    if (params.RUN_SELENIUM && configuredPort == 18080) {
                        error('APP_HOST_PORT 18080 is reserved for the isolated Selenium stack when RUN_SELENIUM is enabled.')
                    }
                }
            }
        }

        stage('Build and unit/integration tests') {
            steps {
                script {
                    runCommand('java -version')
                    runCommand('mvn -version')
                    runCommand('mvn -B clean verify')
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build container image') {
            steps {
                script {
                    runCommand(
                        'docker build --pull -t ${IMAGE_NAME}:${IMAGE_TAG} .',
                        'docker build --pull -t %IMAGE_NAME%:%IMAGE_TAG% .'
                    )
                    runCommand(
                        'docker image inspect ${IMAGE_NAME}:${IMAGE_TAG} --format "{{.Id}}"',
                        'docker image inspect %IMAGE_NAME%:%IMAGE_TAG% --format "{{.Id}}"'
                    )
                }
            }
        }

        stage('Selenium end-to-end tests') {
            when { expression { return params.RUN_SELENIUM } }
            steps {
                script {
                    withEnv([
                        "POSTGRES_PASSWORD=ci-only-${env.BUILD_NUMBER}-temporary",
                        "POSTGRES_DB=nutrisync",
                        "POSTGRES_USER=nutrisync",
                        "SPRING_PROFILES_ACTIVE=demo",
                        "APP_SEED_DEMO_DATA=true",
                        "APP_BOOTSTRAP_STAFF_ENABLED=false",
                        "APP_HOST_PORT=18080",
                        "COMPOSE_PROJECT_NAME=nutrisync-ci-${env.BUILD_NUMBER}"
                    ]) {
                        runCommand('docker compose --profile e2e up -d --build --wait')
                        runCommand('mvn -B -Pselenium-e2e verify -De2e.enabled=true -De2e.baseUrl=http://127.0.0.1:18080 -De2e.remoteUrl=http://localhost:4444')
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/selenium-failures/**/*.png'
                }
            }
        }

        stage('Publish image to Docker Hub') {
            when {
                allOf {
                    branch 'main'
                    expression { return params.PUSH_IMAGE }
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nutrisync-dockerhub', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    script {
                        runCommand('''
                      set +x
                      echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin
                      docker tag "$IMAGE_NAME:$IMAGE_TAG" "$DOCKERHUB_USER/nutrisync:$IMAGE_TAG"
                      docker push "$DOCKERHUB_USER/nutrisync:$IMAGE_TAG"
                      docker logout
                    ''',
                    '''
                      @echo off
                      echo %DOCKERHUB_TOKEN%| docker login --username "%DOCKERHUB_USER%" --password-stdin
                      if errorlevel 1 exit /b 1
                      docker tag "%IMAGE_NAME%:%IMAGE_TAG%" "%DOCKERHUB_USER%/nutrisync:%IMAGE_TAG%"
                      if errorlevel 1 exit /b 1
                      docker push "%DOCKERHUB_USER%/nutrisync:%IMAGE_TAG%"
                      if errorlevel 1 exit /b 1
                      docker logout
                    ''')
                    }
                }
            }
        }

        stage('Deploy on main') {
            when { branch 'main' }
            steps {
                script {
                    // Tear down only this build's disposable E2E stack, never the persistent deploy stack.
                    withEnv([
                        "POSTGRES_PASSWORD=ci-only-${env.BUILD_NUMBER}-temporary",
                        "POSTGRES_DB=nutrisync",
                        "POSTGRES_USER=nutrisync",
                        "SPRING_PROFILES_ACTIVE=demo",
                        "APP_SEED_DEMO_DATA=true",
                        "APP_BOOTSTRAP_STAFF_ENABLED=false",
                        "APP_HOST_PORT=18080",
                        "COMPOSE_PROJECT_NAME=nutrisync-ci-${env.BUILD_NUMBER}"
                    ]) {
                        runCommand(
                            'docker compose --profile e2e down --volumes --remove-orphans || true',
                            'docker compose --profile e2e down --volumes --remove-orphans'
                        )
                    }

                    def deploymentCredentials = [
                        string(credentialsId: 'nutrisync-postgres-password', variable: 'POSTGRES_PASSWORD'),
                        string(credentialsId: 'nutrisync-bootstrap-staff-name', variable: 'APP_BOOTSTRAP_STAFF_NAME'),
                        string(credentialsId: 'nutrisync-bootstrap-staff-email', variable: 'APP_BOOTSTRAP_STAFF_EMAIL'),
                        string(credentialsId: 'nutrisync-bootstrap-staff-password', variable: 'APP_BOOTSTRAP_STAFF_PASSWORD')
                    ]

                    if (params.PUSH_IMAGE) {
                        withCredentials(deploymentCredentials + [
                            usernamePassword(credentialsId: 'nutrisync-dockerhub', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')
                        ]) {
                            withEnv([
                                "COMPOSE_PROJECT_NAME=nutrisync",
                                "POSTGRES_DB=nutrisync",
                                "POSTGRES_USER=nutrisync",
                                "SPRING_PROFILES_ACTIVE=prod",
                                "APP_SEED_DEMO_DATA=false",
                                "APP_BOOTSTRAP_STAFF_ENABLED=true",
                                "APP_HOST_PORT=${params.APP_HOST_PORT}",
                                "NUTRISYNC_IMAGE=${env.DOCKERHUB_USER}/nutrisync:${env.IMAGE_TAG}"
                            ]) {
                                runCommand('''
                                  set +x
                                  echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin
                                  docker compose pull app
                                  docker compose up -d --no-build --wait
                                  curl --fail --silent --show-error --retry 12 --retry-delay 5 "http://127.0.0.1:$APP_HOST_PORT/actuator/health"
                                  docker logout
                                ''',
                                '''
                                  @echo off
                                  echo %DOCKERHUB_TOKEN%| docker login --username "%DOCKERHUB_USER%" --password-stdin
                                  if errorlevel 1 exit /b 1
                                  docker compose pull app
                                  if errorlevel 1 exit /b 1
                                  docker compose up -d --no-build --wait
                                  if errorlevel 1 exit /b 1
                                  curl.exe --fail --silent --show-error --retry 12 --retry-delay 5 "http://127.0.0.1:%APP_HOST_PORT%/actuator/health"
                                  if errorlevel 1 exit /b 1
                                  docker logout
                                ''')
                            }
                        }
                    } else {
                        withCredentials(deploymentCredentials) {
                            runCommand(
                                'docker tag "$IMAGE_NAME:$IMAGE_TAG" "nutrisync-app:$IMAGE_TAG"',
                                'docker tag "%IMAGE_NAME%:%IMAGE_TAG%" "nutrisync-app:%IMAGE_TAG%"'
                            )
                            withEnv([
                                "COMPOSE_PROJECT_NAME=nutrisync",
                                "POSTGRES_DB=nutrisync",
                                "POSTGRES_USER=nutrisync",
                                "SPRING_PROFILES_ACTIVE=prod",
                                "APP_SEED_DEMO_DATA=false",
                                "APP_BOOTSTRAP_STAFF_ENABLED=true",
                                "APP_HOST_PORT=${params.APP_HOST_PORT}",
                                "NUTRISYNC_IMAGE=nutrisync-app:${env.IMAGE_TAG}"
                            ]) {
                                runCommand('docker compose up -d --no-build --wait')
                                runCommand(
                                    'curl --fail --silent --show-error --retry 12 --retry-delay 5 "http://127.0.0.1:$APP_HOST_PORT/actuator/health"',
                                    'curl.exe --fail --silent --show-error --retry 12 --retry-delay 5 "http://127.0.0.1:%APP_HOST_PORT%/actuator/health"'
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                if (env.BRANCH_NAME != 'main' || currentBuild.currentResult != 'SUCCESS') {
                    withEnv([
                        "POSTGRES_PASSWORD=ci-only-${env.BUILD_NUMBER}-temporary",
                        "POSTGRES_DB=nutrisync",
                        "POSTGRES_USER=nutrisync",
                        "APP_HOST_PORT=18080",
                        "COMPOSE_PROJECT_NAME=nutrisync-ci-${env.BUILD_NUMBER}"
                    ]) {
                        runCommand(
                            'docker compose --profile e2e down --volumes --remove-orphans || true',
                            'docker compose --profile e2e down --volumes --remove-orphans'
                        )
                    }
                }
            }
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/nutrisync.jar,target/selenium-failures/**/*.png'
        }
    }
}
