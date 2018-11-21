【我的Android进阶之旅】Android自定义Lint实践  https://blog.csdn.net/ouyang_peng/article/details/80374867

> 目前这份代码是基于Gradle 2.x 编写的，代码分支为 https://github.com/ouyangpeng/XTCLint/tree/feature/LintBaseOnGradel2.x

> 接下来准备开始基于Gradle 3.x 重新编译自定义Lint插件，写新的Lint自定义规则。 敬请期待！

# 背景

2017年8月份的时候，我在公司开始推广Lint、FindBugs等静态代码检测工具。然后发现系统自带的Lint检测的Issue不满足我们团队内部的特定需求，因此去自定义了部分Lint规则。这个检测运行了大半年，运行良好，团队的代码规范也有了大幅度提升。这个是基于当时Gradle2.x系列写出来的自定义Lint实践总结，过去大半年了，现在将它搬到CSDN博客分享给大家一起学习学习。如果要在Gradle3.x系列使用该自定义规定的话，部分代码都得修改成最新的语法，因此此篇博客的内容请使用Gralde2.x系列编译项目中可以加入，去定义你自己的Lint规则吧。


当时已经实现的自定义规则大概有：

![这里写图片描述](https://img-blog.csdn.net/20180519170039664?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)



我这里只介绍如何去实现你自己的Lint规则，具体源代码的话不方便贴出了，所以不会去公布源代码。


# 一、Lint介绍

android lint是一个静态代码分析工具，通过lint工具，你可以不用边运行边调试，或者通过单元测试进行代码检查，可以检测代码中不规范、不和要求的问题，解决一些潜在的bug。lint工具可以在命令行上使用，也可以在adt中使用。

比如当想检查在manifest.xml中是否有activity，activity中是否包含了launcher activity。如果没有进行错误的警告。

通过lint的这种手段，可以对代码进行规范的控制，毕竟一个团队每个人的风格不同，但是要注意的当然是代码的质量，所以lint可以进行代码的规范和质量控制。

在Android studio还没出来时，lint和Eclipse并不能很好的结合在一起，只能作为一个独立的工具，通过命令行去执行lint检查。

在android studio出现之后，不再建议单独使用lint命令，而是结合gradle进行操作，命令为* ./gradlew lint *进行执行

lint工具通过一下六个方面去检查代码中的问题correctness, security, performance, usability, accessibility, and internationalization。检查的范围包括java文件，xml文件，class文件。

lint工具在sdk16版本之后就带有了，所以在sdk目录/tools/可以找到lint工具。现在建议与gradle一起使用，使用./gradlew lint进行 

[参考官方文档介绍](http://developer.android.com/tools/debugging/improving-w-lint.html)


# 二、使用Lint的方法

关于lint的一些命令，可以[参考官网](http://developer.android.com/tools/help/lint.html)，这里简单介绍一些。

+ lint path(项目目录) ——进行项目的lint检查
+ lint –disable id(MissingTranslation,UnusedIds,Usability:Icons)path ——id是lint issue(问题)的标志，检查项目，不包括指定的issue
+ lint –check id path ——利用指定的issue进行项目检查
+ lint –list ——列出所有的issue
+ lint –show id ——介绍指定的issue
+ lint –help ——查看帮助

## 2.1 使用android studio自带的lint工具
点击Analyze的Inspect Code选项，即可开启lint检查，在Inspection窗口中可以看到lint检查的结果，lint查询的错误类型包括：

*  Missing Translation and Unused Translation【缺少翻译或者没有】
*  Layout Peformance problems (all the issues the old layoutopt tool used to find, and more)【布局展示问题】
*  Unused resources【没有使用的资源】
*  Inconsistent array sizes (when arrays are defined in multiple configurations)【不一致的数组大小】
*  Accessibility and internationalization problems (hardcoded strings, missing contentDescription, etc)【可访问性和国际化问题，包括硬链接的字符串，缺少contentDescription，等等】
*  Icon problems (like missing densities, duplicate icons, wrong sizes, etc)【图片问题，丢失密度，重复图片，错误尺寸等】
*  Usability problems (like not specifying an input type on a text field)【使用规范，比如没有在一个文本上指定输入的类型】
*  Manifest errors【Manifest.xml中的错误】
*  and so on


android自带的lint规则的更改可以在Setting的Edit选项下选择Inspections（File > Settings > Project Settings），对已有的lint规则进行自定义选择。 
[参考官方文档](http://tools.android.com/tips/lint)

## 2.2 使用lint.xml定义检查规则

可以通过lint.xml来自定义检查规则，这里的自定义是指定义系统原有的操作，所以和第一个步骤的结果是一样的，只是可以更方便的配置。

lint.xml生效的位置是要放在项目的根目录下面，lint.xml的示例如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- 忽略指定的检查 -->
    <issue id="IconMissingDensityFolder" severity="ignore" />

    <!-- 忽略指定文件的指定检查 -->
    <issue id="ObsoleteLayoutParam">
        <ignore path="res/layout/activation.xml" />
        <ignore path="res/layout-xlarge/activation.xml" />
    </issue>

    <!-- 更改检查问题归属的严重性 -->
    <issue id="HardcodedText" severity="error" />
</lint>
```

[参考官方文档](http://developer.android.com/tools/debugging/improving-w-lint.html)

# 三、自定义lint检查规则结构介绍

## 3.1 概述

Android Lint是Google提供给Android开发者的静态代码检查工具。使用Lint对Android工程代码进行扫描和检查，可以发现代码潜在的问题，提醒程序员及早修正。

为保证代码质量，在开发流程中加入了代码检查，如果代码检测到问题，则无法合并到正式分支中，这些检查中就包括Lint。

## 3.2 为什么需要自定义

我们在实际使用Lint中遇到了以下问题：

原生Lint无法满足我们团队特有的需求，例如：编码规范。
原生Lint存在一些检测缺陷或者缺少一些我们认为有必要的检测。
基于上面的考虑，我们开始调研并开发自定义Lint。


## 3.3 如何加入已有的lint规则

Lint规则是基于Java写的，是在AST抽象语法树上去进行一个解析。所以在写Lint规则的时候，要学习一下AST抽象语法树。才知道如何去寻找一个类方法和其参数等。以下有两种方法：

1.  所以自定义Lint规则应该是一个写好的jar包，jar包生效的位置是在**~/.android/lint**目录，这个是对于Mac和Linux来说的，对于Windows来说就是在**C:/Users/Administrator/.android/lint**下，放到这个目录下，Lint工具会自动加载这个jar包作为lint的自定义检查规则。

2.  放到lint目录下着实是一件比较麻烦的事情，即使可以用脚本来代替，但是仍然不是一个特别方便的方法。也是由于当android项目直接依赖于lint.jar包时不能起作用，而无法进行直接依赖。 
而aar很好的解决了这个问题，aar能够将项目中的资源、class文件、jar文件等都包含，所以通过将lint.jar放入lintaar中，再由项目依赖于lintaar，这时候就可以达到自定义lint检查的目的。


下面就是如何使自定义lint生效的代码示例，使用第二个方法（第二个方法就包括了第一个方法）：

![这里写图片描述](https://img-blog.csdn.net/20180519153009863?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

主要包括了三个Module一个是**XTCLintrRules**，一个是**XTCLintAAR**，一个是**XTCLintPlugin**，还有一个是测试的**app**项目。

*  **XTCLintrRules** ，主要是用来编写自定义Lint规则，编译后生成 lint.jar
*  **XTCLintAAR**，主要是将**XTCLintrRules**生成的**lint.jar**包大包成aar，方便引用
*  **XTCLintPlugin**，主要是后面统一管理**lint.xml和lintOptions，自动添加aar**，后面再讲解。

### 3.3.1 XTCLintrRules的 gradle配置

在XTCLintrRules中，主要是编写lint规则,他是一个Java工程。

它的gradle如下：

```groovy
//java项目，该项目编译之后生成  XTCLintRules.jar

apply plugin: 'java'

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])

    // 依赖于lint的规则的api
    compile 'com.android.tools.lint:lint-api:25.0.0'
    compile 'com.android.tools.lint:lint-checks:25.0.0'
    testCompile 'com.android.tools.lint:lint-tests:24.5.0'
}

/**
 * Lint-Registry是透露给lint工具的注册类的方法，
 * 也就是PermissionIssueRegistry是lint工具的入口,同时也通过这个方法进行打jar包
 */
jar{
    manifest{
        attributes('Lint-Registry': 'com.xtc.lint.rules.XTCIssueRegister')
    }
}

// 创建了一个叫“lintJarOutput”的Gradle configuration，
// 用于输出我们生成的jar包。在生成aar的模块 "XTCLintAAR" 的build.gradle中会引用此configuration。
configurations {
    lintJarOutput
}

// 指定定义方法lintJarOutput的作用，此处是获得调用jar方法后的生成的jar包
dependencies {
    lintJarOutput files(jar)
}

defaultTasks 'assemble'

//指定编译使用JDK1.8
//sourceCompatibility = JavaVersion.VERSION_1_8
//targetCompatibility = JavaVersion.VERSION_1_8

//指定编译的编码
tasks.withType(JavaCompile){
    options.encoding = "UTF-8"
}
```

+ lint-api: 官方给出的API，API并不是最终版，官方提醒随时有可能会更改API接口。
+ lint-checks：已有的检查。

### 3.3.2 XTCLintAAR的 gradle配置

```groovy
apply plugin: 'com.android.library'

apply from: 'maven_upload.gradle'
apply from: '../jenkins.gradle'

android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"

    defaultConfig {
        minSdkVersion 9
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        debug {
            buildConfigField 'String', 'JenkinsName', "\"" + jenkinsName + "\""
            buildConfigField 'String', 'JenkinsRevision', "\"" + jenkinsRevision + "\""
            buildConfigField 'String', 'GitSHA', "\"" + gitSHA  + "\""
            buildConfigField 'String', 'GitBranch', "\"" + gitBranch + "\""
            buildConfigField 'String', 'GitTag', "\"" + gitTag + "\""

            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }

        release {
            buildConfigField 'String', 'JenkinsName', "\"" + jenkinsName + "\""
            buildConfigField 'String', 'JenkinsRevision', "\"" + jenkinsRevision + "\""
            buildConfigField 'String', 'GitSHA', "\"" + gitSHA  + "\""
            buildConfigField 'String', 'GitBranch', "\"" + gitBranch + "\""
            buildConfigField 'String', 'GitTag', "\"" + gitTag + "\""

            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}


/**
 * 定义方法lintJarImport,引入 XTCLintRules.jar
 *
 * rules for including "lint.jar" in aar
 */
configurations {
    lintJarImport
}

// 链接到lintJar中的lintJarOutput方法,调用jar方法，并获得jar包
dependencies {
    //其引用了模块 “:XTCLintRules”的 Gradle configuration “lintJarOutput”。
    lintJarImport project(path: ':XTCLintrRules', configuration: "lintJarOutput")
}

// 将得到的JAR包复制到目录build/intermediates/lint/下，并且重命名为 lint.jar
task copyLintJar(type: Copy) {
    from(configurations.lintJarImport) {
        rename {
            String fileName ->
                'lint.jar'
        }
    }
    into 'build/intermediates/lint/'
}

// 当项目build到compileLint这一步时执行copyLintJar方法
project.afterEvaluate {
    def compileLintTask = project.tasks.find { it.name == 'compileLint' }
    //对内置的Gradle task “compileLint”做了修改，让其依赖于我们定义的一个task “copyLintJar”。
    compileLintTask.dependsOn(copyLintJar)
}

```

该Module的作用就是：
>1.  链接到lintJar中的lintJarOutput方法,调用jar方法，并获得jar包
2.  将得到的JAR包复制到目录build/intermediates/lint/下，并且重命名为 lint.jar
3.  当项目build到compileLint这一步时执行copyLintJar方法，这样的话就可以调用到我们自定义的Lint规则
4.  生成AAR方便项目调用


### 3.3.3 XTCLintPlugin介绍

关于 XTCLintPlugin 的介绍，等我们先将自定义规则讲解完后再介绍，这里先不介绍。


# 四、自定义lint检查规则的编写

上面大致讲解了下 XTCLintrRules和XTCLintAAR两个Module的gradle配置和作用，下面我们来针对XTCLintrRules这个Module来编写我们自定义的Lint检查规则

![这里写图片描述](https://img-blog.csdn.net/20180519153043439?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


## 4.1 创建 Detector

Detector负责扫描代码，发现问题并报告。  我们通过一个 XTCCustomLogDetector 这个类来学习下 Detector怎么实现，

XTCCustomLogDetector 类主要功能是：针对代码中直接使用**android.util.Log**的方法 { v,d,i,w,e,wtf }或者直接使用了 **System.out.print/System.err.print**进行日志打印的一个判断，然后提示各位开发人员使用我们自定义好的**com.xtc.log.LogUtil**类进行日志打印。



```java

package com.xtc.lint.rules.detectors.java;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.xtc.lint.rules.JavaPackageRelativePersonUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
/**
 * 定义代码检查规则
 * 这个是针对代码中直接使用android.util.Log的方法 { v,d,i,w,e,wtf } 进行日志打印的一个判断
 * </p>
 * created by OuyangPeng at 2017/8/31 9:55
 */
public class XTCCustomLogDetector extends Detector implements Detector.JavaScanner {
    private static final Class<? extends Detector> DETECTOR_CLASS = XTCCustomLogDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_LogUseError";
    private static final String ISSUE_DESCRIPTION = "警告:你应该使用我们团队自定义的Log打印工具类工具类{com.xtc.log.LogUtil}";
    private static final String ISSUE_EXPLANATION = "为了能够更好的控制Log打印的开关，你不能直接使用{android.util.Log}或者{System.out.println}直接打印日志，你应该使用我们团队自定义的Log打印工具类工具类{com.xtc.log.LogUtil}";

    private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;

    private static final String SYSTEM_OUT_PRINT = "System.out.print";
    private static final String SYSTEM_OUT_PRINTLN = " System.out.println";
    private static final String SYSTEM_ERR_PRINT = "System.err.print";
    private static final String SYSTEM_ERR_PRINTLN = " System.err.println";


    private static final String CHECK_PACKAGE = "android.util.Log";

    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    );

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("v", "d", "i", "w", "e", "wtf");
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.singletonList(MethodInvocation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(final JavaContext context) {
        return new LogVisit(context);
    }

    private class LogVisit extends ForwardingAstVisitor {
        private final JavaContext javaContext;

        private LogVisit(JavaContext context) {
            javaContext = context;
        }

        @Override
        public boolean visitMethodInvocation(MethodInvocation node) {
            String nodeString = node.toString();
            if (nodeString.startsWith(SYSTEM_OUT_PRINT)
                    || nodeString.startsWith(SYSTEM_OUT_PRINTLN)
                    || nodeString.startsWith(SYSTEM_ERR_PRINT)
                    || nodeString.startsWith(SYSTEM_ERR_PRINTLN)) {
                String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(javaContext,node);
//                System.out.println("LogVisit visitMethodInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                javaContext.report(ISSUE, node, javaContext.getLocation(node), message);
                return true;
            }

            JavaParser.ResolvedNode resolve = javaContext.resolve(node);
            if (resolve instanceof JavaParser.ResolvedMethod) {
                JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolve;
                JavaParser.ResolvedClass containingClass = method.getContainingClass();

                if (resolve.getName().equals("v")
                        ||resolve.getName().equals("d")
                        ||resolve.getName().equals("i")
                        ||resolve.getName().equals("w")
                        ||resolve.getName().equals("e")
                        ||resolve.getName().equals("wtf")){
//                    System.out.println("XTCCustomLogDetector  called method  one of { v,d,i,w,e,wtf }");
                    if (containingClass.matches(CHECK_PACKAGE)) {
//                      System.out.println("XTCCustomLogDetector  called method  one of { v,d,i,w,e,wtf } , and the className is : android.util.Log");

                        String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(javaContext,node);
//                        System.out.println("LogVisit visitMethodInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                        String message = ISSUE_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                        javaContext.report(ISSUE, node, javaContext.getLocation(node),
                                message);
                        return true;
                    }
                }
            }

            return super.visitMethodInvocation(node);
        }
    }
}


```

## 4.2 Detector介绍

可以看到这个Detector继承Detector类，然后实现Scanner接口。

自定义Detector可以实现一个或多个Scanner接口,选择实现哪种接口取决于你想要的扫描范围

*  Detector.XmlScanner
*  Detector.JavaScanner
*  Detector.ClassScanner
*  Detector.BinaryResourceScanner
*  Detector.ResourceFolderScanner
*  Detector.GradleScanner
*  Detector.OtherFileScanner

![这里写图片描述](https://img-blog.csdn.net/20180519153138624?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

这里因为我们是要针对Java代码扫描，所以选择使用JavaScanner。

![这里写图片描述](https://img-blog.csdn.net/2018051915320334?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

代码中**getApplicableNodeTypes()**方法决定了什么样的类型能够被检测到。这里我们想看Log以及println的方法调用，选取**MethodInvocation**。

对应的，我们在**createJavaVisitor()**创建一个**ForwardingAstVisitor**通过**visitMethodInvocation**方法来接收被检测到的Node。

可以看到**getApplicableNodeTypes()**返回值是一个List，也就是说可以同时检测多种类型的节点来帮助精确定位到代码，对应的**ForwardingAstVisitor**接受返回值进行逻辑判断就可以了。

可以看到JavaScanner中还有其他很多方法，**getApplicableMethodNames**（指定方法名）、**visitMethod**（接收检测到的方法），这种对于直接找寻方法名的场景会更方便。

当然这种场景我们用最基础的方式也可以完成，只是比较繁琐。


那么其他Scanner如何去写呢？
可以去查看各接口中的方法去实现，一般都是有这两种对应：什么样的类型需要返回、接收发现的类型。

这里插一句，Lint是如何实现Java扫描分析的呢？Lint使用了[Lombok](https://github.com/tnorbye/lombok.ast)做抽象语法树的分析。所以在我们告诉它需要什么类型后，它就会把相应的Node返回给我们。


回到示例，当接收到返回的Node之后需要进行判断，如果调用方法是**android.util.Log**的方法 { v,d,i,w,e,wtf }或者直接使用了 **System.out.print/System.err.print**，则调用context.report上报。

```java
  javaContext.report(ISSUE, node, javaContext.getLocation(node), message);
```

第一个参数是Issue，这个之后会讲到；
第二个参数是当前节点；
第三个参数location会返回当前的位置信息，便于在报告中显示定位；
最后的字符串用来为警告添加解释。

对应报告中的位置如下图：

![这里写图片描述](https://img-blog.csdn.net/2018051915323430?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 4.2.1 Issue介绍

Issue由Detector发现并报告，是Android程序代码可能存在的bug。


```

    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_LogUseError";
    private static final String ISSUE_DESCRIPTION = "警告:你应该使用我们团队自定义的Log打印工具类工具类{com.xtc.log.LogUtil}";
    private static final String ISSUE_EXPLANATION = "为了能够更好的控制Log打印的开关，你不能直接使用{android.util.Log}或者{System.out.println}直接打印日志，你应该使用我们团队自定义的Log打印工具类工具类{com.xtc.log.LogUtil}";

    private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;

    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    );
