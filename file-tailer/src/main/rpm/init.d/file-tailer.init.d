#!/bin/bash
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
# Provides:          file-tailer
# Required-Start:    $remote_fs
# Should-Start:
# Required-Stop:     $remote_fs
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: File Tailer
### END INIT INFO

# Custom part

NAME=file-tailer
DESC="File Tailer"

# Common part

DEFAULT=/etc/default/$NAME
FT_LOG_DIR=/var/log/file-tailer
FT_CONF_DIR=/etc/$NAME/conf
FT_RUN_DIR=/var/run/file-tailer
FT_HOME=/usr/lib/$NAME
FT_USER=file-tailer
FT_LOCK_DIR="/var/lock/subsys/"
FT_PID_FILE=${FT_RUN_DIR}/${NAME}.pid
FT_SHUTDOWN_TIMEOUT=${FT_SHUTDOWN_TIMEOUT:-60}

LOCKFILE="${FT_LOCK_DIR}/${NAME}"
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
for dir in "$FT_RUN_DIR" "$FT_LOCK_DIR"; do
  [ -d "${dir}" ] || install -d -m 0755 -o $FT_USER -g $FT_USER ${dir}
done

export FT_HOME
export FT_LOG_DIR
export JAVA_OPTIONS

start() {
  [ -x $exec ] || exit $ERROR_PROGRAM_NOT_INSTALLED

  checkstatus
  status=$?
  if [ "$status" -eq "$STATUS_RUNNING" ]; then
    exit 0
  fi

  if [ ! -d ${FT_LOG_DIR} ]; then
      mkdir -p ${FT_LOG_DIR}
  fi

  log_success_msg "Starting $desc ($NAME): "
  /bin/su -s /bin/bash -c "/bin/bash -c 'echo \$\$ >${FT_PID_FILE} && exec ${EXEC_PATH} start >>${FT_LOG_DIR}/${NAME}-server.init.log 2>&1' &" $FT_USER
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE
  return $RETVAL
}

stop() {
  if [ ! -e $FT_PID_FILE ]; then
    log_failure_msg "$desc is not running"
    exit 0
  fi

  log_success_msg "Stopping $desc ($NAME): "

  FT_PID=`cat $FT_PID_FILE`
  if [ -n $FT_PID ]; then
    ${EXEC_PATH} stop
    for i in `seq 1 ${FT_SHUTDOWN_TIMEOUT}` ; do
      kill -0 ${FT_PID} &>/dev/null || break
      sleep 1
    done
    kill -KILL ${FT_PID} &>/dev/null
  fi
  rm -f $LOCKFILE $FT_PID_FILE
  return 0
}

restart() {
  stop
  start
}

checkstatus(){
  pidofproc -p $FT_PID_FILE java > /dev/null
  status=$?

  case "$status" in
    $STATUS_RUNNING)
      success && echo "$desc is running"
      ;;
    $STATUS_DEAD)
      failure && echo "$desc is dead and pid file exists"
      ;;
    $STATUS_DEAD_AND_LOCK)
      failure && echo "$desc is dead and lock file exists"
      ;;
    $STATUS_NOT_RUNNING)
      failure && echo "$desc is not running"
      ;;
    *)
      failure && echo "$desc status is unknown"
      ;;
  esac
  return $status
}

condrestart(){
  [ -e ${LOCKFILE} ] && restart || :
}

case "$1" in
  start)
    start
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
