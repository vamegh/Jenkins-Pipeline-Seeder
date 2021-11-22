@Library('jenkins-global')
import com.ev9.global.*

node() {
  checkout scm
  parallel (
    "stream 1" : { load('deploy/pipeline.groovy').manage('jobs/example/config.json') },
  )
}

