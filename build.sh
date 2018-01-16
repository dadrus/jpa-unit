#/bin/bash

export SONAR_HOST_URL='https://sonarcloud.io'
export SONAR_ORGANIZATION='dadrus-github'

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Building and analyzing a regular branch"
  
  if [ "$TRAVIS_BRANCH" == "master" ]; then
    mvn -settings .travis/settings.xml clean deploy sonar:sonar \
	  -Dsonar.organization=$SONAR_ORGANIZATION \
	  -Dsonar.host.url=$SONAR_HOST_URL \
	  -Dsonar.login=$SONAR_TOKEN \
	  -Dpgp.skip=false
  else
    mvn clean verify sonar:sonar \
	  -Dsonar.organization=$SONAR_ORGANIZATION \
	  -Dsonar.host.url=$SONAR_HOST_URL \
	  -Dsonar.login=$SONAR_TOKEN \
	  -Dsonar.branch.name=$TRAVIS_BRANCH
  fi
else
  echo "Building and analyzing a pull request from $TRAVIS_PULL_REQUEST_BRANCH branch"
  
  mvn clean verify sonar:sonar \
    -Dsource.skip=true \
    -Dsonar.analysis.mode=preview \
    -Dsonar.organization=$SONAR_ORGANIZATION \
    -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
    -Dsonar.github.repository=$TRAVIS_PULL_REQUEST_SLUG \
    -Dsonar.github.oauth=$SONAR_GITHUB_TOKEN \
    -Dsonar.host.url=$SONAR_HOST_URL \
    -Dsonar.login=$SONAR_TOKEN \
    -Dsonar.branch.name=$TRAVIS_PULL_REQUEST_BRANCH
fi