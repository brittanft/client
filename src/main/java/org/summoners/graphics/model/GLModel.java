package org.summoners.graphics.model;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

import org.summoners.cache.data.model.*;
import org.summoners.cache.data.model.AnimationDefinition.*;
import org.summoners.cache.data.model.SkeletonDefinition.*;
import org.summoners.cache.data.model.SkinDefinition.*;
import org.summoners.util.*;

public class GLModel {
	
	public String textureName;
	
	public LinkedList<String> getAnimationNames() {
		return animations.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toCollection(() -> new LinkedList<>()));
	}
	
	public int indexCount, vao, vertexPositionBuffer, indexBuffer, vertexTextureCoordinateBuffer, 
		vertexNormalBuffer, vertexBoneBuffer, vertexBoneWeightBuffer;
	
	public String currentAnimation;
	public float currentTimeFrame;
	public int currentFrame;
	
	public HashMap<String, GLAnimation> animations = new HashMap<>();
	
	public GLModel() {
		textureName = "";
		indexCount = vao = vertexPositionBuffer = indexBuffer = vertexTextureCoordinateBuffer = 
				vertexNormalBuffer = vertexBoneBuffer = vertexBoneWeightBuffer = 0;
		
		currentAnimation = "";
		currentTimeFrame = 0.0F;
		currentFrame = 0;
	}
	
	public boolean create(SkinDefinition skn, SkeletonDefinition skl, HashMap<String, AnimationDefinition> anims) {
		boolean result = true;
		
		LinkedList<Float> vertexPositions = new LinkedList<>();
		LinkedList<Float> vertexNormals = new LinkedList<>();
		LinkedList<Float> vertexTextureCoordinates = new LinkedList<>();
		LinkedList<Float> vertexBoneIndices = new LinkedList<>();
		LinkedList<Float> vertexBoneWeights = new LinkedList<>();
		LinkedList<Long> indices = new LinkedList<>();
		
		LinkedList<Quaternion> boneOrientations = new LinkedList<>();
		LinkedList<Vec3f> bonePositions = new LinkedList<>();
		LinkedList<Float> boneScales = new LinkedList<>();
		LinkedList<Integer> boneParents = new LinkedList<>();
		LinkedList<String> boneNames = new LinkedList<>();
		
		HashMap<String, Integer> boneNameToID = new HashMap<>();
		HashMap<Integer, String> boneIDToName = new HashMap<>();
		
		for (int i = 0; i != skn.getVertexCount(); ++i) {
			SkinVertex vertex = skn.getVertices().get(i);
			vertexPositions.add(vertex.getPosition()[0]);
			vertexPositions.add(vertex.getPosition()[1]);
			vertexPositions.add(-vertex.getPosition()[2]);
			
			vertexNormals.add(vertex.getNormal()[0]);
			vertexNormals.add(vertex.getNormal()[1]);
			vertexNormals.add(-vertex.getNormal()[2]);

			vertexTextureCoordinates.add(vertex.getTexCoords()[0]);
			vertexTextureCoordinates.add(vertex.getTexCoords()[1]);
			
			for (int j = 0; j != SkinVertex.BONE_INDEX_SIZE; ++j)
				vertexBoneIndices.add((float) vertex.getBoneIndex()[j]);
			
			vertexBoneWeights.add(vertex.getWeights()[0]);
			vertexBoneWeights.add(vertex.getWeights()[1]);
			vertexBoneWeights.add(vertex.getWeights()[2]);
			vertexBoneWeights.add(vertex.getWeights()[3]);
		}
		
		for (int i = 0; i != skl.getBoneCount(); ++i) {
			SkeletonBone bone = skl.getBones().get(i);
			
			Quaternion orientation = Quaternion.ID;
			if (skl.getVersion() == 0) {
				orientation.x = bone.getOrientation()[0];
				orientation.y = bone.getOrientation()[1];
				orientation.z = bone.getOrientation()[2];
				orientation.w = bone.getOrientation()[3];
			} else {
				Matrix4f transform = Matrix4f.ID;
				
				Matrix4f matrix = new Matrix4f(bone.getOrientation());
				/*transform.matrix1_1 = bone.getOrientation()[0]; //TODO
				transform.matrix2_1 = bone.getOrientation()[1];
				transform.matrix3_1 = bone.getOrientation()[2];
				
				transform.matrix1_2 = bone.getOrientation()[4];
				transform.matrix2_2 = bone.getOrientation()[5];
				transform.matrix3_2 = bone.getOrientation()[6];

				transform.matrix1_3 = bone.getOrientation()[8];
				transform.matrix2_3 = bone.getOrientation()[9];
				transform.matrix3_3 = bone.getOrientation()[10];*/
				
                // Convert the matrix to a quaternion.
                //orientation = OpenTKExtras.Matrix4.CreateQuatFromMatrix(transform); //TODO
                //orientation.Z = -orientation.Z;
                //orientation.W = -orientation.W;
			}
			
			boneOrientations.add(orientation);
			bonePositions.add(new Vec3f(bone.getPosition()[0], bone.getPosition()[1], bone.getPosition()[2]));
			
			boneNames.add(bone.getName());
			boneNameToID.put(bone.getName(), i);
			boneIDToName.put(i, bone.getName());
			
			boneScales.add(bone.getScale());
			boneParents.add(bone.getParentId());
		}
		
		if (skl.getVersion() == 0) {
			for (int i = 0; i != skl.getBoneCount(); ++i) {
				SkeletonBone bone = skl.getBones().get(i);
				if (bone.getParentId() != -1) {
					int parentBoneId = bone.getParentId();
					//boneOrientations.get(i) = boneOrientations.get(parentBoneId) * boneOrientations.get(i); //TODO
					
					Vec3f vec = new Vec3f(bone.getPosition());
                    //bonePositions[i] = bonePositions[parentBoneID] + Vector3.Transform(localPosition, boneOrientations[parentBoneID]); //TODO
					//bonePositions.set(i, bonePositions.get(i) + vec);
				}
			}
		}
		
		if (skl.getVersion() == 2 || skl.getVersion() == 0) {
			for (int i = 0; i != vertexBoneIndices.size(); ++i) {
				if (vertexBoneIndices.get(i) < skl.getBoneIds().size())
					vertexBoneIndices.set(i, (float) skl.getBoneIds().get(vertexBoneIndices.get(i).intValue()));
			}
		}
		
		for (Entry<String, AnimationDefinition> entry : anims.entrySet()) {
			if (!animations.containsKey(entry.getKey())) {
				GLAnimation glAnimation = new GLAnimation();
				glAnimation.playbackFPS = entry.getValue().getPlaybackFps();
				glAnimation.boneCount = entry.getValue().getBoneCount();
				glAnimation.frameCount = entry.getValue().getFrameCount();
				
				for (AnimationBone bone : entry.getValue().getBones()) {
					GLBone glBone = new GLBone();
					
					if (entry.getValue().getVersion() == 4 && skl.getBoneIdMap().size() > 0) {
						if (skl.getBoneIdMap().containsKey(bone.getId())) {
							int sklID = skl.getBoneIdMap().get(bone.getId()).intValue();
							glBone.name = boneIDToName.get(sklID);
						}
					} else
						glBone.name = bone.getName();
					
					for (AnimationFrame frame : bone.getFrames()) {
						Matrix4f transform = Matrix4f.ID;
						
						Quaternion quat = new Quaternion(frame.getOrientation()[0], frame.getOrientation()[1], -frame.getOrientation()[2], -frame.getOrientation()[3]);
						//transform = Matrix4f.rotate(quaternion); //TODO
						//
						
						glBone.frames.add(transform);
					}
					
					glAnimation.bones.add(glBone);
				}
				
				glAnimation.timePerFrame = 1.0F / entry.getValue().getPlaybackFps();
				animations.put(entry.getKey(), glAnimation);
			}
		}
		
		for (int i = 0; i != skn.getIndexCount(); ++i) 
			indices.add(skn.getIndices().get(i).longValue());
	
		this.indexCount = indices.size();
		
		for (Entry<String, GLAnimation> entry : animations.entrySet()) {
			//TODO
		}
		
		GLAnimation bindingBones = new GLAnimation();
		for (int i = 0; i != boneOrientations.size(); ++i) {
			GLBone bone = new GLBone();
			bone.name = boneNames.get(i);
			bone.parent = boneParents.get(i);
			//bone.transform = Matrix4f.rotats(boneOrientations.get(i)); //TODO
			
			bindingBones.bones.add(bone);
		}
		
		//TODO
		
		return result;
	}
}
