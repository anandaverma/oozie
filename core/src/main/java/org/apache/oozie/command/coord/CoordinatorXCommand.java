/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oozie.command.coord;

import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.CoordinatorJobBean;
import org.apache.oozie.client.event.Event.AppType;
import org.apache.oozie.command.XCommand;
import org.apache.oozie.coord.CoordELFunctions;
import org.apache.oozie.event.CoordinatorActionEvent;
import org.apache.oozie.event.CoordinatorJobEvent;
import org.apache.oozie.util.ParamChecker;

/**
 * Abstract coordinator command class derived from XCommand
 */
public abstract class CoordinatorXCommand<T> extends XCommand<T> {

    /**
     * Base class constructor for coordinator commands.
     *
     * @param name command name
     * @param type command type
     * @param priority command priority
     */
    public CoordinatorXCommand(String name, String type, int priority) {
        super(name, type, priority);
    }

    /**
     * Base class constructor for coordinator commands.
     *
     * @param name command name
     * @param type command type
     * @param priority command priority
     * @param dryrun true if rerun is enabled for command
     */
    public CoordinatorXCommand(String name, String type, int priority, boolean dryrun) {
        super(name, type, priority, dryrun);
    }

    protected static void generateEvent(CoordinatorActionBean coordAction, String user, String appName) {
        if (eventService.checkSupportedApptype(AppType.COORDINATOR_ACTION.name())) {
            ParamChecker.notNull(coordAction, "coordAction");
            ParamChecker.notNull(user, "user");
            ParamChecker.notNull(appName, "appName");
            String missDep = coordAction.getMissingDependencies();
            if (missDep != null && missDep.length() > 0) {
                missDep = missDep.split(CoordELFunctions.INSTANCE_SEPARATOR)[0];
            }
            String pushMissDep = coordAction.getPushMissingDependencies();
            if (pushMissDep != null && pushMissDep.length() > 0) {
                pushMissDep = pushMissDep.split(CoordELFunctions.INSTANCE_SEPARATOR)[0];
            }
            String deps = missDep == null ? (pushMissDep == null ? null : pushMissDep) : (pushMissDep == null ? missDep
                    : missDep + CoordELFunctions.INSTANCE_SEPARATOR + pushMissDep);
            CoordinatorActionEvent event = new CoordinatorActionEvent(coordAction.getId(), coordAction.getJobId(),
                    coordAction.getStatus(), user, appName, coordAction.getNominalTime(), coordAction.getCreatedTime(),
                    deps);
            event.setErrorCode(coordAction.getErrorCode());
            event.setErrorMessage(coordAction.getErrorMessage());
            eventService.queueEvent(event);
        }
    }

    protected void generateEvent(CoordinatorJobBean coordJob) {
        if (eventService.checkSupportedApptype(AppType.COORDINATOR_JOB.name())) {
            ParamChecker.notNull(coordJob, "coordJob");
            CoordinatorJobEvent event = new CoordinatorJobEvent(coordJob.getId(), coordJob.getBundleId(),
                    coordJob.getStatus(), coordJob.getUser(), coordJob.getAppName(), coordJob.getStartTime(),
                    coordJob.getEndTime());
            eventService.queueEvent(event);
        }
    }

}
