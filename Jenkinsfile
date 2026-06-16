pipeline {
    agent any
    
    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    
    environment {
        // Service definitions
        REGISTRY = 'localhost'
        IMAGE_PREFIX = 'chatbot'
        BUILD_TAG = "${BUILD_NUMBER}"
        GIT_COMMIT_SHORT = "${GIT_COMMIT.take(7)}"
        GIT_BRANCH_NAME = "${GIT_BRANCH.replaceAll('^origin/', '')}"
        
        // Service names and paths
        SERVICES = '''
            api-gateway:"./api-gateway (1)/api-gateway"
            chatbot-bridge:"./chatbot-bridge/chatbot-bridge"
            organization-service:"./organization-service/organization-service"
            chatbot-engine:"./chatbot-engine"
        '''
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
                script {
                    echo "========================================="
                    echo "Checking out code from Git repository"
                    echo "========================================="
                }
                checkout scm
                script {
                    echo "Git checkout complete"
                    echo "Branch: ${env.GIT_BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                    sh 'git log -1 --pretty=format:"%h - %an - %s"'
                }
            }
        }
        
        stage('Build & Test - Parallel') {
            parallel {
                stage('API Gateway') {
                    steps {
                        script {
                            dir('./api-gateway (1)/api-gateway') {
                                echo "========================================="
                                echo "Building API Gateway service"
                                echo "========================================="
                                
                                def buildCmd = './gradlew clean build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                
                                sh buildCmd
                                
                                echo "API Gateway build completed successfully"
                                sh 'ls -la build/libs/'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: './api-gateway (1)/api-gateway/build/test-results/**/*.xml', 
                                  allowEmptyResults: true,
                                  skipPublishingChecks: true
                            archiveArtifacts artifacts: './api-gateway (1)/api-gateway/build/libs/*.jar',
                                              allowEmptyArchive: true
                        }
                    }
                }
                
                stage('Chatbot Bridge') {
                    steps {
                        script {
                            dir('./chatbot-bridge/chatbot-bridge') {
                                echo "========================================="
                                echo "Building Chatbot Bridge service"
                                echo "========================================="
                                
                                def buildCmd = './gradlew clean build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                
                                sh buildCmd
                                
                                echo "Chatbot Bridge build completed successfully"
                                sh 'ls -la build/libs/'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: './chatbot-bridge/chatbot-bridge/build/test-results/**/*.xml',
                                  allowEmptyResults: true,
                                  skipPublishingChecks: true
                            archiveArtifacts artifacts: './chatbot-bridge/chatbot-bridge/build/libs/*.jar',
                                              allowEmptyArchive: true
                        }
                    }
                }
                
                stage('Organization Service') {
                    steps {
                        script {
                            dir('./organization-service/organization-service') {
                                echo "========================================="
                                echo "Building Organization Service"
                                echo "========================================="
                                
                                def buildCmd = './gradlew clean build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                
                                sh buildCmd
                                
                                echo "Organization Service build completed successfully"
                                sh 'ls -la build/libs/'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: './organization-service/organization-service/build/test-results/**/*.xml',
                                  allowEmptyResults: true,
                                  skipPublishingChecks: true
                            archiveArtifacts artifacts: './organization-service/organization-service/build/libs/*.jar',
                                              allowEmptyArchive: true
                        }
                    }
                }
                
                stage('Chatbot Engine') {
                    steps {
                        script {
                            dir('./chatbot-engine') {
                                echo "========================================="
                                echo "Building Chatbot Engine service (Kotlin DSL)"
                                echo "========================================="
                                
                                def buildCmd = './gradlew clean build'
                                if (params.SKIP_TESTS) {
                                    buildCmd += ' -x test'
                                }
                                
                                sh buildCmd
                                
                                echo "Chatbot Engine build completed successfully"
                                sh 'ls -la build/libs/'
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: './chatbot-engine/build/test-results/**/*.xml',
                                  allowEmptyResults: true,
                                  skipPublishingChecks: true
                            archiveArtifacts artifacts: './chatbot-engine/build/libs/*.jar',
                                              allowEmptyArchive: true
                        }
                    }
                }
            }
        }
        
        stage('Docker Build - Parallel') {
            parallel {
                stage('Docker: API Gateway') {
                    steps {
                        script {
                            echo "========================================="
                            echo "Building Docker image for API Gateway"
                            echo "========================================="
                            
                            sh '''
                                cd "./api-gateway (1)/api-gateway"
                                docker build -f Dockerfile \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG} \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:latest \
                                    .
                                docker images | grep ${IMAGE_PREFIX}-api-gateway
                            '''
                        }
                    }
                }
                
                stage('Docker: Chatbot Bridge') {
                    steps {
                        script {
                            echo "========================================="
                            echo "Building Docker image for Chatbot Bridge"
                            echo "========================================="
                            
                            sh '''
                                cd "./chatbot-bridge/chatbot-bridge"
                                docker build -f Dockerfile \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:${BUILD_TAG} \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-bridge:latest \
                                    .
                                docker images | grep ${IMAGE_PREFIX}-chatbot-bridge
                            '''
                        }
                    }
                }
                
                stage('Docker: Organization Service') {
                    steps {
                        script {
                            echo "========================================="
                            echo "Building Docker image for Organization Service"
                            echo "========================================="
                            
                            sh '''
                                cd "./organization-service/organization-service"
                                docker build -f Dockerfile \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:${BUILD_TAG} \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-organization-service:latest \
                                    .
                                docker images | grep ${IMAGE_PREFIX}-organization-service
                            '''
                        }
                    }
                }
                
                stage('Docker: Chatbot Engine') {
                    steps {
                        script {
                            echo "========================================="
                            echo "Building Docker image for Chatbot Engine"
                            echo "========================================="
                            
                            sh '''
                                cd "./chatbot-engine"
                                docker build -f Dockerfile \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:${BUILD_TAG} \
                                    -t ${REGISTRY}/${IMAGE_PREFIX}-chatbot-engine:latest \
                                    .
                                docker images | grep ${IMAGE_PREFIX}-chatbot-engine
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Verify Docker Images') {
            steps {
                script {
                    echo "========================================="
                    echo "Verifying all Docker images"
                    echo "========================================="
                    
                    sh '''
                        echo "Docker images with tag 'latest':"
                        docker images | grep -E "${IMAGE_PREFIX}-(api-gateway|chatbot-bridge|organization-service|chatbot-engine)" | grep latest
                        
                        echo ""
                        echo "Docker images with build tag '${BUILD_TAG}':"
                        docker images | grep -E "${IMAGE_PREFIX}-(api-gateway|chatbot-bridge|organization-service|chatbot-engine)" | grep ${BUILD_TAG}
                        
                        echo ""
                        echo "Total chatbot images: $(docker images | grep ${IMAGE_PREFIX} | wc -l)"
                    '''
                }
            }
        }
        
        stage('Build Summary') {
            steps {
                script {
                    echo "========================================="
                    echo "BUILD PIPELINE COMPLETE"
                    echo "========================================="
                    echo "Branch: ${env.GIT_BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                    echo "Build #: ${env.BUILD_NUMBER}"
                    echo "Build Time: ${new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())}"
                    echo ""
                    echo "Docker Images Built:"
                    echo "  - chatbot-api-gateway:${BUILD_TAG}"
                    echo "  - chatbot-chatbot-bridge:${BUILD_TAG}"
                    echo "  - chatbot-organization-service:${BUILD_TAG}"
                    echo "  - chatbot-chatbot-engine:${BUILD_TAG}"
                    echo ""
                    echo "All images also tagged as 'latest'"
                    echo "========================================="
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Pipeline execution finished"
                // Cleanup workspace if needed (optional)
                // cleanWs()
            }
        }
        success {
            script {
                echo "========================================="
                echo "BUILD SUCCESSFUL ✓"
                echo "========================================="
                echo "All 4 services built and Docker images created"
                echo "Next: Push images to registry or run with docker-compose"
            }
        }
        failure {
            script {
                echo "========================================="
                echo "BUILD FAILED ✗"
                echo "========================================="
                echo "Check console logs above for error details"
                echo "Common issues:"
                echo "  - Gradle build failures: Check Java code syntax"
                echo "  - Docker build failures: Check Dockerfile syntax"
                echo "  - Test failures: Check unit tests"
            }
        }
        unstable {
            script {
                echo "========================================="
                echo "BUILD UNSTABLE ⚠"
                echo "========================================="
                echo "Build completed but with warnings/test failures"
            }
        }
    }
}
