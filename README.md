#Jenkins Pipeline Jobs Seeder Repo

## Pipeline Job Creation Readme

### Introduction
This is a creation method for pipeline jobs.  The job creation is handled by a JSON configuration file.


### Structure:

The structure is as follows:

Jenkinsfile add entries as follows:

```
  load('deploy/pipeline.groovy').manage('jobs/<project>/config.json')
```

This in turn calls deploy/pipeline.groovy, and passes the json configuration file to this, the json file is as
follows:

```
{
  "name": "project-builds",
  "git_credentials": "git_user",
  "git_url": "https://github.com/vamegh",
  "git_extra_lib": "jenkins-sandbox-lib",
  "deployments": [
    {
      "git_repo": "Jenkins-Pipeline-Seeder",
      "git_branch": "master",
      "paths": [
        "jenkins-master/ubuntu-ec2",
        "jenkins-agent/ubuntu-ec2",
        "base"
      ]
    }
  ]
}
```


The pipeline will then be created as follows:

```
Jenkins >> project-builds >> Jenkins-Pipeline-Seeder >> master >> jenkins-master.ubuntu-ec2
Jenkins >> project-builds >> Jenkins-Pipeline-Seeder >> master >> jenkins-agent.ubuntu-ec2
Jenkins >> project-builds >> Jenkins-Pipeline-Seeder >> master >> base
```

The name is the top level. This then leads into the codebase or git_repo (either name can be used),
there can be multiple codebases / git repos specified they will all be created here,
followed by the git_branch then the paths again there can be multiple paths listed under any 
particular codebase/git_repo and git branch.

The pipeline will then look for a Jenkinsfile in each path specified in that git repo,
which will allow for multiple different types of builds from a single git repo. if the path is blank or "."
it will be renamed to build_root and the Jenkinsfile will be expected to be in the root of the git repo,
rather than in a specific folder 

the git_extra_lib setting attaches that specific library into the particular pipeline being defined, this
can be done at the deployments level or at the codebase level, this is optional if not provided no extra libraries
will be attached.

git_branch can be specified either at the top level or at the deployments level, 
if it is not defined / specified it defaults to master. If specified at the deployments level it will need to be
specified for each git_repo, if all of the git_repos use the same branch then it can be specified at the top level.

git_url must be provided this is the base from which all codebases / git repos and extra libs will be downloaded 
or pulled from. 

git_credentials must be provided, either at the top level or from the deployments level, if the git credentials 
are the same for all codebases it can be provided at the top level as in the example above, if the various 
codebases require different credentials then git_credentials can be supplied with the codebase and that 
specific credential will be used for that codebase. In general this should not need to be changed 
from the example above.


pipeline.groovy reads in the JSON file maps the file to the structure as described above and then calls 
deploy/main.groovy to do the rest.


```
(https://github.com/vamegh/jenkins-global)/vars/jsonConfigParse.groovy
   _|-----------> com.ev9.global.ConfigParser
                         _|-------> com.ev9.global.Deployments

```

This does the Json Config file parsing, so if any new fields need to be added,
please add it to the above structure.

### Usage
Usage is simple, the groovy files dont need to be modified. New jobs should be specified in the Jenkinsfile
and the job details should be added to jobs/job_name/config.json


