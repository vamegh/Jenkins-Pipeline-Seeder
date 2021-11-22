/*
 * Author: vhedayati
 */

def manage(String config){
    def basepath = env.WORKSPACE
    def configFile = "${basepath}/${config}"

    def jsonConfig = jsonConfigParse(readFile(configFile))
    echo "Configuration Data: ${jsonConfig.toString()}"
    def name = jsonConfig.name

    echo "Creating Job Folder: ${name}"
    jobDsl scriptText:  """folder('${name}') """

    try {
        createPipelineJob(basepath, jsonConfig)
    } catch (error) {
        echo "Unable to Create Pipeline Job(s)"
        echo error.message
    }
}

def createPipelineJob(basepath, jsonConfig) {
    echo "Managing codebases and jobs"
    def name = jsonConfig.name
    def gitUrl = jsonConfig.git_url
    def gitExtraLib = jsonConfig.git_extra_lib
    def gitCredentials = jsonConfig.git_credentials
    def branch = jsonConfig.git_branch
    try {
        def dm = load("${basepath}/deploy/main.groovy")
        def deploymentList = jsonConfig.deployments
        for (int i=0; i < deploymentList.size(); i++) {
            def config = deploymentList[i]
            def codebase = config.git_repo
            if (!codebase || codebase == null) {
                codebase = config.codebase
                if (!codebase || codebase == null) {
                    notify.failure(false, "git_repo or codebase must be specified - git_repo always takes precedence")
                }
            }
            def gitCreds = config.git_credentials
            def gitLib = config.git_extra_lib
            def gitBranch = config.git_branch
            def buildList = config.paths
            def environments = config.environments
            if (! gitCreds || gitCreds == null ) {
                gitCreds = gitCredentials
            }
            if (! gitLib || gitLib == null ) {
                gitLib = gitExtraLib
            }

            if (!gitBranch || gitBranch == null) {
                gitBranch = branch
                if (!gitBranch || gitBranch == null) {
                    gitBranch = 'master'
                }
            }
            echo "Creating Pipeline Job for : ${name} :: Codebase: $codebase"
            def codebasePath = "${name}/${codebase}"
            echo "Creating Codebase Path: ${codebasePath}"
            def gitRepo = "${gitUrl}/${codebase}"
            dm.createPipelineFolder(codebasePath, gitUrl, gitLib, gitCreds)
            echo "Creating pipeline git branch folder ${codebasePath}/${gitBranch}"
            jobDsl scriptText: 	"""folder('${codebasePath}/${gitBranch}') """
            //if (environments) {
            //    buildList = environments
            //}
            def parallelPipelines = [:]
            for (int count=0; count < buildList.size(); count++) {
                def buildPath = buildList[count]
                def buildName = buildPath
                if (buildName == '.' || !buildName || buildName == null) {
                    buildName = "build_root"
                }
                if (buildName.contains('/')) {
                    buildName = buildName.tokenize('/').join('.')
                }
                parallelPipelines["pipe"+count] = {
                    echo "Creating Pipeline for: ${gitBranch}/${buildName}"
                    dm.buildPipelineJob(gitRepo, "${codebasePath}/${gitBranch}/${buildName}", buildPath, gitCreds, gitBranch)
                }
            }
            parallel parallelPipelines
        }
    } catch (error) {
        echo "Pipeline Creation Failed"
        echo error.message
        echo error
    }
}

return this;