```

声明为final class，由静态工厂方法创建。对应参数解释如下：

![这里写图片描述](https://img-blog.csdn.net/20180519153259535?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ id : 唯一值，应该能简短描述当前问题。利用Java注解或者XML属性进行屏蔽时，使用的就是这个id。
+ summary : 简短的总结，通常5-6个字符，描述问题而不是修复措施。
+ explanation : 完整的问题解释和修复建议。
+ category : 问题类别。详见下文详述部分。
+ priority : 优先级。1-10的数字，10为最重要/最严重。
+ severity : 严重级别：Fatal, Error, Warning, Informational, Ignore。
+ Implementation : 为Issue和Detector提供映射关系，Detector就是当前Detector。声明扫描检测的范围+ + + Scope，Scope用来描述Detector需要分析时需要考虑的文件集，包括：Resource文件或目录、Java文件、Class文件。

## 4.2.2 Issue与Lint HTML报告对应关系

对应着 id  和 summary
![这里写图片描述](https://img-blog.csdn.net/20180519153321579?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


对应着  explanation 、category 、severity 、priority 

![这里写图片描述](https://img-blog.csdn.net/20180519153336561?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 4.2.2 Category详述

系统现在已有的类别如下：

+ Lint
+ Correctness (incl. Messages)
+ Security
+ Performance
+ Usability (incl. Icons, Typography)
+ Accessibility
+ Internationalization
+ Icons
+ Typography
+ Messages

![这里写图片描述](https://img-blog.csdn.net/20180519153403626?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


Category类的部分代码

```
 /** Issues related to running lint itself */
    public static final Category LINT = create("Lint", 110);

    /** Issues related to correctness */
    public static final Category CORRECTNESS = create("Correctness", 100);

    /** Issues related to security */
    public static final Category SECURITY = create("Security", 90);

    /** Issues related to performance */
    public static final Category PERFORMANCE = create("Performance", 80);

    /** Issues related to usability */
    public static final Category USABILITY = create("Usability", 70);

    /** Issues related to accessibility */
    public static final Category A11Y = create("Accessibility", 60);

    /** Issues related to internationalization */
    public static final Category I18N = create("Internationalization", 50);

    // Sub categories

    /** Issues related to icons */
    public static final Category ICONS = create(USABILITY, "Icons", 73);

    /** Issues related to typography */
    public static final Category TYPOGRAPHY = create(USABILITY, "Typography", 76);

    /** Issues related to messages/strings */
    public static final Category MESSAGES = create(CORRECTNESS, "Messages", 95);

    /** Issues related to right to left and bidirectional text support */
    public static final Category RTL = create(I18N, "Bidirectional Text", 40);
