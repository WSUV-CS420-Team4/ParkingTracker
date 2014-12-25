package com.southwaterfront.parkingtracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to hold a block face, holds block, face names
 * and list of {@link ParkingStall} objects.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public final class BlockFace {

	public final String 						block;
	public final String 						face;
	private final List<ParkingStall> stalls;

	public BlockFace(String block, String face) {
		if (block == null || face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		this.block = block;
		this.face = face;
		this.stalls = new ArrayList<ParkingStall>();
	}

	/**
	 * Creates a specified empty block face. The stalls can still be
	 * edited with {@link BlockFace#setStall(ParkingStall, int)}
	 * 
	 * @param block Block name
	 * @param face Face name
	 * @param stalls Number of empty stalls to put
	 * @return Created empty padded block face
	 */
	public static BlockFace emptyPaddedBlockFace(String block, String face, int stalls) {
		if (block == null || face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		if (stalls < 0)
			throw new IllegalArgumentException("Must have a positive size");
		BlockFace bF = new BlockFace(block, face);
		
		for (int i = 0; i < stalls; i++)
			bF.stalls.add(ParkingStall.EmptyStall);
		
		return bF;
	}
	
	/**
	 * Add a stall to this block face. Adding a null will
	 * not throw an exception but will be ignored. This is an
	 * append add
	 * 
	 * @param stall Stall to add
	 */
	public void addStall(ParkingStall stall) {
		if (stall == null)
			return;
		
		this.stalls.add(stall);
	}
	
	/**
	 * Add a stall at a specified position, 0 indexed.
	 * Adding a stall at an index past the next position
	 * will result in a list whose empty spaces are padded
	 * with {@link ParkingStall#EmptyStall} objects.
	 * 
	 * @param stall Stall to add
	 * @param position Position to place it at
	 */
	public void setStall(ParkingStall stall, int position) {
		if (stall == null || position < 0)
			return;
		
		for (int i = this.stalls.size(); i < position; i++)
			this.stalls.add(ParkingStall.EmptyStall);
		
		this.stalls.set(position, stall);
	}

	/**
	 * Remove an object from stalls list
	 * 
	 * @param obj Object to remove
	 * @return True if object removed, false otherwise
	 */
	public boolean removeStall(Object obj) {
		if (obj == null)
			return false;
		
		return this.stalls.remove(obj);
	}

	/**
	 * Getter for the list of stalls. To keep this data safe
	 * the returned list will be unmodifiable.
	 * 
	 * @return The list of parking stalls
	 */
	public List<ParkingStall> getParkingStalls() {
		return Collections.unmodifiableList(this.stalls);
	}
	
	/**
	 * Two block faces are equal if and only if the block and face
	 * names are case insensitive lexicographically equivalent.
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof BlockFace) {
			BlockFace o = (BlockFace) other;
			return this.block.equalsIgnoreCase(o.block) && this.face.equalsIgnoreCase(o.face);
		} else
			return false;
	}
	
	/**
	 * The hash code is the XOR of the hashes of the block
	 * and face names
	 */
	@Override
	public int hashCode() {
		return this.block.hashCode() ^ this.face.hashCode();
	}

}
