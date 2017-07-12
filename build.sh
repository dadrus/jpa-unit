#/bin/bash

export SONAR_HOST_URL='https://sonarqube.com'
export SONAR_ORGANIZATION='dadrus-github'

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Building and analyzing a regular branch"
  
  export SONAR_ENV="-Dsonar.organization=$SONAR_ORGANIZATION -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN"
  export MAVEN_SETTINGS=""
  if [ "$TRAVIS_BRANCH" != "master" ]; then
    export SONAR_ENV="$SONAR_ENV -Dsonar.branch=$TRAVIS_BRANCH"
  else
	export MAVEN_SETTINGS="-settings .travis/settings.xml"
  fi
  
  mvn $MAVEN_SETTINGS clean deploy sonar:sonar $SONAR_ENV -Dpgp.skip=false -DskipTests
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
    -Dsonar.login=$SONAR_TOKEN
fi