```


## 4.2.3 自定义Category


```
public class XTCCategory {
    public static final Category NAMING_CONVENTION = Category.create("小天才命名规范", 101);
}
```

使用

```
public static final Issue ISSUE = Issue.create(
        "IntentExtraKey",
        "intent extra key 命名不规范",
        "请在接受此参数中的Activity中定义一个按照EXTRA_<name>格式命名的常量",
        XTCCategory.NAMING_CONVENTION , 5, Severity.ERROR,
        new Implementation(IntentExtraKeyDetector.class, Scope.JAVA_FILE_SCOPE));
```


## 4.3 IssueRegistry

IssueRegistry就是注册类，继承他，并重写getIssues的方法即可,提供需要被检测的Issue列表

例如我们的项目工程中的**XTCIssueRegister.java**代码如下

```java

package com.xtc.lint.rules;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.xtc.lint.rules.detectors.binaryResource.XTCImageFileSizeDetector;
import com.xtc.lint.rules.detectors.java.XTCActivityFragmentLayoutNameDetector;
import com.xtc.lint.rules.detectors.java.XTCChineseStringDetector;
import com.xtc.lint.rules.detectors.java.XTCCustomLogDetector;
import com.xtc.lint.rules.detectors.java.XTCCloseDetector;
import com.xtc.lint.rules.detectors.java.XTCCustomToastDetector;
import com.xtc.lint.rules.detectors.java.XTCEnumDetector;
import com.xtc.lint.rules.detectors.java.XTCHardcodedValuesDetector;
import com.xtc.lint.rules.detectors.java.XTCHashMapForJDK7Detector;
import com.xtc.lint.rules.detectors.java.XTCMessageObtainDetector;
import com.xtc.lint.rules.detectors.java.XTCViewHolderItemNameDetector;
import com.xtc.lint.rules.detectors.xml.XTCViewIdNameDetector;

