package org.summoners.graphics.model;

import org.summoners.math.*;

public class GLCamera {
	
	public float fov, aspect, near, far;
	public Vec3f defaultEye, defaultTarget;
	public Vec3f eye;
	public Vec3f target;
	public Matrix4f view, projection;
	
	public static final float MINIMUM_RADIUS = 35.0F;
	public static final float MAXIMUM_RADIUS = 800.0F;
	public static final float INITIAL_RADIUS = 300.0F;
	public float radius, defaultRadius;
	
	public boolean wasDraggedSinceLastUpdate;
	public Matrix4f lastRotation;
	public boolean updateLastRotation;
	
	//private static enum CameraKeyValues {
	//	LEFT, RIGHT, FORWARD, BACKWARD, RESET, SIZE;
	//}
	
	public GLCamera() {
		fov = aspect = near = far = 0.0F;
		
		eye = defaultEye = Vec3f.ID;
		target = defaultTarget = Vec3f.ID;
		view = Matrix4f.ID;
		projection = Matrix4f.ID;
		
		wasDraggedSinceLastUpdate = false;
		radius = defaultRadius = INITIAL_RADIUS;
		
		reset();
	}
	
	public void update() {
		if (!wasDraggedSinceLastUpdate)
			return;
		
		wasDraggedSinceLastUpdate = false;
		
		/* Calculate rotation. */
		
		
	}
	
	public void setViewParams(Vec3f eye, Vec3f target) {
		this.eye = this.defaultEye = eye;
		this.target = this.defaultTarget = target;
		
		Matrix4f rotation = Matrix4f.ID;
		view = rotation;
		
		wasDraggedSinceLastUpdate = true;
		update();
	}
	
	public void reset() {}

}
