package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import java.util.List;

class PolystarContent implements Content, PathContent {
  /**
   * This was empirically derived by creating polystars, converting them to
   * curves, and calculating a scale factor.
   * It works best for polygons and stars with 3 points and needs more
   * work otherwise.
   */
  private static final float POLYSTAR_MAGIC_NUMBER = .47829f;
  private static final float POLYGON_MAGIC_NUMBER = .25f;
  private final Path path = new Path();

  private final LottieDrawable lottieDrawable;
  private final PolystarShape.Type type;
  private final BaseKeyframeAnimation<?, Float> pointsAnimation;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;
  private final BaseKeyframeAnimation<?, Float> rotationAnimation;
  @Nullable private final BaseKeyframeAnimation<?, Float> innerRadiusAnimation;
  private final BaseKeyframeAnimation<?, Float> outerRadiusAnimation;
  @Nullable private final BaseKeyframeAnimation<?, Float> innerRoundednessAnimation;
  private final BaseKeyframeAnimation<?, Float> outerRoundednessAnimation;

  @Nullable private TrimPathContent trimPath;
  private boolean isPathValid;

  PolystarContent(LottieDrawable lottieDrawable, BaseLayer layer, PolystarShape polystarShape) {
    this.lottieDrawable = lottieDrawable;

    type = polystarShape.getType();
    pointsAnimation = polystarShape.getPoints().createAnimation();
    positionAnimation = polystarShape.getPosition().createAnimation();
    rotationAnimation = polystarShape.getRotation().createAnimation();
    outerRadiusAnimation = polystarShape.getOuterRadius().createAnimation();
    outerRoundednessAnimation = polystarShape.getOuterRoundedness().createAnimation();
    if (type == PolystarShape.Type.Star) {
      innerRadiusAnimation = polystarShape.getInnerRadius().createAnimation();
      innerRoundednessAnimation = polystarShape.getInnerRoundedness().createAnimation();
    } else {
      innerRadiusAnimation = null;
      innerRoundednessAnimation = null;
    }

    layer.addAnimation(pointsAnimation);
    layer.addAnimation(positionAnimation);
    layer.addAnimation(rotationAnimation);
    layer.addAnimation(outerRadiusAnimation);
    layer.addAnimation(outerRoundednessAnimation);
    if (type == PolystarShape.Type.Star) {
      layer.addAnimation(innerRadiusAnimation);
      layer.addAnimation(innerRoundednessAnimation);
    }

    BaseKeyframeAnimation.AnimationListener<Float> floatListener =
        new BaseKeyframeAnimation.AnimationListener<Float>() {
          @Override public void onValueChanged(Float value) {
            invalidate();
          }
        };
    BaseKeyframeAnimation.AnimationListener<PointF> pointListener =
        new BaseKeyframeAnimation.AnimationListener<PointF>() {
          @Override public void onValueChanged(PointF value) {
            invalidate();
          }
        };
    pointsAnimation.addUpdateListener(floatListener);
    positionAnimation.addUpdateListener(pointListener);
    rotationAnimation.addUpdateListener(floatListener);
    outerRadiusAnimation.addUpdateListener(floatListener);
    outerRoundednessAnimation.addUpdateListener(floatListener);
    if (type == PolystarShape.Type.Star) {
      outerRadiusAnimation.addUpdateListener(floatListener);
      outerRoundednessAnimation.addUpdateListener(floatListener);
    }
  }

  private void invalidate() {
    isPathValid = false;
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent) {
        trimPath = (TrimPathContent) content;
        trimPath.addListener(new BaseKeyframeAnimation.SimpleAnimationListener() {
          @Override public void onValueChanged() {
            lottieDrawable.invalidateSelf();
          }
        });
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    switch (type) {
      case Star:
        createStarPath();
        break;
      case Polygon:
        createPolygonPath();
        break;
    }

    path.close();

    Utils.applyTrimPathIfNeeded(path, trimPath);

    isPathValid = false;
    return path;
  }

  private void createStarPath() {
    float points = pointsAnimation.getValue();
    double currentAngle = rotationAnimation == null ? 0f : rotationAnimation.getValue();
    // Start at +y instead of +x
    currentAngle -= 90;
    // convert to radians
    currentAngle = Math.toRadians(currentAngle);
    // adjust current angle for partial points
    float anglePerPoint = (float) (2 * Math.PI / points);
    float halfAnglePerPoint = anglePerPoint / 2.0f;
    float partialPointAmount = points - (int) points;
    if (partialPointAmount != 0) {
      currentAngle += halfAnglePerPoint * (1f - partialPointAmount);
    }

    float outerRadius = outerRadiusAnimation.getValue();
    //noinspection ConstantConditions
    float innerRadius = innerRadiusAnimation.getValue();

    float innerRoundedness = 0f;
    if (innerRoundednessAnimation != null) {
      innerRoundedness = innerRoundednessAnimation.getValue() / 100f;
    }
    float outerRoundedness = 0f;
    if (outerRoundednessAnimation != null) {
      outerRoundedness = outerRoundednessAnimation.getValue() / 100f;
    }

    float x;
    float y;
    float previousX;
    float previousY;
    float partialPointRadius = 0;
    if (partialPointAmount != 0) {
      partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius);
      x = (float) (partialPointRadius * Math.cos(currentAngle));
      y = (float) (partialPointRadius * Math.sin(currentAngle));
      path.moveTo(x, y);
      currentAngle += anglePerPoint * partialPointAmount / 2f;
    } else {
      x = (float) (outerRadius * Math.cos(currentAngle));
      y = (float) (outerRadius * Math.sin(currentAngle));
      path.moveTo(x, y);
      currentAngle += halfAnglePerPoint;
    }

