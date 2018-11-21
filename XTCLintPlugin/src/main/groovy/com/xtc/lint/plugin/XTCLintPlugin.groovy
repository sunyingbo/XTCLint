package com.xtc.lint.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.tasks.Lint
import org.gradle.api.*
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.tasks.TaskState

/**
 * aar虽然很方便，但是在团队内部推广中我们遇到了以下问题：

 配置繁琐，不易推广。每个库都需要自行配置lint.xml、lintOptions，并且compile aar。
 不易统一。各库之间需要使用相同的配置，保证代码质量。但现在手动来回拷贝规则，且配置文件可以自己修改。

 于是我们想到开发一个plugin，统一管理lint.xml和lintOptions，自动添加aar。
 */
class XTCLintPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        applyTask(project, getAndroidVariants(project))
    }

    private static final String sPluginMissConfiguredErrorMessage = "Plugin requires the 'android' or 'android-library' plugin to be configured."

    /**
     * 获取project 项目 中 android项目 或者library项目 的 variant 列表
     * @param project 要编译的项目
     * @return variants列表
     */
    private static DomainObjectCollection<BaseVariant> getAndroidVariants(Project project) {

        if (project.getPlugins().hasPlugin(AppPlugin)) {
            return project.getPlugins().getPlugin(AppPlugin).extension.applicationVariants
        }

        if (project.getPlugins().hasPlugin(LibraryPlugin)) {
            return project.getPlugins().getPlugin(LibraryPlugin).extension.libraryVariants
        }

        throw new ProjectConfigurationException(sPluginMissConfiguredErrorMessage, null)
    }

    /**
     *  插件的实际应用：统一管理lint.xml和lintOptions，自动添加aar。
     * @param project 项目
     * @param variants 项目的variants
     */
    private void applyTask(Project project, DomainObjectCollection<BaseVariant> variants) {
        //========================== 统一  自动添加AAR  开始=============================================//
        //配置project的dependencies配置，默认都自动加上 自定义lint检测的AAR包
        project.dependencies {
            //如果是android application项目
            if (project.getPlugins().hasPlugin('com.android.application')) {
                compile('com.xtc.lint:lint-check:+') {
                    force = true
                }
            } else {
                provided('com.xtc.lint:lint-check:+') {
                    force = true
                }
            }
        }

        //去除gradle缓存的配置
        project.configurations.all {
            resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
            resolutionStrategy.cacheDynamicVersionsFor 0, 'seconds'
        }
        //========================== 统一  自动添加AAR  结束=============================================//


        def xtcLintTaskExists = false

        variants.all { variant ->
            //获取Lint Task
            def variantName = variant.name.capitalize()
            Lint lintTask = project.tasks.getByName("lint" + variantName) as Lint

            //Lint 会把project下的lint.xml和lintConfig指定的lint.xml进行合并，为了确保只执行插件中的规则，采取此策略
            File lintFile = project.file("lint.xml")
            File lintOldFile = null

            //==========================  统一  lintOptions  开始=============================================//
            /*
            lintOptions {
               lintConfig file("lint.xml")
               warningsAsErrors true
               abortOnError true
               htmlReport true
               htmlOutput file("lint-report/lint-report.html")
               xmlReport false
            }
            */

            def newLintOptions = new LintOptions()

            //配置lintConfig的配置文件路径
            newLintOptions.lintConfig = lintFile

            //是否将所有的warnings视为errors
//            newLintOptions.warningsAsErrors = true

            //是否lint检测出错则停止编译
            newLintOptions.abortOnError = true

            //htmlReport打开
            newLintOptions.htmlReport = true
            newLintOptions.htmlOutput = project.file("${project.buildDir}/reports/lint/lint-result.html")

            //xmlReport打开 因为Jenkins上的插件需要xml文件
            newLintOptions.xmlReport = true
            newLintOptions.xmlOutput = project.file("${project.buildDir}/reports/lint/lint-result.xml")

            //配置 lint任务的配置为  newLintOptions
            lintTask.lintOptions = newLintOptions
            //==========================  统一  lintOptions  结束=============================================//

            //==========================  统一  lint.xml  开始=============================================//
            //lint任务执行前，先复制lint.xml
            lintTask.doFirst {
                //如果 lint.xml 存在，则改名为 lintOld.xml
                if (lintFile.exists()) {
                    lintOldFile = project.file("lintOld.xml")
                    lintFile.renameTo(lintOldFile)
                }

                //进行 将plugin内置的lint.xml文件和项目下面的lint.xml进行复制合并操作
                def isLintXmlReady = copyLintXml(project, lintFile)
                //合并完毕后，将lintOld.xml 文件改名为 lint.xml
                if (!isLintXmlReady) {
                    if (lintOldFile != null) {
                        lintOldFile.renameTo(lintFile)
                    }
                    throw new GradleException("lint.xml不存在")
                }
            }

            //lint任务执行后，删除lint.xml
            project.gradle.taskGraph.afterTask { task, TaskState state ->
                if (task == lintTask) {
                    lintFile.delete()
                    if (lintOldFile != null) {
                        lintOldFile.renameTo(lintFile)
                    }
                }
            }
            //==========================  统一  lint.xml  结束=============================================//


            //==========================  在终端 执行命令 gradlew lintForXTC  的配置  开始=============================================//
            // 在终端 执行命令 gradlew lintForXTC 的时候，则会应用  lintTask
            if (!xtcLintTaskExists) {
                xtcLintTaskExists = true
                //创建一个task 名为  lintForXTC
                project.task("lintForXTC").dependsOn lintTask
            }
            //==========================  在终端 执行命令 gradlew lintForXTC  的配置  结束=============================================//
        }
    }

    /**
     * 复制 lint.xml 到 targetFile
     * @param project 项目
     * @param targetFile 复制到的目标文件
     * @return 是否复制成功
     */
    boolean copyLintXml(Project project, File targetFile) {
        //创建目录
        targetFile.parentFile.mkdirs()

        //目标文件为  resources/config/lint.xml文件
        InputStream lintIns = this.class.getResourceAsStream("/config/lint.xml")
        OutputStream outputStream = new FileOutputStream(targetFile)

        int retroLambdaPluginVersion = getRetroLambdaPluginVersion(project)

        if (retroLambdaPluginVersion >= 180) {
            // 加入屏蔽try with resource 检测  1.8.0版本引入此功能
            InputStream retroLambdaLintIns = this.class.getResourceAsStream("/config/retrolambda_lint.xml")
            XMLMergeUtil.merge(outputStream, "/lint", lintIns, retroLambdaLintIns)
        } else {
            // 未使用 或 使用了不支持try with resource的版本
            IOUtils.copy(lintIns, outputStream)
            IOUtils.closeQuietly(outputStream)
            IOUtils.closeQuietly(lintIns)
        }

        //如果复制操作完成后，目标文件存在
        if (targetFile.exists()) {
            return true
        }
        return false
    }
    /**
     * 获取 使用的 RetroLambda Plugin插件的版本
     * @param project 项目
     * @return 没找到时返回-1 ，找到返回正常version
     */
    def static int getRetroLambdaPluginVersion(Project project) {
        DefaultExternalModuleDependency retroLambdaPlugin = findClassPathDependencyVersion(project, 'me.tatarka', 'gradle-retrolambda') as DefaultExternalModuleDependency
        if (retroLambdaPlugin == null) {
            retroLambdaPlugin = findClassPathDependencyVersion(project.getRootProject(), 'me.tatarka', 'gradle-retrolambda') as DefaultExternalModuleDependency
        }
        if (retroLambdaPlugin == null) {
            return -1
        }
        return retroLambdaPlugin.version.split("-")[0].replaceAll("\\.", "").toInteger()
    }

    /**
     * 查找Dependency的Version信息
     * @param project
     * @param group
     * @param attributeId
     * @return
     */
   def static findClassPathDependencyVersion(Project project, group, attributeId) {
        return project.buildscript.configurations.classpath.dependencies.find {
            it.group != null && it.group.equals(group) && it.name.equals(attributeId)
        }
    }
}