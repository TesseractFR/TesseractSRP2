// Pipeline multibranch du plugin TesseractSRP2.
//
// Seuls master et les pull requests sont construits :
// - master       : build, tests et déploiement Maven, version suffixée « -master »
// - pull request : build, tests et déploiement Maven, version suffixée « -MR<numéro> »
// Toute autre branche est ignorée sans réserver d'agent (build « Not built »).
//
// La version passe par la propriété Maven « revision » (CI-friendly versions) :
// les artefacts sont publiés en 0.1-master ou 0.1-MR42, le jar du plugin
// s'appelle plugin-0.1-master.jar et la même version apparaît dans plugin.yml.
//
// Le déploiement publie tous les modules dans le dépôt Maven DEPLOY_REPOSITORY_URL
// (par défaut le dépôt fichier /srv/artifacts/maven de la machine Jenkins).

pipeline {
    agent none

    options {
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    parameters {
        string(name: 'DEPLOY_REPOSITORY_URL', defaultValue: 'file:///srv/artifacts/maven',
               description: 'Dépôt Maven où publier les artefacts.')
        string(name: 'PACKAGES_CREDENTIALS_ID', defaultValue: 'github-packages',
               description: 'Identifiant Jenkins (utilisateur + token) pour lire TesseractCore sur GitHub Packages.')
    }

    stages {
        stage('Master ou MR') {
            when {
                beforeAgent true
                anyOf {
                    branch 'master'
                    changeRequest()
                }
            }
            agent any
            stages {
                stage('Version') {
                    steps {
                        script {
                            def baseVersion = maven('help:evaluate -N -q -DforceStdout -Dexpression=revision', true)
                                    .trim()
                                    .replace('-SNAPSHOT', '')
                            env.BUILD_VERSION = "${baseVersion}-${buildSuffix()}"
                            currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.BUILD_VERSION}"
                            echo "Version construite : ${env.BUILD_VERSION}"
                        }
                    }
                }

                stage('Build et deploy') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: params.PACKAGES_CREDENTIALS_ID,
                                                          usernameVariable: 'PACKAGES_USERNAME',
                                                          passwordVariable: 'PACKAGES_ACCESS_TOKEN')]) {
                            maven("clean deploy -Drevision=${env.BUILD_VERSION} " +
                                  "-DaltDeploymentRepository=tesseract-artifacts::${params.DEPLOY_REPOSITORY_URL}")
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                        }
                        success {
                            archiveArtifacts artifacts: 'plugin/target/plugin-*.jar', fingerprint: true
                        }
                    }
                }
            }
        }
    }
}

// Suffixe de version du build courant : « MR<numéro> » pour une pull request, « master » sinon.
def buildSuffix() {
    return env.CHANGE_ID ? "MR${env.CHANGE_ID}" : 'master'
}

// Lance Maven avec les réglages GitHub Packages du dépôt : le wrapper sous Unix,
// le Maven du PATH sous Windows (le dépôt n'a pas de mvnw.cmd).
def maven(String arguments, boolean returnStdout = false) {
    def command = "-B -s .github/workflows/maven-settings.xml ${arguments}"
    if (isUnix()) {
        return sh(script: "./mvnw ${command}", returnStdout: returnStdout)
    }
    return bat(script: "@mvn ${command}", returnStdout: returnStdout)
}