    // True means the line will go to outer radius. False means inner radius.
    boolean longSegment = false;
    double numPoints = Math.ceil(points) * 2;
    for (int i = 0; i < numPoints; i++) {
      float radius = longSegment ? outerRadius : innerRadius;
      float dTheta = halfAnglePerPoint;
      if (partialPointRadius != 0 && i == numPoints - 2) {
        dTheta = anglePerPoint * partialPointAmount / 2f;
      }
      if (partialPointRadius != 0 && i == numPoints - 1) {
        radius = partialPointRadius;
      }
      previousX = x;
      previousY = y;
      x = (float) (radius * Math.cos(currentAngle));
      y = (float) (radius * Math.sin(currentAngle));

      if (innerRoundedness == 0 && outerRoundedness == 0) {
        path.lineTo(x, y);
      } else {
        float cp1Theta = (float) (Math.atan2(previousY, previousX) - Math.PI / 2f);
        float cp1Dx = (float) Math.cos(cp1Theta);
        float cp1Dy = (float) Math.sin(cp1Theta);

        float cp2Theta = (float) (Math.atan2(y, x) - Math.PI / 2f);
        float cp2Dx = (float) Math.cos(cp2Theta);
        float cp2Dy = (float) Math.sin(cp2Theta);

        float cp1Roundedness = longSegment ? innerRoundedness : outerRoundedness;
        float cp2Roundedness = longSegment ? outerRoundedness : innerRoundedness;
        float cp1Radius = longSegment ? innerRadius : outerRadius;
        float cp2Radius = longSegment ? outerRadius : innerRadius;

        float cp1x = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dx;
        float cp1y = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dy;
        float cp2x = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dx;
        float cp2y = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dy;
        if (partialPointAmount != 0) {
          if (i == 0) {
            cp1x *= partialPointAmount;
            cp1y *= partialPointAmount;
          } else if (i == numPoints - 1) {
            cp2x *= partialPointAmount;
            cp2y *= partialPointAmount;
          }
        }

        path.cubicTo(previousX - cp1x,previousY - cp1y, x + cp2x, y + cp2y, x, y);
      }

      currentAngle += dTheta;
      longSegment = !longSegment;
    }


    PointF position = positionAnimation.getValue();
    path.offset(position.x, position.y);
    path.close();
  }

  private void createPolygonPath() {
    int points = (int) Math.floor(pointsAnimation.getValue());
    double currentAngle = rotationAnimation == null ? 0f : rotationAnimation.getValue();
    // Start at +y instead of +x
    currentAngle -= 90;
    // convert to radians
    currentAngle = Math.toRadians(currentAngle);
    // adjust current angle for partial points
    float anglePerPoint = (float) (2 * Math.PI / points);

    float roundedness = outerRoundednessAnimation.getValue() / 100f;
    float radius = outerRadiusAnimation.getValue();
    float x;
    float y;
    float previousX;
    float previousY;
    x = (float) (radius * Math.cos(currentAngle));
    y = (float) (radius * Math.sin(currentAngle));
    path.moveTo(x, y);
    currentAngle += anglePerPoint;

    double numPoints = Math.ceil(points);
    for (int i = 0; i < numPoints; i++) {
      previousX = x;
      previousY = y;
      x = (float) (radius * Math.cos(currentAngle));
      y = (float) (radius * Math.sin(currentAngle));

      if (roundedness != 0) {
        float cp1Theta = (float) (Math.atan2(previousY, previousX) - Math.PI / 2f);
        float cp1Dx = (float) Math.cos(cp1Theta);
        float cp1Dy = (float) Math.sin(cp1Theta);

        float cp2Theta = (float) (Math.atan2(y, x) - Math.PI / 2f);
        float cp2Dx = (float) Math.cos(cp2Theta);
        float cp2Dy = (float) Math.sin(cp2Theta);

        float cp1x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dx;
        float cp1y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dy;
        float cp2x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dx;
        float cp2y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dy;
        path.cubicTo(previousX - cp1x,previousY - cp1y, x + cp2x, y + cp2y, x, y);
      } else {
        path.lineTo(x, y);
      }

      currentAngle += anglePerPoint;
    }

    PointF position = positionAnimation.getValue();
    path.offset(position.x, position.y);
    path.close();
  }
}
