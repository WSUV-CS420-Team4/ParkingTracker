package com.southwaterfront.parkingtracker.data;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.southwaterfront.parkingtracker.jsonify.BlockFaceParser;

/**
 * A data collector for collecting parking data
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class ParkingDataCollector {

	private static String LOG_TAG = ParkingDataCollector.class.getSimpleName();

	private final HashMap<String, BlockFace> data;

	/**
	 * Default constructor
	 * 
	 * @param defs Streetmodel definition
	 * @param cacheFolder The cache folder of this session
	 */
	public ParkingDataCollector(List<BlockFaceDefinition> defs, File cacheFolder) {
		if (defs == null || cacheFolder == null)
			throw new IllegalArgumentException("Arguments cannot be null");

		data = new HashMap<String, BlockFace>();
		streetToDataModel(defs, cacheFolder);
	}

	/**
	 * Converts the street model to a data model
	 */
	private void streetToDataModel(List<BlockFaceDefinition> defs, File cacheFolder) {
		for (BlockFaceDefinition d : defs) {
			String name = BlockFace.createName(d.block, d.face);
			File f = new File(cacheFolder, name);
			BlockFace b = null;
			if (f.exists()) {
				b = BlockFaceParser.parse(f);
			} 
			if (b == null)
				b = BlockFace.emptyPaddedBlockFace(d.block, d.face, d.numStalls);
			data.put(name, b);
		}
	}

	/**
	 * Getter for a specific block face instance
	 * 
	 * @param block Block number
	 * @param face Face name
	 * @return The corresponding {@link BlockFace} or null
	 */
	public BlockFace getBlockFace(int block, String face) {
		if (face == null || block < 0)
			throw new IllegalArgumentException("Arguments cannot be null");
		String name = BlockFace.createName(block, face);
		return data.get(name);
	}
	
	/**
	 * Used to reset a stall to empty
	 * 
	 * @param block Block number
	 * @param face Face name
	 * @param stallNumber Stall position on block face
	 * @return True if stall set, false if no such block face exists
	 */
	public boolean removeStall(int block, String face, int stallNumber) {
		if (block < 0 || face == null || stallNumber < 0)
			throw new IllegalArgumentException("Arguments cannot be null");
		BlockFace f = getBlockFace(block, face);
		if (f != null) {
			f.setStall(ParkingStall.EmptyStall, stallNumber);
			return true;
		}
		return false;
	}
	
	/**
	 * Convenience method to set parking stall data
	 * 
	 * @param block Block number
	 * @param face Face name
	 * @param stallNumber Stall position on block face
	 * @param stall Corresponding {@link ParkingStall}
	 * @return True if set, false if not
	 */
	public boolean setStall(int block, String face, int stallNumber, ParkingStall stall) {
		if (block < 0 || face == null || stallNumber < 0 || stall == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		BlockFace f = getBlockFace(block, face);
		if (f != null) {
			f.setStall(stall, stallNumber);
			return true;
		}
		return false;
	}
	
	/**
	 * {@link BlockFace} collection
	 * @return The collection of blockfaces in this collector
	 */
	public Collection<BlockFace> getBlockFaces() {
		return data.values();
	}
}
