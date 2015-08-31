package com.shopgun.android.sdk.utils;

/**
 * Version class is used for easier migration of various features in the SDK.<br/><br/>
 *
 * The general design of Version is based on Semantic Versioning (http://semver.org/).<br/><br/>
 *
 * The version is stored as follows: major*MASK_MAJOR + minor*MASK_MINOR + patch
 *
 */
public class Version {

    public static final int MASK_MAJOR = 100000;
    public static final int MASK_MINOR = 1000;

    private final int mVersion;

    public Version(int version) {
        mVersion = version;
    }

    public Version(int major, int minor, int patch) {
        this(major*MASK_MAJOR+minor*MASK_MINOR+patch);
    }

    public int getCode() {
        return mVersion;
    }

    public int getMajor() {
        return mVersion/MASK_MAJOR;
    }

    public int getMinor() {
        return mVersion-(getMajor()*MASK_MAJOR)/MASK_MINOR;
    }

    public int getPatch() {
        return mVersion-(getMajor()*MASK_MAJOR)-(getMinor()*MASK_MINOR);
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", getMajor(), getMinor(), getPatch());
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
