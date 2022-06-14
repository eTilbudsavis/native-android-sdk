package com.tjek.sdk.publicationviewer.paged.zoomlayout;

public class ZoomOnDoubleTapListener implements ZoomLayoutInterface.OnDoubleTapListener {

    private boolean mThreeStep = false;

    public ZoomOnDoubleTapListener(boolean threeStep) {
        mThreeStep = threeStep;
    }

    @Override
    public boolean onDoubleTap(ZoomLayout view, TapInfo info) {
        try {
            if (mThreeStep) {
                threeStep(view, info.getAbsoluteX(), info.getAbsoluteY());
            } else {
                twoStep(view, info.getAbsoluteX(), info.getAbsoluteY());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }
        return true;
    }

    private void twoStep(ZoomLayout view, float x, float y) {
        if (view.getScale() > view.getMinScale()) {
            view.setScale(view.getMinScale(), true);
        } else {
            view.setScale(view.getMaxScale(), x, y, true);
        }
    }

    private void threeStep(ZoomLayout view, float x, float y) {
        float scale = view.getScale();
        float medium = view.getMinScale() + ((view.getMaxScale() - view.getMinScale()) * 0.3f);
        if (scale < medium) {
            view.setScale(medium, x, y, true);
        } else if (scale >= medium && scale < view.getMaxScale()) {
            view.setScale(view.getMaxScale(), x, y, true);
        } else {
            view.setScale(view.getMinScale(), true);
        }
    }

}
