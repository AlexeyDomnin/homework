#!/usr/bin/env groovy
pipeline {
    agent any
    stages {
        stage('Gradle Build') {
            steps {
                script {
                    try {
                        sh './gradlew build -x test'
                    } catch (Throwable ex) {
                        ex.printStackTrace()
                    }
                }
            }
        }

        stage('Static code analysis') {
            when { expression { env.CHANGE_ID } }
            steps {
                //sh './gradlew check -x test'

                step([
                        $class: 'ViolationsToGitHubRecorder',
                        config: [
                                gitHubUrl                             : 'https://api.github.com/',
                                repositoryOwner                       : 'sberbankjavaschool',
                                repositoryName                        : 'homework',
                                pullRequestId                         : "$CHANGE_ID",

                                credentialsId                         : 'jsj-github',

                                createCommentWithAllSingleFileComments: false,
                                createSingleFileComments              : true,
                                commentOnlyChangedContent             : true,
                                minSeverity                           : 'INFO',
                                maxNumberOfViolations                 : 99999,
                                keepOldComments                       : false,

                                commentTemplate                       : """
    **Reporter**: {{violation.reporter}}{{#violation.rule}}  **Rule**: {{violation.rule}}{{/violation.rule}} **Severity**: {{violation.severity}}
{{violation.message}}""",

                                violationConfigs                      : [
                                        [pattern: '.*/reports/checkstyle/.*\\.xml$', parser: 'CHECKSTYLE', reporter: 'Checkstyle']
                                ]
                        ]
                ])
            }

        }
        stage('Gradle Test') {
            steps {
                script {
                    sh './gradlew test'
                }
            }
        }
        stage('Gradle Publish') {
            when { branch 'master' }
            steps {
                script {
                    sh './gradlew publish'
                }
            }
        }

        stage('Gradle Clean') {
            steps {
                script {
                    sh './gradlew clean'
                }
            }
        }
    }
}