import java.util.Arrays;
import java.util.List;

public class XTCIssueRegister extends IssueRegistry {
    static {
        System.out.println("***************************************************");
        System.out.println("**************** lint 读取配置文件 *****************");
        System.out.println("***************************************************");
        LoadPropertiesFile.loadPropertiesFile();
    }

    @Override
    public List<Issue> getIssues() {
        System.out.println("***************************************************");
        System.out.println("**************** lint 开始静态分析代码 *****************");
        System.out.println("***************************************************");
        return Arrays.asList(
                XTCChineseStringDetector.ISSUE,
                XTCActivityFragmentLayoutNameDetector.ACTIVITY_LAYOUT_NAME_ISSUE,
                XTCActivityFragmentLayoutNameDetector.FRAGMENT_LAYOUT_NAME_ISSUE,
                XTCMessageObtainDetector.ISSUE,
                XTCCustomToastDetector.ISSUE,
                XTCCustomLogDetector.ISSUE,
                XTCViewIdNameDetector.ISSUE,
                XTCViewHolderItemNameDetector.ISSUE,
                XTCCloseDetector.ISSUE,
                XTCImageFileSizeDetector.ISSUE,
                XTCHashMapForJDK7Detector.ISSUE,
                XTCHardcodedValuesDetector.ISSUE,
                XTCEnumDetector.ISSUE
        );
    }
}


