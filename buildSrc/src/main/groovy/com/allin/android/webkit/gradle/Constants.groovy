package com.allin.android.webkit.gradle

import com.android.SdkConstants

class Constants {
    static final String TRANSFORM_NAME = 'AWebkit'

    static final String AWEBKIT_CLASS_PACKAGE_NAME = 'com/allin/android/webkit/generate/'

    static final String GENERATE_TO_CLASS_NAME = 'com/allin/android/webkit/internal/AWebkitGeneratedClassesHelper'

    static final String GENERATE_TO_CLASS_FILE_NAME = GENERATE_TO_CLASS_NAME + SdkConstants.DOT_CLASS

    static final String[] AWEBKIT_CORE_INTERFACES = [
            'com/allin/android/webkit/api/LifecycleRegistrant',
            'com/allin/android/webkit/api/JavascriptApiCollector'
    ]
}