package com.xtc.lint.rules.detectors.java;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;

import lombok.ast.Node;

import lombok.ast.StringLiteral;

/**
 * 定义代码检查规则
 * 这个是针对java代码中的中文硬编码进行一个判断
 * 由于要对java代码进行扫描,因此继承的是JavaScanner的接口
 * </p>
 * created by OuyangPeng at 2017/8/31 9:42
 */
public class XTCChineseStringDetector extends Detector implements Detector.JavaScanner {

	private static final Class<? extends Detector> DETECTOR_CLASS = XTCChineseStringDetector.class;
	private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
	private static final Implementation IMPLEMENTATION = new Implementation(
			DETECTOR_CLASS,
			DETECTOR_SCOPE
	);

    private static final String ISSUE_ID = "XTC_JavaChineseString";
    private static final String ISSUE_DESCRIPTION = "错误：除了Log日志打印之外，不能在java文件中使用中文字符串硬编码";
    private static final String ISSUE_EXPLANATION = "错误：除了Log日志打印之外，不能在java文件中使用中文字符串硬编码";
	private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
	private static final int ISSUE_PRIORITY = 5;
	private static final Severity ISSUE_SEVERITY = Severity.ERROR;

	public static final Issue ISSUE = Issue.create(
			ISSUE_ID,
			ISSUE_DESCRIPTION,
			ISSUE_EXPLANATION,
			ISSUE_CATEGORY,
			ISSUE_PRIORITY,
			ISSUE_SEVERITY,
			IMPLEMENTATION
	);


    private static final String LOG_CLASS = "android.util.Log";
    private static final String LOGUTIL_CLASS = "com.xtc.log.LogUtil";

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return true;
    }

    @NonNull
    @Override
    public Speed getSpeed() {
        return Speed.FAST;
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.<Class<? extends Node>>singletonList(StringLiteral.class);
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new XTCChineseStringDetector.StringChecker(context);
    }

    private static class StringChecker extends ForwardingAstVisitor {
        private final JavaContext mContext;

        public StringChecker(JavaContext context) {
            mContext = context;
        }

        @Override
        public boolean visitStringLiteral(StringLiteral stringLiteral) {
            String astValue = stringLiteral.astValue();
            if (astValue.isEmpty()) {
                return false;
            }
            String patternStr = "[\\u4e00-\\u9fa5]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(astValue);
            //匹配到了中文字符
            if (matcher.find()) {
                Node node = stringLiteral.getParent();
                JavaParser.ResolvedNode resolve =  mContext.resolve(node);
//                System.out.println("=======================出现中文：" + astValue + " ,resolve = " + resolve);
                if (resolve == null){
//                    System.out.println("=======================出现中文：" + astValue + " ,resolve == null,现在进入循环判断");
                    while (resolve == null){
                        node = node.getParent();
                        resolve =  mContext.resolve(node);
//                        System.out.println("=======================出现中文：" + astValue + " ,循环判断中,resolve = " + resolve);
                        if (resolve != null) {
                            checkIfInLogPrint(stringLiteral, astValue, (JavaParser.ResolvedMethod) resolve);
                            break;
                        }
                    }
                }else {
                    checkIfInLogPrint(stringLiteral, astValue, (JavaParser.ResolvedMethod) resolve);
                }
            }
            return false;
        }

        /**
         * 判断是否处于Log打印中的中文
         * @param stringLiteral
         * @param astValue
         * @param resolve
         */
        private void checkIfInLogPrint(StringLiteral stringLiteral, String astValue, JavaParser.ResolvedMethod resolve) {
            JavaParser.ResolvedMethod method = resolve;
            JavaParser.ResolvedClass containingClass = method.getContainingClass();
//            System.out.println("=======================出现中文======================= ， containingClass = " + containingClass.getName());

            //如果是Log或者LogUtil打印日志里面出现中文的话
            if (containingClass.matches(LOG_CLASS)
                    || containingClass.matches(LOGUTIL_CLASS)) {
//                System.out.println("Log或者LogUtil打印日志,出现中文不报告");
            } else{
                mContext.report(ISSUE, stringLiteral, mContext.getLocation(stringLiteral),
                        "不能在java文件中使用中文字符串硬编码,检测出来的字符串为:{  " + astValue + "  }");
            }
        }
    }
}
