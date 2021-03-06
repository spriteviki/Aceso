/*
 *
 *  * Copyright (C) 2017 meili-inc company
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.mogujie.aceso

import com.mogujie.aceso.processor.ExpandScopeProcessor
import com.mogujie.aceso.processor.FixClassProcessor
import com.mogujie.aceso.transoform.HookDexTransform
import com.mogujie.aceso.transoform.HookTransform
import com.mogujie.aceso.util.FileUtils
import com.mogujie.aceso.util.GradleUtil
import com.mogujie.aceso.util.Log
import com.mogujie.instantrun.IncrementalTool
import org.gradle.api.GradleException
/**
 * A plugin for generate patch apk.
 *
 * @author wangzhi
 */

public class AcesoFixPlugin extends AcesoBasePlugin {

    @Override
    protected void realApply() {
        IncrementalTool.setMethodLevelFix(config.methodLevelFix)

        project.android.applicationVariants.all { variant ->
            doIt(variant)
        }
    }

    public void doIt(def variant) {
        String varName = variant.name.capitalize()
        String varDirName = variant.getDirName()
        //create the aceso task
        project.tasks.create("aceso" + varName, AcesoTask, new AcesoTask.HotFixAction(varName))
        addProguardKeepRule(variant)
        if (GradleUtil.isAcesoFix(project)) {
            Log.i "next will be aceso fix."
            HookTransform.injectTransform(project, variant, new FixClassProcessor(project, variant, config),
                    HookDexTransform.BUILDER)
        } else {
            Log.i "next will expand scope."
            HookTransform.injectTransform(project, variant, new ExpandScopeProcessor(project, variant, config)
                    , HookDexTransform.BUILDER)
        }
    }


    protected void assignDefaultValue() {
        super.assignDefaultValue()

        if (FileUtils.isStringEmpty(config.instrumentJar)) {
            config.instrumentJar = new File(project.projectDir, Constant.INSTRUMENT_JAR).absolutePath
        }
//        if (FileUtils.isStringEmpty(config.allClassesJar)) {
//            config.allClassesJar = new File(project.projectDir, Constant.ALL_CLASSES_JAR).absolutePath
//        }
    }

    protected void checkNecessaryFile() {
        super.checkNecessaryFile()

        if (!GradleUtil.isAcesoFix(project)) {
            return
        }
        if (!FileUtils.checkFile(new File(config.instrumentJar))) {
            throw new GradleException("instrumentJar('${config.instrumentJar}') not found!")
        }
//        if (!FileUtils.checkFile(new File(config.allClassesJar))) {
//            throw new GradleException("allClassesJar('${config.allClassesJar}') not found!")
//        }
        if (!FileUtils.checkFile(config.acesoMapping)) {
            throw new GradleException("acesoMapping('${config.acesoMapping}') not found!")
        }
    }
}
