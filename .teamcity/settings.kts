import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

version = "2020.2"

project {
    buildType(Build)
}

object Build : BuildType({
    name = "Build & test"

    artifactRules = "build"
    publishArtifacts = PublishMode.SUCCESSFUL

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
