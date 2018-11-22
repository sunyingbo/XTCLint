package com.xtc.lint.rules;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.xtc.lint.rules.detectors.binaryResource.XTCImageFileSizeDetector;
import com.xtc.lint.rules.detectors.java.XTCActivityFragmentLayoutNameDetector;
import com.xtc.lint.rules.detectors.java.XTCChineseStringDetector;
import com.xtc.lint.rules.detectors.java.XTCCustomLogDetector;
import com.xtc.lint.rules.detectors.java.XTCCloseDetector;
import com.xtc.lint.rules.detectors.java.XTCCustomToastDetector;
import com.xtc.lint.rules.detectors.java.XTCEnumDetector;
import com.xtc.lint.rules.detectors.java.XTCHardcodedValuesDetector;
import com.xtc.lint.rules.detectors.java.XTCHashMapForJDK7Detector;
import com.xtc.lint.rules.detectors.java.XTCMessageObtainDetector;
import com.xtc.lint.rules.detectors.java.XTCViewHolderItemNameDetector;
import com.xtc.lint.rules.detectors.xml.XTCViewIdNameDetector;

import java.util.Arrays;
import java.util.List;

public class XTCIssueRegister extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        System.out.println("***************************************************");
        System.out.println("**************** lint 开始静态分析代码 *****************");
        System.out.println("***************************************************");
        return Arrays.asList(
                XTCChineseStringDetector.ISSUE,
                XTCActivityFragmentLayoutNameDetector.ACTIVITY_LAYOUT_NAME_ISSUE,
                XTCActivityFragmentLayoutNameDetector.FRAGMENT_LAYOUT_NAME_ISSUE,
                XTCMessageObtainDetector.ISSUE,
                XTCCustomToastDetector.ISSUE,
                XTCCustomLogDetector.ISSUE,
                XTCViewIdNameDetector.ISSUE,
                XTCViewHolderItemNameDetector.ISSUE,
                XTCCloseDetector.ISSUE,
                XTCImageFileSizeDetector.ISSUE,
                XTCHashMapForJDK7Detector.ISSUE,
                XTCHardcodedValuesDetector.ISSUE,
                XTCEnumDetector.ISSUE
        );
    }
}
