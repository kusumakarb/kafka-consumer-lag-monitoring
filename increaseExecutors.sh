#! /bin/bash
PID=$(sudo -H -u spark bash -c 'jps' | grep SparkSubmit | awk '{print $1}')
echo $PID
if [[ "" !=  "$PID" ]]; then
  echo "killing $PID"
  sudo kill  $PID
fi

a="sudo -H -u spark bash -c '/usr/local/spark/latest/bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.2.0 --master spark://kafka01:7077 --conf \"spark.sql.shuffle.partitions=1\" --conf \"spark.default.parallelism=1\" --conf \"spark.executor.cores=1\" --conf \"spark.executor.instances="$1"\"  --executor-memory 2G --total-executor-cores "$1"  --driver-memory 1G  --class barclays.OutlierDetectionApp /home/ubuntu/theDemo/realTime.jar'"


eval  $a