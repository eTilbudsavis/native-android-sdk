package com.shopgun.android.sdk.utils;

public class Version {

    //versionMajor * 1000000 + versionMinor * 10000 + versionPatch * 100 + versionBuild

    public static final String TAG = Version.class.getSimpleName();

    public static final int MASK_MAJOR = 1000000;
    public static final int MASK_MINOR = 10000;
    public static final int MASK_PATCH = 100;

    private final int mVersion;
    private final String mBuild;

    /**
     * 1000000 == 1.00.00.00 = v1.0.0
     * 1020304 == 1.02.03.04 = v1.2.3
     * @param version
     */
    public Version(int version) {
        this(version, null);
    }

    public Version(int version, String build) {
        mVersion = version;
        mBuild = build;
    }

    public Version(int major, int minor, int patch, String build) {
        this(buildVersion(major, minor, patch), build);
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public Version(int major, int minor, int patch, int build) {
        this(major, minor, patch, String.valueOf(build));
    }

    private static int buildVersion(int major, int minor, int patch) {
        return (major*MASK_MAJOR) + (minor*MASK_MINOR) + (patch*MASK_PATCH);
    }

    public int getCode() {
        return mVersion;
    }

    public int getMajor() {
        return mVersion/MASK_MAJOR;
    }

    public int getMinor() {
        return (mVersion%MASK_MAJOR)/MASK_MINOR;
    }

    public int getPatch() {
        return (mVersion%MASK_MINOR)/MASK_PATCH;
    }

    public String getBuild() {
        return mBuild;
    }

    public String getName() {
        if (mBuild != null) {
            return String.format("%s.%s.%s-%s", getMajor(), getMinor(), getPatch(), mBuild);
        } else {
            return String.format("%s.%s.%s", getMajor(), getMinor(), getPatch());
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;
        return mVersion == version.mVersion;
    }

    @Override
    public int hashCode() {
        return mVersion;
    }


}
