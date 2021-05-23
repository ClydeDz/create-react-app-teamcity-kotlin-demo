import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger

version = "2020.2"

project {
    buildType(Build)
    buildType(Deploy)
}

object Build : BuildType({
    name = "Build & test"

    artifactRules = "build"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(DslContext.settingsRoot)
    }
    
    steps {
        script {
            name = "Install NPM packages"
            scriptContent = "npm ci"
        }
        script {
            name = "Build project"
            scriptContent = "npm run build"
        }
        script {
            name = "Run unit tests"
            scriptContent = "set CI=true&&npm test"
        }
    }

    triggers {
        vcs {
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = false
        }
    }
})

object Deploy : BuildType({
    name = "Deploy"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    params {
        password("service-principal-id", "credentialsJSON:4baaaddb-aef6-478f-97c2-19b06d9677c4")
        password("service-principal-password", "credentialsJSON:e218e9a3-4fa6-4294-acb6-7c949043f7a0")
        password("azure-tenant", "credentialsJSON:77e469c2-b099-464f-ad03-8f7c72d5723d")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "Check installed Azure CLI version"
            scriptContent = "az --version"
        }
        script {
            name = "Azure login"
            scriptContent = "az login --service-principal -u %service-principal-id% -p %service-principal-password% --tenant %azure-tenant%"
        }
        script {
            name = "Update blob storage"
            scriptContent = "az storage blob upload-batch --account-name craazstoragedemo48765 -d ${'$'}web -s ./target --auth-mode login"
        }
        script {
            name = "Azure logout"
            scriptContent = "az logout"
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${Build.id}"
            successfulOnly = true
            branchFilter = "+:main"
        }
    }

    dependencies {
        dependency(Build) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
                onDependencyCancel = FailureAction.CANCEL
            }

            artifacts {
                cleanDestination = true
                artifactRules = "**/*.*=>target"
            }
        }
    }
})