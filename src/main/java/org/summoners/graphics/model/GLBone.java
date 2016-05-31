package org.summoners.graphics.model;

import java.util.*;

import org.summoners.util.*;

public class GLBone {
	
	public String name;
	public int parent;
	
	public Matrix4f transform;
	
	public LinkedList<Matrix4f> frames = new LinkedList<>();
}
