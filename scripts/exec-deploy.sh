set -euo pipefail
IFS=$'\n\t'

latest () {
  ls | grep "$1.*\.jar" | sort -V -r | head -n 1
}

server_admin_jar="$(latest server-admin)"
synchro_jar="$(latest synchro)"

echo "Deploying $synchro_jar"
java -jar $server_admin_jar $synchro_jar
