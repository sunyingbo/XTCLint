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
/**
 * 定义代码检查规则
 * 这个是针对ViewHolder的布局文件的文件名前缀进行一个判断
 * ViewHolder的布局文件前缀应该为 item_
 * </p>
 * created by OuyangPeng at 2017/8/31 11:13
 */
public class XTCViewHolderItemNameDetector extends Detector implements Detector.JavaScanner {
    private static final Class<? extends Detector> DETECTOR_CLASS = XTCViewHolderItemNameDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_HolderItemNamePrefixError";
    private static final String ISSUE_DESCRIPTION = "警告:你应该使用{item_}作为ViewHolder的布局文件名的前缀";
    private static final String ISSUE_EXPLANATION = "警告:你应该使用{item_}作为ViewHolder的布局文件名的前缀。例如：你可以命名为 {item_function.xml}";

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

    /**
     * 只关心 名为  inflate 的方法
     */
    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("inflate");
    }

    @Override
    public void visitMethod(JavaContext context, AstVisitor visitor, MethodInvocation node) {

        JavaParser.ResolvedClass surroundingClass = (JavaParser.ResolvedClass)context.resolve(JavaContext.findSurroundingClass(node));
        JavaParser.ResolvedMethod surroundingMethod = (JavaParser.ResolvedMethod)context.resolve(JavaContext.findSurroundingMethod(node));

        if (surroundingMethod.getName().equals("onCreateViewHolder")
                && surroundingClass.isSubclassOf("android.support.v7.widget.RecyclerView.Adapter", false)){


            String layoutString = getParamWithLayoutAnnotation(context, node);
            if (layoutString == null){
                return;
            }

            if (!isFileStringStartWithPrefix(layoutString, "item_")) {
                String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(context,node);
//                System.out.println("XTCViewHolderItemNameDetector visitMethod() 出现lint检测项，对应的责任人为： " + relativePersonName);
                String message = ISSUE_DESCRIPTION + " ,请 【" + relativePersonName + "】速度修改";
                context.report(ISSUE,
                        node,
                        context.getLocation(node),
                        message);
            }

        }
    }


    /**
     * There are more than one methods overloading in the name of "inflate()" in android.view.LayoutInflater.<br>
     * We only care about those having an param with `@LayoutRes` annotation,
     * for example {public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot)}.<br>
     * This method will find out the resource param with an `@LayoutRes` annotation in String format, for example `R.layout.fragment_blank` .<br>
     * If no such param exists, <B>null</B> will be returned.
     */
    private String getParamWithLayoutAnnotation(@NonNull JavaContext context, @NonNull MethodInvocation node) {
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

    /**
     * We get the layout file resource name, for example "R.layout.fragment_blank".
     * This method will check if it starts with the given prefix.
     * @param layoutFileResourceString layout resource file name, like "R.layout.item_music_favorite"
     * @param prefix the given prefix, must be "item_"
     * @return "true" if layoutFileResourceString starts with prefix, "false" otherwise.
     */
    private boolean isFileStringStartWithPrefix(String layoutFileResourceString, String prefix){
        int lastDotIndex = layoutFileResourceString.lastIndexOf(".");
        String fileName = layoutFileResourceString.substring(lastDotIndex + 1);
        return fileName.startsWith(prefix);
    }
}
