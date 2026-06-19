// pipeline {
//     agent any

//     options {
//         timeout(time: 30, unit: 'MINUTES')
//         buildDiscarder(logRotator(numToKeepStr: '10'))
//         disableConcurrentBuilds()
//     }

//     environment {
//         REGISTRY = 'localhost'
//         IMAGE_PREFIX = 'chatbot'
//         BUILD_TAG = "${BUILD_NUMBER}"
//     }

//     parameters {
//         booleanParam(
//             name: 'SKIP_TESTS',
//             defaultValue: false,
//             description: 'Skip running unit tests (faster builds for testing pipeline)'
//         )
//         booleanParam(
//             name: 'CLEAN_BUILD',
//             defaultValue: false,
//             description: 'Perform clean build (removes build artifacts)'
//         )
//     }

//     stages {
//         stage('Checkout') {
//             steps {
//                 echo 'Checking out code from Git repository...'
//                 checkout scm
//                 script {
//                     env.GIT_COMMIT_SHORT = env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'unknown'
//                     env.GIT_BRANCH_NAME = env.GIT_BRANCH ? env.GIT_BRANCH.replaceAll('^origin/', '') : env.BRANCH_NAME
//                     echo "Branch: ${env.GIT_BRANCH_NAME}"
//                     echo "Commit: ${env.GIT_COMMIT_SHORT}"
//                     sh 'git log -1 --pretty=format:"%h - %an - %s"'
//                 }
//             }
//         }

//         stage('Prepare Gradle Wrappers') {
//             steps {
//                 sh '''
//                     chmod +x "./api-gateway/api-gateway/gradlew"
//                     chmod +x "./chatbot-bridge/chatbot-bridge/gradlew"
//                     chmod +x "./organization-service/organization-service/gradlew"
//                     chmod +x "./chatbot-engine/gradlew"
//                 '''
//             }
//         }

//         stage('Build & Test - Parallel') {
//             parallel {
//                 stage('API Gateway') {
//                     steps {
//                         script {
//                             dir('./api-gateway/api-gateway') {
//                                 echo 'Building API Gateway...'
//                                 def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
//                                 if (params.SKIP_TESTS) {
//                                     buildCmd += ' -x test'
//                                 }
//                                 sh buildCmd
//                                 sh 'ls -la build/libs/ || true'
//                             }
//                         }
//                     }
//                     post {
//                         always {
//                             junit testResults: 'api-gateway/api-gateway/build/test-results/test/*.xml',
//                                   allowEmptyResults: true
//                             archiveArtifacts artifacts: 'api-gateway/api-gateway/build/libs/*.jar',
//                                               allowEmptyArchive: true
//                         }
//                     }
//                 }

//                 stage('Chatbot Bridge') {
//                     steps {
//                         script {
//                             dir('./chatbot-bridge/chatbot-bridge') {
//                                 echo 'Building Chatbot Bridge...'
//                                 def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
//                                 if (params.SKIP_TESTS) {
//                                     buildCmd += ' -x test'
//                                 }
//                                 sh buildCmd
//                                 sh 'ls -la build/libs/ || true'
//                             }
//                         }
//                     }
//                     post {
//                         always {
//                             junit testResults: 'chatbot-bridge/chatbot-bridge/build/test-results/test/*.xml',
//                                   allowEmptyResults: true
//                             archiveArtifacts artifacts: 'chatbot-bridge/chatbot-bridge/build/libs/*.jar',
//                                               allowEmptyArchive: true
//                         }
//                     }
//                 }

//                 stage('Organization Service') {
//                     steps {
//                         script {
//                             dir('./organization-service/organization-service') {
//                                 echo 'Building Organization Service...'
//                                 def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
//                                 if (params.SKIP_TESTS) {
//                                     buildCmd += ' -x test'
//                                 }
//                                 sh buildCmd
//                                 sh 'ls -la build/libs/ || true'
//                             }
//                         }
//                     }
//                     post {
//                         always {
//                             junit testResults: 'organization-service/organization-service/build/test-results/test/*.xml',
//                                   allowEmptyResults: true
//                             archiveArtifacts artifacts: 'organization-service/organization-service/build/libs/*.jar',
//                                               allowEmptyArchive: true
//                         }
//                     }
//                 }

