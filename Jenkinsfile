// Pipeline multibranch du plugin TesseractSRP2.
//
// - master          : build, tests, archivage et déploiement, version suffixée « -master »
// - pull request    : build, tests, archivage et déploiement, version suffixée « -MR<numéro> »
// - autres branches : build et tests seulement, version du pom inchangée
//
// La version passe par la propriété Maven « revision » (CI-friendly versions) :
// le jar produit s'appelle donc plugin-0.1-master.jar ou plugin-0.1-MR42.jar,
// et la même version apparaît dans plugin.yml.
//
// Le déploiement copie le jar dans un dossier de l'agent Jenkins, réglé par les
// paramètres DEPLOY_DIR_MASTER et DEPLOY_DIR_MR. Un paramètre vide désactive le
// déploiement correspondant (le jar reste disponible dans les artefacts du build).

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    parameters {
        string(name: 'DEPLOY_DIR_MASTER', defaultValue: '',
               description: 'Dossier où copier le jar de master (ex. dossier plugins du serveur). Vide : pas de déploiement.')
        string(name: 'DEPLOY_DIR_MR', defaultValue: '',
               description: 'Dossier où copier le jar des pull requests (ex. serveur de test). Vide : pas de déploiement.')
        string(name: 'PACKAGES_CREDENTIALS_ID', defaultValue: 'github-packages',
               description: 'Identifiant Jenkins (utilisateur + token) pour lire TesseractCore sur GitHub Packages.')
    }

    stages {
        stage('Version') {
            steps {
                script {
                    def baseVersion = maven('help:evaluate -N -q -DforceStdout -Dexpression=revision', true)
                            .trim()
                            .replace('-SNAPSHOT', '')
                    env.BUILD_SUFFIX = buildSuffix()
                    env.BUILD_VERSION = env.BUILD_SUFFIX ? "${baseVersion}-${env.BUILD_SUFFIX}" : ''
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.BUILD_VERSION ?: baseVersion}"
                    echo "Version construite : ${env.BUILD_VERSION ?: 'version du pom'}"
                }
            }
        }

        stage('Build') {
            steps {
                withCredentials([usernamePassword(credentialsId: params.PACKAGES_CREDENTIALS_ID,
                                                  usernameVariable: 'PACKAGES_USERNAME',
                                                  passwordVariable: 'PACKAGES_ACCESS_TOKEN')]) {
                    script {
                        def revision = env.BUILD_VERSION ? "-Drevision=${env.BUILD_VERSION}" : ''
                        maven("clean verify ${revision}")
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Archive') {
            when { expression { env.BUILD_SUFFIX } }
            steps {
                archiveArtifacts artifacts: 'plugin/target/plugin-*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            when { expression { env.BUILD_SUFFIX && deployDirectory() } }
            steps {
                script {
                    def target = deployDirectory()
                    def jar = "plugin/target/plugin-${env.BUILD_VERSION}.jar"
                    // On retire le jar précédent du même canal, sinon Paper chargerait les deux.
                    def previous = env.CHANGE_ID ? 'plugin-*-MR*.jar' : 'plugin-*-master.jar'
                    if (isUnix()) {
                        sh """
                            mkdir -p '${target}'
                            rm -f '${target}'/${previous}
                            cp '${jar}' '${target}/'
                        """
                    } else {
                        bat """
                            if not exist "${target}" mkdir "${target}"
                            del /q "${target}\\${previous}" 2>nul
                            copy /y "${jar.replace('/', '\\')}" "${target}\\"
                        """
                    }
                    echo "Jar déployé dans ${target}"
                }
            }
        }
    }
}

// Suffixe de version du build courant, vide pour une branche ni master ni pull request.
def buildSuffix() {
    if (env.CHANGE_ID) {
        return "MR${env.CHANGE_ID}"
    }
    if (env.BRANCH_NAME == 'master') {
        return 'master'
    }
    return ''
}

// Dossier de déploiement du canal courant, vide si le déploiement est désactivé.
def deployDirectory() {
    return (env.CHANGE_ID ? params.DEPLOY_DIR_MR : params.DEPLOY_DIR_MASTER)?.trim() ?: ''
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