```


**在getIssues()**方法中返回需要被检测的Issue List，我们刚才编写的 **XTCCustomLogDetector.ISSUE** 也被注册进去了。


在build.grade中声明Lint-Registry属性

```groovy

/**
 * Lint-Registry是透露给lint工具的注册类的方法，
 * 也就是PermissionIssueRegistry是lint工具的入口,同时也通过这个方法进行打jar包
 */
jar{
    manifest{
        attributes('Lint-Registry': 'com.xtc.lint.rules.XTCIssueRegister')
    }
}

```

![这里写图片描述](https://img-blog.csdn.net/20180519153433422?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


至此，自定义Lint的编码部分就完成了。


# 五、为自定义Lint开发plugin

## 5.1 为自定义Lint开发plugin的目的

aar虽然很方便，但是在团队内部推广中我们遇到了以下问题：

+ 配置繁琐，不易推广。每个库都需要自行配置lint.xml、lintOptions，并且compile aar。
+ 不易统一。各库之间需要使用相同的配置，保证代码质量。但现在手动来回拷贝规则，且配置文件可以自己修改。

于是我想到开发一个plugin，统一管理lint.xml和lintOptions，自动添加aar。下图就是我们的工程**XTCLintPlugin**

![这里写图片描述](https://img-blog.csdn.net/20180519153533925?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

编写自定义插件需要 实现 **Plugin<Project>** 接口，然后将插件的作用在**apply(Project project)**方法中实现即可。

![这里写图片描述](https://img-blog.csdn.net/20180519153555763?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

```
class XTCLintPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        applyTask(project, getAndroidVariants(project))
    }
    ...
}
```
## 5.2 自定义Lint开发plugin需要实现的功能
### 5.2.1 统一lint.xml
我们在plugin中内置lint.xml，执行前拷贝过去，执行完成后删除。

```java
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

