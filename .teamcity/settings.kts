import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

version = "2020.2"

project {
    vcsRoot(ReactTeamCityKotlinDemoRepo)
    buildType(Build)
}


object ReactTeamCityKotlinDemoRepo : GitVcsRoot({
    name = "${DslContext.getParameter("repoName")} Repo"
    url = DslContext.getParameter("fetchUrl")
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
})

object Build : BuildType({
    name = "Build & test"

    artifactRules = "build"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(ReactTeamCityKotlinDemoRepo)
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
