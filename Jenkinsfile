pipeline {
    agent any

    environment {
        // Environment variables
        BACKEND_DIR = 'backend'
        FRONTEND_DIR = 'frontend'
        DOCKER_REGISTRY = 'your-registry.com'
        APP_NAME = 'buzzlink'

        // Credentials (would be stored in Jenkins)
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials')
        CLERK_CREDENTIALS = credentials('clerk-api-keys')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from Git...'
                checkout scm

                // Display build information
                sh 'git rev-parse --short HEAD > .git/commit-hash'
                script {
                    env.GIT_COMMIT_HASH = readFile('.git/commit-hash').trim()
                }
                echo "Building commit: ${env.GIT_COMMIT_HASH}"
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot backend...'
                dir(BACKEND_DIR) {
                    // Clean and build with Gradle
                    sh './gradlew clean build -x test'

                    echo 'Backend build successful!'
                }
            }
        }

        stage('Test Backend') {
            steps {
                echo 'Running backend unit tests...'
                dir(BACKEND_DIR) {
                    sh './gradlew test'

                    // Publish test results
                    junit '**/build/test-results/test/*.xml'

                    // Publish code coverage (if JaCoCo is configured)
                    // jacoco execPattern: '**/build/jacoco/*.exec'
                }
            }
        }

        stage('Backend Code Quality') {
            steps {
                echo 'Running SonarQube analysis...'
                dir(BACKEND_DIR) {
                    // In production, would run SonarQube scanner
                    // sh './gradlew sonarqube'
                    echo 'Code quality checks passed (stub)'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo 'Building Next.js frontend...'
                dir(FRONTEND_DIR) {
                    // Install dependencies
                    sh 'npm ci'

                    // Run linting
                    sh 'npm run lint'

                    // Build production bundle
                    sh 'npm run build'

                    echo 'Frontend build successful!'
                }
            }
        }

        stage('Run Frontend Tests') {
            steps {
                echo 'Running frontend tests...'
                dir(FRONTEND_DIR) {
                    // Would run Jest/Vitest tests
                    // sh 'npm test'
                    echo 'Frontend tests passed (stub)'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Build Backend Image') {
                    steps {
                        echo 'Building backend Docker image...'
                        dir(BACKEND_DIR) {
                            script {
                                // Build Docker image
                                def backendImage = docker.build(
                                    "${DOCKER_REGISTRY}/${APP_NAME}-backend:${env.GIT_COMMIT_HASH}"
                                )

                                // Tag as latest
                                backendImage.tag('latest')

                                echo "Backend image built: ${DOCKER_REGISTRY}/${APP_NAME}-backend:${env.GIT_COMMIT_HASH}"
                            }
                        }
                    }
                }

                stage('Build Frontend Image') {
                    steps {
                        echo 'Building frontend Docker image...'
                        dir(FRONTEND_DIR) {
                            script {
                                // Build Docker image
                                def frontendImage = docker.build(
                                    "${DOCKER_REGISTRY}/${APP_NAME}-frontend:${env.GIT_COMMIT_HASH}"
                                )

                                // Tag as latest
                                frontendImage.tag('latest')

                                echo "Frontend image built: ${DOCKER_REGISTRY}/${APP_NAME}-frontend:${env.GIT_COMMIT_HASH}"
                            }
                        }
                    }
                }
            }
        }

        stage('Security Scanning') {
            parallel {
                stage('Dependency Check') {
                    steps {
                        echo 'Scanning for vulnerable dependencies...'
                        // Would use OWASP Dependency-Check or Snyk
                        echo 'Dependency scan completed (stub)'
                    }
                }

                stage('Container Scan') {
                    steps {
                        echo 'Scanning Docker images for vulnerabilities...'
                        // Would use Trivy, Clair, or Anchore
                        echo 'Container scan completed (stub)'
                    }
                }
            }
        }

        stage('Push Docker Images') {
            when {
                branch 'main'
            }
            steps {
                echo 'Pushing Docker images to registry...'
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                        // Push backend
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}-backend:${env.GIT_COMMIT_HASH}").push()
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}-backend:latest").push()

                        // Push frontend
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}-frontend:${env.GIT_COMMIT_HASH}").push()
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}-frontend:latest").push()
                    }
                }
                echo 'Images pushed successfully!'
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'

                // Would deploy to Kubernetes, ECS, or other platform
                // Example Kubernetes deployment:
                // sh 'kubectl set image deployment/buzzlink-backend backend=${DOCKER_REGISTRY}/${APP_NAME}-backend:${env.GIT_COMMIT_HASH} -n staging'
                // sh 'kubectl set image deployment/buzzlink-frontend frontend=${DOCKER_REGISTRY}/${APP_NAME}-frontend:${env.GIT_COMMIT_HASH} -n staging'

                echo 'Deployment to staging initiated (stub)'
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment...'

                // Require manual approval for production
                input message: 'Deploy to production?', ok: 'Deploy'

                echo 'Deploying backend...'
                // sh 'kubectl set image deployment/buzzlink-backend backend=${DOCKER_REGISTRY}/${APP_NAME}-backend:${env.GIT_COMMIT_HASH} -n production'

                echo 'Deploying frontend...'
                // sh 'kubectl set image deployment/buzzlink-frontend frontend=${DOCKER_REGISTRY}/${APP_NAME}-frontend:${env.GIT_COMMIT_HASH} -n production'

                // Wait for rollout
                // sh 'kubectl rollout status deployment/buzzlink-backend -n production'
                // sh 'kubectl rollout status deployment/buzzlink-frontend -n production'

                echo 'Production deployment completed (stub)'
            }
        }

        stage('Smoke Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Running smoke tests...'

                // Health check endpoints
                // sh 'curl -f http://staging.buzzlink.com/actuator/health'
                // sh 'curl -f http://staging.buzzlink.com/'

                echo 'Smoke tests passed (stub)'
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }

        success {
            echo 'Pipeline completed successfully!'

            // Send success notification
            // slackSend(
            //     color: 'good',
            //     message: "Build #${env.BUILD_NUMBER} succeeded for ${env.JOB_NAME}"
            // )

            // emailext(
            //     subject: "✓ Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Build completed successfully. Commit: ${env.GIT_COMMIT_HASH}",
            //     to: 'team@buzzlink.com'
            // )
        }

        failure {
            echo 'Pipeline failed!'

            // Send failure notification
            // slackSend(
            //     color: 'danger',
            //     message: "Build #${env.BUILD_NUMBER} failed for ${env.JOB_NAME}"
            // )

            // emailext(
            //     subject: "✗ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Build failed. Please check Jenkins for details.",
            //     to: 'team@buzzlink.com'
            // )
        }

        unstable {
            echo 'Pipeline is unstable!'

            // Send warning notification
            // slackSend(
            //     color: 'warning',
            //     message: "Build #${env.BUILD_NUMBER} is unstable for ${env.JOB_NAME}"
            // )
        }
    }
}
