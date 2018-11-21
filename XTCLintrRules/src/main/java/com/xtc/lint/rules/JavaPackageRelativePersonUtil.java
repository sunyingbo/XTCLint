package com.xtc.lint.rules;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;

import lombok.ast.Node;

/**
 * 找到对应的责任人
 * Created by OuyangPeng on 2017/9/8.
 */

public class JavaPackageRelativePersonUtil {
    /**
     * 通过方法名找到对应的类名所对应的包名，然后找到对应的责任人
     * @param context java上下文
     * @param node 方法节点
     * @return 对应的人责任
     */
    public static String  getPackageRelativePerson(@NonNull JavaContext context, @NonNull Node node) {
//        System.out.println("JavaPackageRelativePersonUtil getPackageRelativePerson ()");

        return "默认责任人";

//        String defaultRelativePerson = "默认责任人";
//
//        JavaParser.ResolvedNode resolved = context.resolve(JavaContext.findSurroundingClass(node));
//        JavaParser.ResolvedClass surroundingClass = (JavaParser.ResolvedClass) resolved;
//        if (surroundingClass != null) {
////            System.out.println("当前方法所对应的类名为 = " + surroundingClass.getName());
//            JavaParser.ResolvedPackage resolvedPackage = surroundingClass.getPackage();
//            String resolvedPackageName = resolvedPackage.getName();
//
//            //如果包名不为空，则去找对应的责任人
//            while (resolvedPackageName != null){
////                System.out.println("当前方法所对应的类的包名 = " + resolvedPackageName);
//                String relativePerson = Constants.loadPropertiesMap.get(resolvedPackageName);
//                //如果找不到对应的责任人，则去通过父包名去找对应的责任人
//                if (relativePerson == null || relativePerson.length() == 0) {
//                    //获取父包名
//                    JavaParser.ResolvedPackage parentResolvedPackage = resolvedPackage.getParentPackage();
//                    if (parentResolvedPackage != null){
//                        resolvedPackageName = parentResolvedPackage.getName();
////                        System.out.println("获得的责任人为空，现在通过父包名去找对应的责任人,父包名为：" + resolvedPackageName);
//                    } else {
//                        resolvedPackageName = null;
////                        System.out.println("获得的责任人为空，现在通过父包名去找对应的责任人, 找不到 父包名");
//                    }
//                } else {
////                    System.out.println("获得的责任人成功，责任人为 ：" + relativePerson);
//                    return  relativePerson;
//                }
//            }
//        }
//        return defaultRelativePerson;
    }
}