```

![这里写图片描述](https://img-blog.csdn.net/20180519153621175?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


### 5.2.2 统一lintOptions
Android plugin在1.3以后允许我们替换Lint Task的lintOptions

```java

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

```

![这里写图片描述](https://img-blog.csdn.net/201805191536475?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 5.2.3 自动添加最新aar

```
   //========================== 统一  自动添加AAR  开始=============================================//
        //配置project的dependencies配置，默认都自动加上 自定义lint检测的AAR包
        project.dependencies {
            //如果是android application项目
            if (project.getPlugins().hasPlugin('com.android.application')) {
                compile('com.xtc.lint:lint-check:1.1.1') {
                    force = true
                }
            } else {
                provided('com.xtc.lint:lint-check:1.1.1') {
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
```

![这里写图片描述](https://img-blog.csdn.net/20180519153706266?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


本来这段代码

```
//配置project的dependencies配置，默认都自动加上 自定义lint检测的AAR包
        project.dependencies {
            //如果是android application项目
            if (project.getPlugins().hasPlugin('com.android.application')) {
                compile('com.xtc.lint:lint-check:1.1.1') {
                    force = true
                }
            } else {
                provided('com.xtc.lint:lint-check:1.1.1') {
                    force = true
                }
            }
        }
```
中的 lint-check 版本号 我们写的是  **compile('com.xtc.lint:lint-check:+')** ，但是张亚州的依赖管理库不准我们使用+号，因此我们这里写的是指定好的版本。至此我们的插件功能介绍完毕了，下面是我们的Lint插件XTCLintPlugin的具体实现逻辑代码


```
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
                compile('com.xtc.lint:lint-check:1.1.1') {
                    force = true
                }
            } else {
                provided('com.xtc.lint:lint-check:1.1.1') {
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
```


[关于Android团队使用内部Lint检测的指导文档2 ---> 集成 Lint插件自动添加Lint检测的AAR.md](http://doc.stip.bbkedu.com/share?token=$2y$10$VgmKb3Jrzab.GlaLXtqP8u19SlBTzGU0a0nf8H4oRh8oRuSwI7PdG&skin=purple)

# 六、应用自定义Lint到项目中去
我们刚才说了，我们有自定义Lint的AAR，你可以直接加入到项目中，当然也可以通过自定义的插件来知己添加到项目中去。

## 6.1 添加自定义Lint检测的AAR

### 6.1.1 添加自定义Lint检测的AAR

在项目的build.gradle 里面添加如下代码

```
compile 'com.xtc.lint:lint-check:1.0.1'
```

![这里写图片描述](https://img-blog.csdn.net/20180519161850128?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

目前版本为1.0.1，之后随着自定义规则越来越多，版本再逐步升高。


## 6.2 执行lint检测命令

### 6.2.1  本地执行lint命令

执行如下lint检测命令

```
gradlew clean lint
```
开始执行命令

![这里写图片描述](https://img-blog.csdn.net/20180519162001971?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

开始Lint检测

![这里写图片描述](https://img-blog.csdn.net/20180519162035156?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

检测完毕

![这里写图片描述](https://img-blog.csdn.net/20180519162056326?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 6.2.2 Android Studio执行 Inspect Code
如果上面的命令你用的不习惯，你可以使用Android Studio 自带的代码检测

打开【Analyze】—>【Inspect Code】
![这里写图片描述](https://img-blog.csdn.net/20180519162200287?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

弹出如下所示的Scope对话框，你可以选择检测的范围
![这里写图片描述](https://img-blog.csdn.net/20180519162221327?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

选择完后，点击【OK】按钮即开始进行检测。

APP项目比较大，可能检测得几分钟，请等待。

![这里写图片描述](https://img-blog.csdn.net/20180519162242959?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

检测完后，会出现上图所示的选项框，在Android Lint 下面可以看到我们自定义的Lint检测规则

![这里写图片描述](https://img-blog.csdn.net/20180519162303781?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![这里写代码片](https://img-blog.csdn.net/20180519162328272?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 6.2.3 Jenkins执行lint命令
修改Jenkins的编译命令为

```
clean lint build --stacktrace
```

![这里写图片描述](https://img-blog.csdn.net/20180519162411122?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

编译完后，将lint报告归档到FTP

![这里写图片描述](https://img-blog.csdn.net/20180519162431843?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

Source files 填写

watch/build/outputs/lint-results-debug.html,lint-results-debug.xml
改目录要根据自己项目实际输出的lint报告来填写，可以自己查看Jenkins的工作区生成的目录

![这里写图片描述](https://img-blog.csdn.net/20180519162459508?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![这里写图片描述](https://img-blog.csdn.net/20180519162516866?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

Remote directory 填写

```
'Android/Common/${JOB_NAME}/'yyyy-MM-dd-HH-mm-ss-'build-${BUILD_NUMBER}-git-${GIT_COMMIT}'
```

然后点击【高级】选项，勾选上【Flatten files】和【Remote directory】

![这里写图片描述](https://img-blog.csdn.net/20180519162551656?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

这样编译完后，在FTP服务器对应的目录上就会有lint检测报告了
![这里写图片描述](https://img-blog.csdn.net/20180519162816474?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 6.3 查看Lint检测报告
###6.3.1 lint报告输出目录
lint命令执行完毕之后，会输出相应的lint报告，不同的gradle版本输出的文档地址可能不同，APP的输出目录为：\watch\build\outputs

![这里写图片描述](https://img-blog.csdn.net/20180519163120351?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

gradle版本高的，输出目录可能为 \app\build\reports,如下所示

![这里写图片描述](https://img-blog.csdn.net/20180519163146448?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

###6.3.2 报告详情
打开 \watch\build\outputs\lint-results-debug.html 网页即可看到输出的lint检测报告。
![这里写图片描述](https://img-blog.csdn.net/20180519163219574?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ 控件命名

![这里写图片描述](https://img-blog.csdn.net/20180519163241447?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ 图片太大
![这里写图片描述](https://img-blog.csdn.net/20180519163307319?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ Log打印
![这里写图片描述](https://img-blog.csdn.net/20180519163330289?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ Toast
![这里写图片描述](https://img-blog.csdn.net/20180519163407424?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

如上图所示，我们就可以看到自定义规则出来的代码问题。请大家逐步修改检测出来的问题，后期的代码编译会将lint检测出来的问题作为一个指标。


## 6.4 添加自定义的Lint检测插件
上一个方法是要自己手动的添加Lint检测的AAR包到项目中，但是aar虽然很方便，但是在团队内部推广中我们遇到了以下问题：

 1. 配置繁琐，不易推广。每个库都需要自行配置lint.xml、lintOptions，并且compile aar。
 2. 不易统一。各库之间需要使用相同的配置，保证代码质量。但现在手动来回拷贝规则，且配置文件可以自己修改。

于是我开发一个plugin，统一管理lint.xml和lintOptions，自动添加aar,下面是介绍如何添加该Plugin

![这里写图片描述](https://img-blog.csdn.net/20180519163609374?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 6.4.1 在根目录下的在build.gradle 中，添加Lint检测插件
![这里写图片描述](https://img-blog.csdn.net/20180519163803680?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

如上图所示，在 buildscript 中的 dependencies 块中，添加Lint检测的插件地址，

```
//lint 检测插件
classpath 'com.xtc.lint:lint-check-plugin:1.0.7-Dev'
```
### 6.4.2 在module的build.gradle中，应用Lint检测插件

![这里写图片描述](https://img-blog.csdn.net/20180519163903497?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

如上图所示，添加如下代码即可

```
apply plugin: 'XTCLintPlugin'
```

并且把之前已经添加过的lint检测aar包代码去掉，

![这里写图片描述](https://img-blog.csdn.net/20180519163937543?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 6.4.3 运行命令 gradlew lintForXTC 即可正常应用插件统一配置的lint

#### 6.4.3.1 在Android Studio中

```
gradlew clean lintForXTC
```

![这里写图片描述](https://img-blog.csdn.net/20180519164139265?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

这样就可以使用 插件统一配置的lint，进行静态代码分析了。

![这里写图片描述](https://img-blog.csdn.net/2018051916420036?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

编译完后的lint报告，位置位于：lint-report目录下，如下所示：

![这里写图片描述](https://img-blog.csdn.net/20180519164221139?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

#### 6.4.3.1  在Jenkins中

构建
在Jenkins中，修改下 构建的 任务 为

```
clean lintForXTC build --stacktrace
```

![这里写图片描述](https://img-blog.csdn.net/2018051916503633?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

侯建后的操作

1、修改 Publish Android Lint results
![这里写图片描述](https://img-blog.csdn.net/20180519165102344?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

修改为 watch/lint-report/lint-results*.xml

2、修改 FTP Publishers
增加一项，将lint检测的html报告保存到ftp路径
![这里写图片描述](https://img-blog.csdn.net/20180519165152974?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

Source files 路径填写

```
 watch/lint-report/lint-results.html
```

 
Remote directory 远程保存路径填写

```
'Android/Common/${JOB_NAME}/'yyyy-MM-dd-HH-mm-ss-'build-${BUILD_NUMBER}-git-${GIT_COMMIT}'
```

构建过程，lint检测过程中

![这里写图片描述](https://img-blog.csdn.net/20180519165257361?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

构建后的结果
编译完后，位置位于：lint-report目录下，如下所示：

![这里写图片描述](https://img-blog.csdn.net/201805191653197?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

保存到FTP的报告如下

![这里写图片描述](https://img-blog.csdn.net/201805191653425?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

可以将html文件 下载到本地然后使用浏览器打开，如下所示：

![这里写图片描述](https://img-blog.csdn.net/2018051916540855?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

点击【Lint Issues】可以查看Jenkins插件的Lint报告

![这里写图片描述](https://img-blog.csdn.net/20180519165433757?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDQ2MjgyNDEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

查看具体细节的issue和上面介绍的一样。



# 七、参考文献

+ https://tech.meituan.com/android_custom_lint.html
+ https://tech.meituan.com/android_custom_lint2.html
+ https://github.com/GavinCT/MeituanLintDemo
+ http://blog.csdn.net/u010360371/article/details/50189171
+ https://yq.aliyun.com/articles/6918
+ http://www.androidchina.net/5106.html
+ https://engineering.linkedin.com/android/writing-custom-lint-checks-gradle
+ https://github.com/yongce/AndroidDevNotes/blob/master/notes/knowledge/0005-custom-lint.asc
+ https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.LintOptions.html#com.android.build.gradle.internal.dsl.LintOptions:quiet
+ http://blog.csdn.net/itfootball/article/details/49277207
+ https://testerhome.com/topics/3105
+ http://blog.csdn.net/hwhua1986/article/details/50067089
+ http://www.jianshu.com/p/761c88d095a2



------


![这里写图片描述](https://img-blog.csdn.net/20150708201910089)

>作者：欧阳鹏 欢迎转载，与人分享是进步的源泉！ 
转载请保留原文地址：https://blog.csdn.net/ouyang_peng/article/details/80374867

>如果觉得本文对您有所帮助，欢迎您扫码下图所示的支付宝和微信支付二维码对本文进行随意打赏。您的支持将鼓励我继续创作！

![这里写图片描述](https://img-blog.csdn.net/20170413233715262?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvb3V5YW5nX3Blbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

































