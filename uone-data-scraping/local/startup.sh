#!/bin/bash

function _getcaauthtoken() {
	echo "Logging in to SharedServices CodeArtifact for pip install during build..."
	export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain uone-engineering --domain-owner 972576019456 --query authorizationToken --output text` ;
}

case ${1} in
	docker)
		case ${2} in
			build)
				_getcaauthtoken
				echo "Running '$@ --build-arg CODEARTIFACT_AUTH_TOKEN=...'"
				$@ --build-arg CODEARTIFACT_AUTH_TOKEN=$CODEARTIFACT_AUTH_TOKEN
				;;
			*) $@ ;;
		esac
		;;
	docker-compose)
		echo "Intercepting 'docker-compose' command..."
		case ${2} in
			build)
				echo "Intercepting 'build' command..."
				_getcaauthtoken
				echo "Running build with login --build-arg..."
				$@ --build-arg CODEARTIFACT_AUTH_TOKEN=$CODEARTIFACT_AUTH_TOKEN
				echo "exiting..."
				exit 0 ;
				;;
			up)
				echo "Intercepting 'up' command, in case of '--build' flag..."
				case ${3} in
					--build)
						echo "--build flag found, running a build first..."
						_getcaauthtoken
						docker-compose build --build-arg CODEARTIFACT_AUTH_TOKEN=$CODEARTIFACT_AUTH_TOKEN
						${1} ${2} ${@: 4};
						echo "exiting..."
						exit 0 ;
						;;
					*) $@ ;;
				esac
				;;
			*) $@ ;;
		esac
		;;
esac
