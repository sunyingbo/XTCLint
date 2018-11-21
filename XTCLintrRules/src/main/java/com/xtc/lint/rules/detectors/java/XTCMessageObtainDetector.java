package com.xtc.lint.rules.detectors.java;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.xtc.lint.rules.JavaPackageRelativePersonUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ConstructorInvocation;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;
/**
 * 定义代码检查规则
 * 这个是针对代码中直接使用 new Message()的一个判断
 * </p>
 * created by OuyangPeng at 2017/8/31 10:56
 */
public class XTCMessageObtainDetector extends Detector implements Detector.JavaScanner {
    private static final Class<? extends Detector> DETECTOR_CLASS = XTCMessageObtainDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_MessageObtainUseError";
    private static final String ISSUE_DESCRIPTION = "警告:你应该使用 {handler.obtainMessage()方法} 或者 {Message.obtain()方法}";
    private static final String ISSUE_EXPLANATION = "警告:为了减少内存开销，你不应该直接使用{new Message()} ，你应该使用 {handler.obtainMessage()方法} 或者 {Message.obtain()方法}，" +
            "这样的话，从整个Message池中返回一个新的Message实例，从而能够避免重复Message创建对象，减少内存开销，更多讲解可以参考链接：http://blog.csdn.net/dfskhgalshgkajghljgh/article/details/52672115";

    private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;
    private static final String PACKAGE_NAME = "android.os.Message";

    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    ).addMoreInfo("http://blog.csdn.net/dfskhgalshgkajghljgh/article/details/52672115");

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.singletonList(ConstructorInvocation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new MessageObtainVisitor(context);
    }

    private class MessageObtainVisitor extends ForwardingAstVisitor {
        private final JavaContext javaContext;

        private MessageObtainVisitor(JavaContext context) {
            javaContext = context;
        }


        @Override
        public boolean visitConstructorInvocation(ConstructorInvocation node) {
            JavaParser.ResolvedNode resolvedType = javaContext.resolve(node.astTypeReference());
            JavaParser.ResolvedClass resolvedClass = (JavaParser.ResolvedClass) resolvedType;
            if (resolvedClass != null && resolvedClass.isSubclassOf(PACKAGE_NAME, false)) {
                String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(javaContext,node);
//                System.out.println("XTCMessageObtainDetector visitConstructorInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                javaContext.report(ISSUE, node, javaContext.getLocation(node),message);
                return true;
            }
            return super.visitConstructorInvocation(node);
        }
    }
}
