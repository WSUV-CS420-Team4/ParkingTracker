package com.southwaterfront.parkingtracker.data;

import java.util.Date;

/**
 * Class to hold single stall
 * 
 * @author Vitaliy Gavrilov
 *
 */
public final class ParkingStall {
	
	public static final ParkingStall EmptyStall = new ParkingStall("", new Date(), null);

	public final String	 	plate;
	public final Date 		dTStamp;
	public final String[]	attr;
	
	public ParkingStall(String plate, Date time, String[] attr) {
		if (plate == null || time == null)
			throw new IllegalArgumentException("Plate and time cannot be null");
			
		this.plate = plate;
		this.dTStamp = time;
		this.attr = attr;
	}
	
	/**
	 * Getter for empty stall object
	 * 
	 * @return The empty stall
	 */
	public static ParkingStall getEmptyStall() {
		return ParkingStall.EmptyStall;
	}
	
}
