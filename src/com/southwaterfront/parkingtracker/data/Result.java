package com.southwaterfront.parkingtracker.data;

/**
 * Types of results for a task
 * 
 * @author Vitaliy Gavrilov
 *
 */
public enum Result {
	/**
	 * Task in progress
	 */
	IN_PROGRESS, 
	
	/**
	 * Task finished successfully
	 */
	SUCCESS, 
	
	/**
	 * Task finished unsuccessfully
	 */
	FAIL
}