//                 stage('Chatbot Engine') {
//                     steps {
//                         script {
//                             dir('./chatbot-engine') {
//                                 echo 'Building Chatbot Engine...'
//                                 def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
//                                 if (params.SKIP_TESTS) {
//                                     buildCmd += ' -x test'
//                                 }
//                                 sh buildCmd
//                                 sh 'ls -la build/libs/ || true'
//                             }
//                         }
//                     }
//                     post {
//                         always {
//                             junit testResults: 'chatbot-engine/build/test-results/test/*.xml',
//                                   allowEmptyResults: true
//                             archiveArtifacts artifacts: 'chatbot-engine/build/libs/*.jar',
//                                               allowEmptyArchive: true
//                         }
//                     }
//                 }
//             }
//         }

//         stage('Docker Build - Parallel') {
//             parallel {
//                 stage('Docker: API Gateway') {
//                     steps {
//                         sh '''
//                             cd "./api-gateway/api-gateway"
//                             docker build -f Dockerfile \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG} \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:latest \
//                                 .
//                         '''
//                     }
//                 }

//                 stage('Docker: Chatbot Bridge') {
//                     steps {
//                         sh '''
//                             cd "./chatbot-bridge/chatbot-bridge"
//                             docker build -f Dockerfile \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:${BUILD_TAG} \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:latest \
//                                 .
//                         '''
//                     }
//                 }

//                 stage('Docker: Organization Service') {
//                     steps {
//                         sh '''
//                             cd "./organization-service/organization-service"
//                             docker build -f Dockerfile \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:${BUILD_TAG} \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:latest \
//                                 .
//                         '''
//                     }
//                 }

//                 stage('Docker: Chatbot Engine') {
//                     steps {
//                         sh '''
//                             cd "./chatbot-engine"
//                             docker build -f Dockerfile \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:${BUILD_TAG} \
//                                 -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:latest \
//                                 .
//                         '''
//                     }
//                 }
//             }
//         }

//         stage('Verify Docker Images') {
//             steps {
//                 sh '''
//                     echo "Built chatbot images:"
//                     docker images | grep "${IMAGE_PREFIX}-" || echo "No chatbot images found"
//                 '''
//             }
//         }

//         stage('Build Summary') {
//             steps {
//                 script {
//                     echo "BUILD PIPELINE COMPLETE"
//                     echo "Branch: ${env.GIT_BRANCH_NAME ?: 'unknown'}"
//                     echo "Commit: ${env.GIT_COMMIT_SHORT ?: 'unknown'}"
//                     echo "Build #: ${env.BUILD_NUMBER}"
//                     echo "Images tagged: ${BUILD_TAG} and latest"
//                 }
//             }
//         }
//     }

