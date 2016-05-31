package org.summoners.graphics.model;

public class GLShader {
	
	public GLShader() {
		//this.type = type;
		this.shader = 0;
		this.data = "";
	}
	
	public boolean fromMemory(String data) {
		this.data = data;
		return true;
	}
	
	public boolean compile() {
		boolean result = true;
		//shader = GL.createShader(type);
		return result;
	}
	
	public void destroy() {
		//GL.deleteShader(shader);
	}
	
	public int shader;
	//public ShaderType type;
	public String data;
}
