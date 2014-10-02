#!/bin/sh
#
# Copyright © 2014 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#

### BEGIN INIT INFO
# Provides:          file-drop-zone
# Required-Start:    $remote_fs
# Should-Start:
# Required-Stop:     $remote_fs
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: File Drop Zone
### END INIT INFO

# Custom part

NAME=file-drop-zone
DESC="File Drop Zone"

# Common part

DEFAULT=/etc/default/$NAME
FDZ_LOG_DIR=/var/log/file-drop-zone
FDZ_CONF_DIR=/etc/$NAME/conf
FDZ_RUN_DIR=/var/run/file-drop-zone
FDZ_HOME=/usr/lib/$NAME
FDZ_USER=file-drop-zone
FDZ_LOCK_DIR="/var/lock/subsys/"
FDZ_PID_FILE=${FDZ_RUN_DIR}/${NAME}.pid
FDZ_SHUTDOWN_TIMEOUT=${FDZ_SHUTDOWN_TIMEOUT:-60}

LOCKFILE="${FDZ_LOCK_DIR}/${NAME}"
desc="$DESC daemon"
EXEC_PATH=/usr/bin/$NAME
JAVA_OPTIONS="-Xmx256m"

STATUS_RUNNING=0
STATUS_DEAD=1
STATUS_DEAD_AND_LOCK=2
STATUS_NOT_RUNNING=3

ERROR_PROGRAM_NOT_INSTALLED=5

if [ `id -u` -ne 0 ]; then
	echo "You need root privileges to run this script"
	exit 1
fi

. /lib/lsb/init-functions

if [ -f "$DEFAULT" ]; then
    . "$DEFAULT"
fi


# These directories may be tmpfs and may or may not exist
# depending on the OS (ex: /var/lock/subsys does not exist on debian/ubuntu)
for dir in "$FDZ_RUN_DIR" "$FDZ_LOCK_DIR"; do
  [ -d "${dir}" ] || install -d -m 0755 -o $FDZ_USER -g $FDZ_USER ${dir}
done

export FDZ_HOME
export FDZ_LOG_DIR
export JAVA_OPTIONS

start() {
  [ -x $exec ] || exit $ERROR_PROGRAM_NOT_INSTALLED

  pidofproc -p $FDZ_PID_FILE java > /dev/null
  status=$?

  if [ "$status" -eq "$STATUS_RUNNING" ]; then
    exit 0
  fi

  if [ ! -d ${FDZ_LOG_DIR} ]; then
      mkdir -p ${FDZ_LOG_DIR}
  fi

  FDZ_ARGS=$1
  log_success_msg "Starting $desc ($NAME): "
  /bin/su -s /bin/bash -c "/bin/bash -c 'echo \$\$ > $FDZ_PID_FILE && exec ${EXEC_PATH} start $FDZ_ARGS $2 >>${FDZ_LOG_DIR}/${NAME}.init.log 2>&1' &" $FDZ_USER
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE
  return $RETVAL
}

stop() {
  log_success_msg "Stopping $desc ($NAME): "
  if [ -e $FDZ_PID_FILE ]; then
    FDZ_PID=`cat $FDZ_PID_FILE`
    if [ -n $FDZ_PID ]; then
        for i in `seq 1 ${FDZ_SHUTDOWN_TIMEOUT}` ; do
          kill -15 ${FDZ_PID}
          status=$?
          if [ $status -eq 0 ]; then
            break
          fi
          sleep 1
        done
        kill -KILL ${FDZ_PID} >/dev/null
    fi
    rm -f $LOCKFILE $FDZ_PID_FILE
  fi
  return 0
}

restart() {
  stop
  start
}

checkstatus(){
  pidofproc -p $FDZ_PID_FILE java > /dev/null
  status=$?

  case "$status" in
    $STATUS_RUNNING)
      log_success_msg "$desc is running"
      ;;
    $STATUS_DEAD)
      log_failure_msg "$desc is dead and pid file exists"
      ;;
    $STATUS_DEAD_AND_LOCK)
      log_failure_msg "$desc is dead and lock file exists"
      ;;
    $STATUS_NOT_RUNNING)
      log_failure_msg "$desc is not running"
      ;;
    *)
      log_failure_msg "$desc status is unknown"
      ;;
  esac
  return $status
}

condrestart(){
  [ -e ${LOCKFILE} ] && restart || :
}

case "$1" in
  start)
    start $2 $3
    ;;
  stop)
    stop
    ;;
  status)
    checkstatus
    ;;
  restart)
    restart
    ;;
  condrestart|try-restart)
    condrestart
    ;;
  *)
    echo $"Usage: $0 {start|stop|status|restart|try-restart|condrestart}"
    exit 1
esac

exit $RETVAL
