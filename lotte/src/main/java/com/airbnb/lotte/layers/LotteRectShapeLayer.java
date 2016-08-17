package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeRectangle;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteRectShapeLayer extends LotteAnimatableLayer {

    private final Paint paint = new Paint();

    private final LotteShapeTransform transformModel;
    private final LotteShapeStroke stroke;
    private final LotteShapeFill fill;
    private final LotteShapeRectangle rectShape;

    private LotteRoundRectLayer fillLayer;
    private LotteRoundRectLayer strokeLayer;

    LotteAnimationGroup animation;
    LotteAnimationGroup strokeAnimation;
    LotteAnimationGroup fillAanimation;

    public LotteRectShapeLayer(LotteShapeRectangle rectShape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, LotteShapeTransform transform, long duration) {
        super(duration);
        this.rectShape = rectShape;
        this.fill = fill;
        this.stroke = stroke;
        this.transformModel = transform;

        paint.setAntiAlias(true);
        setBounds(transform.getCompBounds());
        anchorPoint = transform.getAnchor().getInitialPoint();
        setAlpha((int) (transform.getOpacity().getInitialValue() * 255));
        position = transform.getPosition().getInitialPoint();
        this.transform = transform.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ(transform.getRotation().getInitialValue());

        if (fill != null) {
            fillLayer = new LotteRoundRectLayer(0);
            fillLayer.setFillColor(fill.getColor().getInitialColor());
            fillLayer.setAlpha((int) (fill.getOpacity().getInitialValue() * 255));
            fillLayer.setRectCornerRadius(rectShape.getCornerRadius().getInitialValue());
            fillLayer.setRectSize(rectShape.getSize().getInitialPoint());
            fillLayer.setRectPosition(rectShape.getPosition().getInitialPoint());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            // TODO
        }

        // TODO
    }

    private void buildAnimation() {
        // TODO
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }

    private static class LotteRoundRectLayer extends LotteAnimatableLayer {

        private final Paint fillPaint = new Paint();
        private final RectF fillRect = new RectF();

        private PointF rectPosition;
        private PointF rectSize;
        private float rectCornerRadius;

        LotteRoundRectLayer(long duration) {
            super(duration);
            fillPaint.setAntiAlias(true);
            fillPaint.setStyle(Paint.Style.FILL);
        }

        public void setFillColor(@ColorInt int color) {
            fillPaint.setColor(color);
        }

        public float getRectCornerRadius() {
            return rectCornerRadius;
        }

        public void setRectCornerRadius(float rectCornerRadius) {
            this.rectCornerRadius = rectCornerRadius;
        }

        public PointF getRectPosition() {
            return rectPosition;
        }

        public void setRectPosition(PointF rectPosition) {
            this.rectPosition = rectPosition;
        }

        public PointF getRectSize() {
            return rectSize;
        }

        public void setRectSize(PointF rectSize) {
            this.rectSize = rectSize;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
            float halfWidth = rectSize.x / 2f;
            float halfHeight = rectSize.y / 2f;

            fillRect.set(rectPosition.x - halfWidth,
                    rectPosition.y - halfHeight,
                    rectPosition.x + halfWidth,
                    rectPosition.y + halfHeight);
            canvas.drawRoundRect(fillRect, rectCornerRadius, rectCornerRadius, fillPaint);
        }
    }

}
