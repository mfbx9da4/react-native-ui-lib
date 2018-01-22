package com.wix.reactnativeuilib.highlighterview;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

class HighlighterViewManager extends SimpleViewManager<HighlighterView> {
    private static final String REACT_CLASS = "HighlighterView";
    private static final int HighlightViewFrameExpand = 5;

    private ThemedReactContext context;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public HighlighterView createViewInstance(ThemedReactContext context) {
        this.context = context;
        return new HighlighterView(context);
    }

    @ReactProp(name = "highlightFrame")
    public void setHighlightFrame(HighlighterView view, ReadableMap highlightFrame) {
        view.setHighlightFrame(new HighlightFrame(view.getResources(), highlightFrame));
    }

    @ReactProp(name = "overlayColor")
    public void setOverlayColor(HighlighterView view, @Nullable Integer overlayColor) {
        view.setOverlayColor((overlayColor == null) ? 0 : overlayColor);
    }

    @ReactProp(name = "borderRadius")
    public void setBorderRadius(HighlighterView view, @Nullable Integer borderRadius) {
        view.setBorderRadius((borderRadius == null) ? 0 : borderRadius);
    }

    @ReactProp(name = "strokeColor")
    public void setStrokeColor(HighlighterView view, @Nullable Integer strokeColor) {
        view.setStrokeColor((strokeColor == null) ? 0 : strokeColor);
    }

    @ReactProp(name = "strokeWidth")
    public void setStrokeWidth(HighlighterView view, @Nullable Integer strokeWidth) {
        view.setStrokeWidth((strokeWidth == null) ? 0 : strokeWidth);
    }

    @ReactProp(name = "highlightViewTag")
    public void setHighlightViewTag(final HighlighterView view, Integer highlightViewTag) {
        try {
            NativeViewHierarchyManager nativeViewHierarchyManager = ReactHacks.getNativeViewHierarchyManager(context.getNativeModule(UIManagerModule.class));
            if (nativeViewHierarchyManager == null) {
                return;
            }

            final View resolvedView = nativeViewHierarchyManager.resolveView(highlightViewTag);
            if (resolvedView != null) {
                if (resolvedView.getWidth() == 0 || resolvedView.getHeight() == 0) {
                    resolvedView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            float width = right - left;
                            float height = bottom - top;
                            if (width > 0 && height > 0) {
                                setViewBasedHighlightFrame(view, resolvedView);
                                resolvedView.removeOnLayoutChangeListener(this);
                            }
                        }
                    });
                } else {
                    setViewBasedHighlightFrame(view, resolvedView);
                }
            }
        }
        catch (IllegalViewOperationException e) {
            Log.e("HighlighterView", "invalid highlightViewTag: " + highlightViewTag.toString() + " " + e.toString());
        }
    }

    private void setViewBasedHighlightFrame(HighlighterView view, View resolvedView) {
        Activity currentActivity = context.getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        final float topOffset = UiUtils.getStatusBarHeight(view, currentActivity.getWindow());
        final float frameExpand = UiUtils.pxToDp(view.getResources(), HighlightViewFrameExpand);
        Rect myViewRect = UiUtils.getVisibleRect(resolvedView);
        view.setViewBasedHighlightFrame(new HighlightFrame(myViewRect.left - frameExpand, myViewRect.top - frameExpand - topOffset, resolvedView.getWidth() + frameExpand * 2, resolvedView.getHeight() + frameExpand * 2));
    }
}
