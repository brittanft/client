package org.summoners.graphics.model;

import java.util.*;

public class GLAnimation {
	
	public GLAnimation() { }
	
	public long playbackFPS = 0;
	public float timePerFrame;
	
	public long boneCount = 0;
	public long frameCount = 0;
	
	public LinkedList<GLBone> bones = new LinkedList<>();
	
}
