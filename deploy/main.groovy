/**
 * Deployment methods for any journey
 */

/**
 * Creates a pipeline folder and adds the
 * workflow sandbox library to the folder
 *
 * @param basePath journey deploy folder
 * @param gitCredentials journey deploy folder
 */
def createPipelineFolder(basePath, gitURL, gitLib, gitCredentials){
    echo "Creating pipeline folder ${basePath}"
    jobDsl scriptText: 	"""folder('${basePath}') """

    if (gitLib && gitURL) {
        echo "Adding global library ${gitLib} to pipeline folder"
        if (gitLib.contains('.git')) {
            gitLibName = gitLib.split('.git').first()
        } else {
            gitLibName = gitLib
            gitLib = gitLib + ".git"
        }
        folderManager("${basePath}",
                "${gitURL}/${gitLib}",
                "${gitLibName}",
                "${gitCredentials}")
        return null
    }
    echo "Skipping Extra Sandbox Global Library Addition"
}

/**
 * Builds a deployment pipeline job
 *
 * @param gitRepo Git source of the deploy repo
 * @param jobName name of the job
 * @param buildPath - the build Path to be used
 * @param gitCredentials job configuration file to be used
 */
def buildPipelineJob(gitRepo, jobName, buildPath, gitCredentials, gitBranch){
    echo "Adding & Creating Jobs"
    if (! buildPath || buildPath == null || buildPath == '.') {
        buildPath = "Jenkinsfile"
    } else if (!buildPath.contains("Jenkinsfile")) {
        buildPath = "${buildPath}/Jenkinsfile"
    }
    jobDsl scriptText: 	"""
		pipelineJob("${jobName}") {
			definition {
				cpsScm {
                      scm {
                        git {
                          remote {
                            url("${gitRepo}")
                            credentials("${gitCredentials}")
                          }
                          branch("${gitBranch}")
                        }
                      }
                      scriptPath("${buildPath}")
                }
			}
		}
	"""
}
return this;
