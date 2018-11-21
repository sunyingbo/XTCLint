package com.xtc.lint.rules.detectors.java;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.ast.AstVisitor;
import lombok.ast.BinaryExpression;
import lombok.ast.ConstructorInvocation;
import lombok.ast.Expression;
import lombok.ast.ExpressionStatement;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;
import lombok.ast.StrictListAccessor;
import lombok.ast.TypeReference;
import lombok.ast.VariableDefinition;

/**
 * 针对 JDK7 新语法的加强检测
 * http://docs.oracle.com/javase/7/docs/technotes/guides/language/type-inference-generic-instance-creation.html
 * </p>
 * created by OuyangPeng at 2017/9/5 16:04
 */
public class XTCHashMapForJDK7Detector extends Detector implements Detector.JavaScanner {

    private static final Class<? extends Detector> DETECTOR_CLASS = XTCHashMapForJDK7Detector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_HashMapForJDK7";
    private static final String ISSUE_DESCRIPTION = "警告:为了更好的性能，请使用 {SparseArray} 来替代 {HashMap}";
    private static final String ISSUE_EXPLANATION = "如果map类型的key值为 Integer类型，使用Android 特有的API `SparseArray`。" +
            "这个检查确定的情况下，为了更好的性能，你可能要考虑使用`SparseArray`代替`HashMap`。" +
            "当你的key类型是int等原始类型的时候，你可以使用`SparseIntArray`来避免自动装箱将`int` 转换为 `Integer`。";

    private static final Category ISSUE_CATEGORY = Category.PERFORMANCE;
    private static final int ISSUE_PRIORITY = 4;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;

