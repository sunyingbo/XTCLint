package com.xtc.lint.rules.detectors.java;

import com.android.annotations.NonNull;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LayoutDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Attr;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_CONTENT_DESCRIPTION;
import static com.android.SdkConstants.ATTR_HINT;
import static com.android.SdkConstants.ATTR_LABEL;
import static com.android.SdkConstants.ATTR_PROMPT;
import static com.android.SdkConstants.ATTR_TEXT;
import static com.android.SdkConstants.ATTR_TITLE;

/**
 * 定义代码检查规则
 * 这个是针对XML文件中的硬编码文本的一个判断
 * </p>
 * created by OuyangPeng at 2017/9/11 17:40
 */
public class XTCHardcodedValuesDetector extends LayoutDetector {

    private static final Class<? extends Detector> DETECTOR_CLASS = XTCHardcodedValuesDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.RESOURCE_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );
    private static final String ISSUE_ID = "XTC_HardcodedText";
    private static final String ISSUE_DESCRIPTION = "错误:你不能在XML布局文件中直接使用硬编码文本";
    private static final String ISSUE_EXPLANATION = "错误:你不能在XML布局文件中直接使用硬编码文本，在Android Studio 中可以使用 Ctrl + Enter 键快速将硬编码文本抽取到strings.xml文件中\n";

    private static final Category ISSUE_CATEGORY = Category.I18N;
    private static final int ISSUE_PRIORITY = 9;
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

    //Add additional issues here, such as hardcoded colors, hardcoded sizes, etc

    @NonNull
    @Override
    public Speed getSpeed() {
        return Speed.FAST;
    }

    @Override
    public Collection<String> getApplicableAttributes() {
        return Arrays.asList(
                // Layouts
                ATTR_TEXT,
                ATTR_CONTENT_DESCRIPTION,
                ATTR_HINT,
                ATTR_LABEL,
                ATTR_PROMPT,

                // Menus
                ATTR_TITLE
        );
    }

    @Override
    public boolean appliesTo(@NonNull ResourceFolderType folderType) {
        return folderType == ResourceFolderType.LAYOUT || folderType == ResourceFolderType.MENU;
    }

    @Override
    public void visitAttribute(@NonNull XmlContext context, @NonNull Attr attribute) {
        String value = attribute.getValue();
        //如果不是使用 @ 或者 ？ 开头来引用文本资源的
        if (!value.isEmpty() && (value.charAt(0) != '@' && value.charAt(0) != '?')) {
            // Make sure this is really one of the android: attributes
            if (!ANDROID_URI.equals(attribute.getNamespaceURI())) {
                return;
            }
            String message = String.format(
                    Locale.US,
                    "[I18N] 硬编码文本为 \"%1$s\", 你应该使用 `@string` 引用 strings.xml中的文本资源",
                    value);
            context.report(ISSUE, attribute, context.getLocation(attribute),ISSUE_DESCRIPTION + message);
        }
    }
}
