/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.Expression;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.StrictListAccessor;
/**
 * 定义代码检查规则
 * 这个是针对activity和fragment的布局文件的文件名前缀进行一个判断
 *
 * activity的布局文件前缀应该为 activity_
 * fragment的布局文件前缀应该为 fragment_
 *
 * </p>
 * created by OuyangPeng at 2017/8/31 10:22
 */
public class XTCActivityFragmentLayoutNameDetector extends Detector
        implements Detector.JavaScanner {
    private static final Class<? extends Detector> DETECTOR_CLASS = XTCActivityFragmentLayoutNameDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ACTIVITY_ID = "XTC_LayoutNamePrefixError";
    private static final String ISSUE_ACTIVITY_DESCRIPTION = "警告:你应该使用{activity_}作为activity的布局文件名的前缀";
    private static final String ISSUE_ACTIVITY_EXPLANATION = "警告:你应该使用{activity_}作为activity的布局文件名的前缀。例如：你可以命名为 {activity_function.xml}";


    private static final String ISSUE_FRAGMENT_ID = "XTC_LayoutNamePrefixError";
    private static final String ISSUE_FRAGMENT_DESCRIPTION = "警告:你应该使用{fragment_}作为fragment的布局文件名的前缀";
    private static final String ISSUE_FRAGMENT_EXPLANATION = "警告:你应该使用{fragment_}作为fragment的布局文件名的前缀。例如：你可以命名为 {fragment_function.xml}";


    private static final Category ISSUE_CATEGORY = Category.MESSAGES;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;

    public static final Issue ACTIVITY_LAYOUT_NAME_ISSUE = Issue.create(
            ISSUE_ACTIVITY_ID,
            ISSUE_ACTIVITY_DESCRIPTION,
            ISSUE_ACTIVITY_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    );

    public static final Issue FRAGMENT_LAYOUT_NAME_ISSUE = Issue.create(
            ISSUE_FRAGMENT_ID,
            ISSUE_FRAGMENT_DESCRIPTION,
            ISSUE_FRAGMENT_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    );

    /**
     * 只关心 名为 setContentView 和  inflate 的方法
     */
    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("setContentView", "inflate");
    }

    @Override
    public void visitMethod(@NonNull JavaContext context, AstVisitor visitor, @NonNull MethodInvocation node) {
        String methodName = node.astName().astValue();
        //如果方法名为 setContentView
        if (methodName.equals("setContentView")) {//Lint check for Activity layout file name
            lintCheckMethodSetContentViewInActivity(context, node);
        } else if (methodName.equals("inflate")) {//Lint check for Fragment layout file name
            lintCheckMethodInflateInFragment(context, node);
        }
    }

    /**
     *  检测Activity中的setContentView中的参数是否为activity_前缀
     */
    private void lintCheckMethodSetContentViewInActivity(@NonNull JavaContext context, @NonNull MethodInvocation node) {
        if (isSetContentViewOnThis_ForActivity(node)
                && isThisInstanceOfActivity_ForActivity(context, node)
                && isThisMethodHasLayoutAnnotation_ForActivity(context, node)) {

            Iterator<Expression> arguments = node.astArguments().iterator();
            Expression argument = arguments.next();

            if (argument == null) {
                System.err.println("Custom Lint Error:Some thing went wrong in XTCActivityFragmentLayoutNameDetector.visitMethod:\n\targument of setContentView is null");
                return;
            }

            String layoutString = argument.toString();
            if (!isFileStringStartWithPrefix(layoutString, "activity_")) {
                String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(context,node);
//                System.out.println("lintCheckMethodSetContentViewInActivity () 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_ACTIVITY_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                context.report(ACTIVITY_LAYOUT_NAME_ISSUE,
                        node,
                        context.getLocation(node),
                        message);
            }
        }
    }
    /**
     *  检测Fragment中的inflate中的参数是否为fragment_前缀
     */
    private void lintCheckMethodInflateInFragment(@NonNull JavaContext context, @NonNull MethodInvocation node) {
        if (isInflateCalledInOnCreateView_ForFragment(context, node)) {
            String layoutString = getParamWithLayoutAnnotation_ForFragment(context, node);

            if (layoutString == null) {
                return;
            }

            if (!isFileStringStartWithPrefix(layoutString, "fragment_")) {
                String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(context,node);
//                System.out.println("lintCheckMethodInflateInFragment () 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_FRAGMENT_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                context.report(FRAGMENT_LAYOUT_NAME_ISSUE,
                        node,
                        context.getLocation(node),
                        message);
            }
        }
    }



    /**
     * If user make this call "setContentView(R.layout.activity_main)" without an explict instance, return true.
     * Else if "this.setContentView(R.layout.activity_main)", then return true.
     * Else if "someobj.setContentView(R.layout.activity_main)", return false.
     */
    private boolean isSetContentViewOnThis_ForActivity(@NonNull MethodInvocation node) {
        StrictListAccessor<Expression, MethodInvocation> args = node.astArguments();
        String argOwner = args.owner().toString();
        return argOwner.startsWith("setContentView(")
                || argOwner.startsWith("this.setContentView(");
    }

    /**
     * If setContentView is called by 'this' instance,
     * this method will check if 'this' is an instance of a Class inherit from android.app.Activity, for eaxmple AppCompatActivity or FragmentActivity, and so on.
     */
    private boolean isThisInstanceOfActivity_ForActivity(@NonNull JavaContext context, @NonNull MethodInvocation node) {
//      Node currentNode = node.getParent();
        JavaParser.ResolvedNode resolved = context.resolve(JavaContext.findSurroundingClass(node));
        JavaParser.ResolvedClass surroundingClass = (JavaParser.ResolvedClass) resolved;
        while (surroundingClass != null) {
//          System.out.println("surroundingClass = " + surroundingClass);
            if ("android.app.Activity".equals(surroundingClass.getName())) {
                return true;
            } else {
                surroundingClass = surroundingClass.getSuperClass();
            }
        }
        return false;
    }

    /**
     * As there are more than one methods overload "setContentView",
     * we have to identify the one we want to check, whose param has an Annotation of "@LayoutRes".
     * In fact, {public void setContentView(@LayoutRes int layoutResID)} is the one we are looking for.
     */
    private boolean isThisMethodHasLayoutAnnotation_ForActivity(@NonNull JavaContext context, @NonNull MethodInvocation node) {
        JavaParser.ResolvedNode resolved = context.resolve(node);
        JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolved;

        if (node.astArguments().size() != 1) {
            return false;
        }

        Iterable<JavaParser.ResolvedAnnotation> annotations = method.getParameterAnnotations(0);
        for (JavaParser.ResolvedAnnotation annotation : annotations) {
            if ("android.support.annotation.LayoutRes".equals(annotation.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * inflater.inflate() can be called from anywhere.
     * We only care about the one call in {public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)}.
     * This method whill check that.
     */
    private boolean isInflateCalledInOnCreateView_ForFragment(@NonNull JavaContext context, @NonNull MethodInvocation node) {
        Node surroundingNode = JavaContext.findSurroundingMethod(node);
        JavaParser.ResolvedNode resolvedNode = context.resolve(surroundingNode);
        try {
            String resolvedNodeName = resolvedNode.getName();
            if ("onCreateView".equals(resolvedNodeName)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * We get the layout file resource name, for example "R.layout.fragment_blank".
     * This method will check if it starts with the given prefix.
     *
     * @param layoutFileResourceString layout resource file name, like "R.layout.fragment_blank"
     * @param prefix                   the given prefix, must be "activity_" or "fragment)"
     * @return "true" if layoutFileResourceString starts with prefix, "false" otherwise.
     */
    private boolean isFileStringStartWithPrefix(String layoutFileResourceString, String prefix) {
        int lastDotIndex = layoutFileResourceString.lastIndexOf(".");
        String fileName = layoutFileResourceString.substring(lastDotIndex + 1);
        return fileName.startsWith(prefix);
    }

    /**
     * There are more than one methods overloading in the name of "inflate()" in android.view.LayoutInflater.<br>
     * We only care about those having an param with `@LayoutRes` annotation,
     * for example {public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot)}.<br>
     * This method will find out the resource param with an `@LayoutRes` annotation in String format, for example `R.layout.fragment_blank` .<br>
     * If no such param exists, <B>null</B> will be returned.
     */
    private String getParamWithLayoutAnnotation_ForFragment(@NonNull JavaContext context, @NonNull MethodInvocation node) {
        Iterator<Expression> arguments = node.astArguments().iterator();
        Expression argument = arguments.next();

        JavaParser.ResolvedNode resolved = context.resolve(node);
        JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolved;

        JavaParser.ResolvedAnnotation layoutParamAnnotation = method.getParameterAnnotation("android.support.annotation.LayoutRes", 0);
        if (layoutParamAnnotation != null) {
            return argument.toString();
        } else {
            return null;
        }

    }


}