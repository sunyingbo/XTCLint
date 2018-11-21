![](custom_lint.png)

# 1、添加自定义Lint检测的AAR

在项目的build.gradle 里面添加如下代码

```
compile 'com.xtc.lint:lint-check:1.0.5'
```

![](http://doc.stip.bbkedu.com/uploads/a42a29573dede35ee704b1a2011cfd5e.png)

目前版本为**1.0.1**，之后随着自定义规则越来越多，版本再逐步升高。

目前最新的可用版本为 **1.0.5**



# 2、执行lint检测命令

# 2.1 本地执行lint命令

执行如下lint检测命令

```
gradlew clean lint
```

开始执行命令

![](http://doc.stip.bbkedu.com/uploads/4d61f8d9cfae36aec2facfe1ac9d3b85.png)

开始Lint检测
![](http://doc.stip.bbkedu.com/uploads/f6f6ec033ccff9651d431b41ffb8ff63.png)


检测完毕

![](http://doc.stip.bbkedu.com/uploads/e6eaf47bbfb4c6801b3f7a4fd7a1fb9e.png)



# 2.2 Android Studio执行 Inspect Code
如果上面的命令你用的不习惯，你可以使用Android Studio 自带的代码检测

打开【Analyze】-->【Inspect Code】
![](http://doc.stip.bbkedu.com/uploads/676118dccb4511d42c04afd26d591c4e.png)

弹出如下所示的Scope对话框，你可以选择检测的范围
![](http://doc.stip.bbkedu.com/uploads/3856e02bde0905a2aa73ff1d485518f0.png)

选择完后，点击【OK】按钮即开始进行检测。

电话手表APP项目比较大，可能检测得几分钟，请等待。

![](http://doc.stip.bbkedu.com/uploads/9c09d56b8a29d48d956bb6b3934a4376.png)

检测完后，会出现上图所示的选项框，在Android Lint 下面可以看到我们自定义的Lint检测规则


![](http://doc.stip.bbkedu.com/uploads/430ff89d2157b84c4d36e6dab9bb85d4.png)

![](http://doc.stip.bbkedu.com/uploads/6ac41ccafc917b3e9ba6a11c7b5feb61.png)



# 2.3 Jenkins执行lint命令
修改Jenkins的编译命令为

```shell
clean lint build --stacktrace
```

![](http://doc.stip.bbkedu.com/uploads/87622c29c7d93a21c8580b37fdea4603.png)

编译完后，将lint报告归档到FTP
![](http://doc.stip.bbkedu.com/uploads/c10324d3c0e45e49e9bdae94eaba0d2b.png)

**Source files 填写**
```
watch/build/outputs/lint-results-debug.html,lint-results-debug.xml
```

改目录要根据自己项目实际输出的lint报告来填写，可以自己查看Jenkins的工作区生成的目录

![](http://doc.stip.bbkedu.com/uploads/f2e5de5344dc9c1645eb6fc501b55d5e.png)

![](http://doc.stip.bbkedu.com/uploads/8286a072d4ddfd3aa6266ce12f9d5e4b.png)




**Remote directory 填写**

```
'Android/Common/${JOB_NAME}/'yyyy-MM-dd-HH-mm-ss-'build-${BUILD_NUMBER}-git-${GIT_COMMIT}'
```

**然后点击【高级】选项，勾选上【Flatten files】和【Remote directory】**

![](http://doc.stip.bbkedu.com/uploads/737639bf1d9a61a5eba3d5fb7550e8ba.png)


这样编译完后，在FTP服务器对应的目录上就会有lint检测报告了

![](http://doc.stip.bbkedu.com/uploads/9de7d257e510e46f380d943e19d4d681.png)





# 3、查看Lint检测报告

## 3.1 lint报告输出目录
lint命令执行完毕之后，会输出相应的lint报告，不同的gradle版本输出的文档地址可能不同，电话手表APP的输出目录为：**\watch\build\outputs**

![](http://doc.stip.bbkedu.com/uploads/3af1c59e91c5901902a4a29aa2283697.png)


gradle版本高的，输出目录可能为  **\app\build\reports**,如下所示

![](http://doc.stip.bbkedu.com/uploads/225106229e9b0cdc598338f13820bbf3.png)

## 3.2 报告详情

打开 \watch\build\outputs\lint-results-debug.html 网页即可看到输出的lint检测报告。

![](http://doc.stip.bbkedu.com/uploads/c6b9af1c3386ceac7a53c3e10836bca5.png)



+ 控件命名
![](http://doc.stip.bbkedu.com/uploads/4ba77b413fa5c0a6844234383639979c.png)

+ 图片太大
![](http://doc.stip.bbkedu.com/uploads/be69152762383c71bd95efb7ef570d8d.png)

+ Log打印
![](http://doc.stip.bbkedu.com/uploads/621979aac6ced9d16eb0fc5fb4c3dd02.png)

+ Toast
![](http://doc.stip.bbkedu.com/uploads/f139b850121f9b219868e8f1efe0a63a.png)


如上图所示，我们就可以看到自定义规则出来的代码问题。请大家逐步修改检测出来的问题，后期的代码编译会将lint检测出来的问题作为一个指标。