//     post {
//         success {
//             echo 'BUILD SUCCESSFUL - all 4 services built and Docker images created'
//         }
//         failure {
//             echo 'BUILD FAILED - check console output above'
//         }
//     }
// }
pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        REGISTRY = 'localhost'
        IMAGE_PREFIX = 'chatbot'
        BUILD_TAG = "${BUILD_NUMBER}"
    }

    parameters {
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip running unit tests (faster builds for testing pipeline)'
        )
        booleanParam(
            name: 'CLEAN_BUILD',
            defaultValue: false,
            description: 'Perform clean build (removes build artifacts)'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git repository...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'unknown'
                    env.GIT_BRANCH_NAME = env.GIT_BRANCH ? env.GIT_BRANCH.replaceAll('^origin/', '') : env.BRANCH_NAME
                    echo "Branch: ${env.GIT_BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                    sh 'git log -1 --pretty=format:"%h - %an - %s"'
                }
            }
        }

        stage('Prepare Gradle Wrappers') {
            steps {
                sh '''
                    chmod +x "./api-gateway/api-gateway/gradlew"
                    chmod +x "./chatbot-bridge/chatbot-bridge/gradlew"
                    chmod +x "./organization-service/organization-service/gradlew"
                    chmod +x "./chatbot-engine/gradlew"
                '''
            }
        }

        stage('Build & Test - Parallel') {
            parallel {
                stage('API Gateway') {
                    steps {
                        script {
                            dir('./api-gateway/api-gateway') {
                                echo 'Building API Gateway...'
                                def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                sh buildCmd
                                sh 'ls -la build/libs/ || true'
                            }
                        }
                    }
                    post {
                        always {
                            echo 'API Gateway build stage completed'
                        }
                    }
                }

                stage('Chatbot Bridge') {
                    steps {
                        script {
                            dir('./chatbot-bridge/chatbot-bridge') {
                                echo 'Building Chatbot Bridge...'
                                def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                sh buildCmd
                                sh 'ls -la build/libs/ || true'
                            }
                        }
                    }
                    post {
                        always {
                            echo 'Chatbot Bridge build stage completed'
                        }
                    }
                }

                stage('Organization Service') {
                    steps {
                        script {
                            dir('./organization-service/organization-service') {
                                echo 'Building Organization Service...'
                                def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                sh buildCmd
                                sh 'ls -la build/libs/ || true'
                            }
                        }
                    }
                    post {
                        always {
                            echo 'Organization Service build stage completed'
                        }
                    }
                }

                stage('Chatbot Engine') {
                    steps {
                        script {
                            dir('./chatbot-engine') {
                                echo 'Building Chatbot Engine...'
                                def buildCmd = params.CLEAN_BUILD ? 'bash ./gradlew clean build' : 'bash ./gradlew build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                sh buildCmd
                                sh 'ls -la build/libs/ || true'
                            }
                        }
                    }
                    post {
                        always {
                            echo 'Chatbot Engine build stage completed'
                        }
                    }
                }
            }
        }

        stage('Docker Build - Parallel') {
            parallel {
                stage('Docker: API Gateway') {
                    steps {
                        sh '''
                            cd "./api-gateway/api-gateway"
                            docker build -f Dockerfile \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG} \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:latest \
                                .
                        '''
                    }
                }

                stage('Docker: Chatbot Bridge') {
                    steps {
                        sh '''
                            cd "./chatbot-bridge/chatbot-bridge"
                            docker build -f Dockerfile \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:${BUILD_TAG} \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:latest \
                                .
                        '''
                    }
                }

                stage('Docker: Organization Service') {
                    steps {
                        sh '''
                            cd "./organization-service/organization-service"
                            docker build -f Dockerfile \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:${BUILD_TAG} \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:latest \
                                .
                        '''
                    }
                }

                stage('Docker: Chatbot Engine') {
                    steps {
                        sh '''
                            cd "./chatbot-engine"
                            docker build -f Dockerfile \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:${BUILD_TAG} \
                                -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:latest \
                                .
                        '''
                    }
                }
            }
        }

        stage('Verify Docker Images') {
            steps {
                sh '''
                    echo "Built chatbot images:"
                    docker images | grep "${IMAGE_PREFIX}-" || echo "No chatbot images found"
                '''
            }
        }

        stage('Build Summary') {
            steps {
                script {
                    echo "BUILD PIPELINE COMPLETE"
                    echo "Branch: ${env.GIT_BRANCH_NAME ?: 'unknown'}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT ?: 'unknown'}"
                    echo "Build #: ${env.BUILD_NUMBER}"
                    echo "Images tagged: ${BUILD_TAG} and latest"
                }
            }
        }
    }

    post {
        success {
            echo 'BUILD SUCCESSFUL - all 4 services built and Docker images created'
        }
        failure {
            echo 'BUILD FAILED - check console output above'
        }
    }
}

//testing it 