    /** Using HashMaps where SparseArray would be better */
    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY ,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    ).addMoreInfo("http://blog.csdn.net/u010687392/article/details/47809295").addMoreInfo("http://www.cnblogs.com/CoolRandy/p/4547904.html");

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.<Class<? extends Node>>singletonList(ConstructorInvocation.class);
    }

    private static final String INTEGER = "Integer";
    private static final String BOOLEAN = "Boolean";
    private static final String BYTE = "Byte";
    private static final String LONG = "Long";
    private static final String HASH_MAP = "HashMap";

    private static final Pattern PATTERN = Pattern.compile(".*<(.*),(.*)>");

    @Override
    public AstVisitor createJavaVisitor(final @NonNull JavaContext context) {
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitConstructorInvocation(ConstructorInvocation node) {
                TypeReference reference = node.astTypeReference();
                String typeName = reference.astParts().last().astIdentifier().astValue();
                // TODO: Should we handle factory method constructions of HashMaps as well,
                // e.g. via Guava? This is a bit trickier since we need to infer the type
                // arguments from the calling context.
                if (typeName.equals(HASH_MAP)) {
                    checkHashMap(context, node, reference);
                }
                return super.visitConstructorInvocation(node);
            }
        };
    }

    /**
     * Checks whether the given constructor call and type reference refers
     * to a HashMap constructor call that is eligible for replacement by a
     * SparseArray call instead
     */
    private void checkHashMap(JavaContext context, ConstructorInvocation node, TypeReference reference) {
        StrictListAccessor<TypeReference, TypeReference> types = reference.getTypeArguments();
        if (types == null || types.size() != 2) {
            /*
            JDK7 新写法
            HashMap<Integer, String> map2 = new HashMap<>();
            map2.put(1, "name");
            Map<Integer, String> map3 = new HashMap<>();
            map3.put(1, "name");
             */
            Node result = node.getParent().getParent();
            if (result instanceof VariableDefinition) {
                TypeReference typeReference = ((VariableDefinition) result).astTypeReference();
                checkCore(context, result, typeReference);
                return;
            }

            if (result instanceof ExpressionStatement) {
                Expression  expression = ((ExpressionStatement) result).astExpression();
                if (expression instanceof BinaryExpression) {
                    Expression left = ((BinaryExpression) expression).astLeft();
                    String fullTypeName = context.getType(left).getName();
                    checkCore2(context, result, fullTypeName);
                }
            }
        }
        // else --> lint本身已经检测
    }

    private void checkCore2(JavaContext context, Node node, String fullTypeName) {
        Matcher m = PATTERN.matcher(fullTypeName);
        if (m.find()) {
            String typeName = m.group(1).trim();
            String valueType = m.group(2).trim();
            int minSdk = context.getMainProject().getMinSdk();

            String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(context,node);
//            System.out.println("XTCHashMapForJDK7Detector checkCore2() 出现lint检测项，对应的责任人为： " + relativePersonName);
            String appendMessage = " ,请 【" + relativePersonName + "】速度修改";
            if (typeName.equals(INTEGER) || typeName.equals(BYTE)) {
                if (valueType.equals(INTEGER)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseIntArray(...) } 来替代 {HashMap}" + appendMessage);
                } else if (valueType.equals(LONG) && minSdk >= 18) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseLongArray(...) } 来替代 {HashMap}" + appendMessage);
                } else if (valueType.equals(BOOLEAN)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseBooleanArray(...) } 来替代 {HashMap}" + appendMessage);
                } else {
                    String message =  String.format(
                            "为了更好的性能，请使用 {SparseArray<%1$s>(...) } 来替代 {HashMap}",
                            valueType);
                    context.report(ISSUE, node, context.getLocation(node),message + appendMessage);
                }
            } else if (typeName.equals(LONG) && (minSdk >= 16 ||
                    Boolean.TRUE.equals( context.getMainProject().dependsOn(
                            SdkConstants.SUPPORT_LIB_ARTIFACT)))) {
                boolean useBuiltin = minSdk >= 16;
                String message = useBuiltin ?
                        "为了更好的性能，请使用 {LongSparseArray(...) } 来替代 {HashMap}" + appendMessage:
                        "为了更好的性能，请使用 {android.support.v4.util.LongSparseArray(...) } 来替代 {HashMap}" + appendMessage;
                context.report(ISSUE, node, context.getLocation(node),
                        message);
            }
        }
    }

    /**
     * copy from lint source code
     */
    private void checkCore(JavaContext context, Node node, TypeReference reference) {
        // reference.hasTypeArguments returns false where it should not
        StrictListAccessor<TypeReference, TypeReference> types = reference.getTypeArguments();
        if (types != null && types.size() == 2) {
            TypeReference first = types.first();
            String typeName = first.getTypeName();
            int minSdk = context.getMainProject().getMinSdk();

            String relativePersonName = JavaPackageRelativePersonUtil.getPackageRelativePerson(context,node);
//            System.out.println("XTCHashMapForJDK7Detector checkCore() 出现lint检测项，对应的责任人为： " + relativePersonName);
            String appendMessage = " ,请 【" + relativePersonName + "】速度修改";

            if (typeName.equals(INTEGER) || typeName.equals(BYTE)) {
                String valueType = types.last().getTypeName();
                if (valueType.equals(INTEGER)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseIntArray(...) } 来替代 {HashMap}" + appendMessage);
                } else if (valueType.equals(LONG) && minSdk >= 18) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseLongArray(...) } 来替代 {HashMap}"+ appendMessage);
                } else if (valueType.equals(BOOLEAN)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseBooleanArray(...) } 来替代 {HashMap}"+ appendMessage);
                } else {
                    String message =  String.format(
                            "为了更好的性能，请使用 {SparseArray<%1$s>(...) } 来替代 {HashMap}",
                            valueType);
                    context.report(ISSUE, node, context.getLocation(node),message + appendMessage);
                }
            } else if (typeName.equals(LONG) && (minSdk >= 16 ||
                    Boolean.TRUE.equals(context.getMainProject().dependsOn(
                            SdkConstants.SUPPORT_LIB_ARTIFACT)))) {
                boolean useBuiltin = minSdk >= 16;
                String message = useBuiltin ?
                        "为了更好的性能，请使用 {LongSparseArray(...) } 来替代 {HashMap}"  + appendMessage:
                        "为了更好的性能，请使用 {android.support.v4.util.LongSparseArray(...) } 来替代 {HashMap}" + appendMessage;
                context.report(ISSUE, node, context.getLocation(node), message);
            }
        }
    }
}