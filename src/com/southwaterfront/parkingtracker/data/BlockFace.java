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
public final class BlockFace implements Comparable<BlockFace> {

	public final int 						block;
	public final String 						face;
	private final List<ParkingStall> stalls;
	private boolean modifiedSince;
	private final String name;

	private int nonEmptyStalls;
	
	public BlockFace(int block, String face) {
		if (face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		this.block = block;
		this.face = face;
		this.stalls = new ArrayList<ParkingStall>();
		this.modifiedSince = false;
		this.name = createName(block, face);
		this.nonEmptyStalls = 0;
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
	public static BlockFace emptyPaddedBlockFace(int block, String face, int stalls) {
		if (face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		if (stalls < 0)
			throw new IllegalArgumentException("Must have a positive size");
		BlockFace bF = new BlockFace(block, face);
		
		for (int i = 0; i < stalls; i++)
			bF.stalls.add(ParkingStall.EmptyStall);
		bF.resetModifiedFlag();
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
		if (stall != ParkingStall.EmptyStall)
			nonEmptyStalls++;
		this.modifiedSince = true;
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
		
		for (int i = this.stalls.size(); i <= position; i++)
			this.stalls.add(ParkingStall.EmptyStall);
		
		ParkingStall prev = this.stalls.set(position, stall);
		if (stall != ParkingStall.EmptyStall)
			nonEmptyStalls++;
		else {
			if (prev != ParkingStall.EmptyStall)
				nonEmptyStalls--;
		}
		this.modifiedSince = true;
	}

	/**
	 * Remove an object from stalls list, literally will make
	 * the list of parking stalls shorter
	 * 
	 * @param obj Object to remove
	 * @return True if object removed, false otherwise
	 */
	public boolean removeStall(Object obj) {
		if (obj == null)
			return false;
		
		boolean modified = this.stalls.remove(obj);
		if (modified && obj != ParkingStall.EmptyStall)
			nonEmptyStalls--;
		this.modifiedSince |= modified;
		return modified;
	}
	
	/**
	 * Resets the flag specifying if the data has been modified since
	 * last reset
	 * 
	 */
	public void resetModifiedFlag() {
		this.modifiedSince = false;
	}
	
	/**
	 * Checks if data modified
	 * @return True if modified since last 
	 */
	public boolean isModified() {
		return this.modifiedSince;
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
			return this.block == o.block && this.face.equalsIgnoreCase(o.face);
		} else
			return false;
	}
	
	/**
	 * The hash code is the XOR of the hashes of the block
	 * and face names
	 */
	@Override
	public int hashCode() {
		return this.block ^ this.face.hashCode();
	}

	/**
	 * Getter for name of block face, used for file naming
	 * @return String name of block face
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Creats the blockface name for a block face
	 * 
	 * @param block Block id
	 * @param face Face string
	 * @return Block face name
	 */
	public static String createName(int block, String face) {
		return block + "_" + face;
	}
	
	@Override
	public String toString() {
		return block + " " + face;
	}

	@Override
	public int compareTo(BlockFace another) {
		if (this.block < another.block)
			return -1;
		if (this.block > another.block)
			return 1;
		else
			return this.face.compareTo(another.face);
	}
	
	/**
	 * Getter for number of non empty stalls in this block face
	 * 
	 * @return Integer
	 */
	public int getNumNonEmptyStalls() {
		return this.nonEmptyStalls;
	}
	
}
