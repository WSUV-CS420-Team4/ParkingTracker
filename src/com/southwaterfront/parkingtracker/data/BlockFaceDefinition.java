package com.southwaterfront.parkingtracker.data;

/**
 * This is a block face definition
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class BlockFaceDefinition {

	public final int block;
	public final String face;
	public final int numStalls;			
	
	public BlockFaceDefinition(int block, String face, int numStalls) {
		if (face == null || numStalls < 0)
			throw new IllegalArgumentException("Arguments cannot be null");
		this.block = block;
		this.face = face;
		this.numStalls = numStalls;
	}
	
}
