
java -version
java_version=`java -version 2>&1 | fgrep -i version | cut -d'"' -f2 | sed -e 's/^1\./1\%/' -e 's/\..*//' -e 's/%/./'`

if [[ $java_version -gt 11 ]]  ; then
  echo "Java version $java_version must not be > 11"
  exit 1
fi



mvn -DskipTests=false  compile dependency:copy-dependencies exec:exec
