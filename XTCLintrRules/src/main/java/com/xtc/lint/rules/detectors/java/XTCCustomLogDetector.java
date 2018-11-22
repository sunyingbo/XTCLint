package com.xtc.lint.rules.detectors.java;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

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
//                System.out.println("LogVisit visitMethodInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_DESCRIPTION + " ,请速度修改";
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

//                        System.out.println("LogVisit visitMethodInvocation() 出现lint检测项，对应的责任人为： " + relativePersonName);
                        String message = ISSUE_DESCRIPTION + " ,请速度修改";
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
