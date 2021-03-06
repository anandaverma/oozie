/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oozie.action.hadoop;

import static org.apache.oozie.action.hadoop.LauncherMapper.CONF_OOZIE_ACTION_MAIN_CLASS;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.action.ActionExecutorException;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.XOozieClient;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public class HiveActionExecutor extends ScriptLanguageActionExecutor {

    private static final String HIVE_MAIN_CLASS_NAME = "org.apache.oozie.action.hadoop.HiveMain";
    static final String HIVE_SCRIPT = "oozie.hive.script";
    static final String HIVE_PARAMS = "oozie.hive.params";

    public HiveActionExecutor() {
        super("hive");
    }

    @Override
    protected String getLauncherMain(Configuration launcherConf, Element actionXml) {
        return launcherConf.get(CONF_OOZIE_ACTION_MAIN_CLASS, HIVE_MAIN_CLASS_NAME);
    }

    @Override
    @SuppressWarnings("unchecked")
    Configuration setupActionConf(Configuration actionConf, Context context, Element actionXml,
                                  Path appPath) throws ActionExecutorException {
        Configuration conf = super.setupActionConf(actionConf, context, actionXml, appPath);

        Namespace ns = actionXml.getNamespace();
        String script = actionXml.getChild("script", ns).getTextTrim();
        String scriptName = new Path(script).getName();
        String hiveScriptContent = context.getProtoActionConf().get(XOozieClient.HIVE_SCRIPT);

        if (hiveScriptContent == null){
            addToCache(conf, appPath, script + "#" + scriptName, false);
        }

        List<Element> params = (List<Element>) actionXml.getChildren("param", ns);
        String[] strParams = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            strParams[i] = params.get(i).getTextTrim();
        }

        setHiveScript(conf, scriptName, strParams);
        return conf;
    }

    public static void setHiveScript(Configuration conf, String script, String[] params) {
        conf.set(HIVE_SCRIPT, script);
        MapReduceMain.setStrings(conf, HIVE_PARAMS, params);
    }

    @Override
    protected boolean getCaptureOutput(WorkflowAction action) throws JDOMException {
        return true;
    }

    /**
     * Return the sharelib name for the action.
     *
     * @return returns <code>hive</code>.
     * @param actionXml
     */
    @Override
    protected String getDefaultShareLibName(Element actionXml) {
        return "hive";
    }

    protected String getScriptName() {
        return XOozieClient.HIVE_SCRIPT;
    }

}
