#!/bin/bash
set -eu

main() {

  local inFile=$1
  local args=("$@")
  local outFile=$2
  if [[ $# -lt 2 ]]; then
  	usage
  fi

  cp $inFile $outFile
  for ((i=2; i<${#args[@]}; i++)); do
  	local pair=(${args[i]//=/ })
  	find "$outFile" -type f -exec sed -i'.backup' "s/\${${pair[0]}}/${pair[1]}/g" {} \;
    echo "Replacing: $i  \${${pair[0]}} with ${pair[1]}"
  done
}

usage(){
	echo "Substitute variables in docker-compose.yml and writes them into the file defined as the first parameter"
	echo "Usage: replace-var.sh oufile.yml var1=value1 var2=value2 ..."
}

main $@
