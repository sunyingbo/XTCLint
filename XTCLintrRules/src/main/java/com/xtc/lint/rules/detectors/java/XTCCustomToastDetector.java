package com.xtc.lint.rules.detectors.java;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
/**
 * 定义代码检查规则
 * 这个是针对代码中直接使用android.widget.Toast类进行Toast显示的一个判断
 * </p>
 * created by OuyangPeng at 2017/8/31 10:55
 */
public class XTCCustomToastDetector extends Detector implements Detector.JavaScanner {
    private static final Class<? extends Detector> DETECTOR_CLASS = XTCCustomToastDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_ToastUseError";
    private static final String ISSUE_DESCRIPTION = "警告:你应该使用我们团队自定义的Toast工具类{com.xtc.widget.phone.toast.ToastUtil}";
    private static final String ISSUE_EXPLANATION = "你不能直接使用Toast，你应该使用我们团队自定义的Toast工具类{com.xtc.widget.phone.toast.ToastUtil}";
    private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;
    private static final String CHECK_CODE = "Toast";
    private static final String CHECK_PACKAGE = "android.widget.Toast";

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
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.singletonList(MethodInvocation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(final JavaContext context) {
        return new CusTomToastVisitor(context);
    }

    private class CusTomToastVisitor extends ForwardingAstVisitor {
        private final JavaContext javaContext;

        private CusTomToastVisitor(JavaContext context) {
            javaContext = context;
        }

        @Override
        public boolean visitMethodInvocation(MethodInvocation node) {

            //以Toast开头的不一定是直接使用了 android.widget.Toast 所以不检测

//            if (node.toString().startsWith(CHECK_CODE)) {
//                javaContext.report(ISSUE, node, javaContext.getLocation(node),
//                        ISSUE_DESCRIPTION);
//                return true;
//            }

            JavaParser.ResolvedNode resolve = javaContext.resolve(node);
            if (resolve instanceof JavaParser.ResolvedMethod) {
                JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolve;
                JavaParser.ResolvedClass containingClass = method.getContainingClass();
                if (containingClass.matches(CHECK_PACKAGE)) {
//                    System.out.println("CusTomToastVisitor visitMethodInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                    String message = ISSUE_DESCRIPTION + " ,请速度修改";
                    javaContext.report(ISSUE, node, javaContext.getLocation(node),message);
                    return true;
                }
            }

            return super.visitMethodInvocation(node);
        }
    }
}
