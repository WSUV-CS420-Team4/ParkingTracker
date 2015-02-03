package com.southwaterfront.parkingtracker.data;

/**
 * These are the types of modifiers a parking stall can have
 * 
 * @author Vitaliy Gavrilov
 *
 */
public enum Flags {

	HANDICAP {
		@Override
		public String getQualifiedName() {
			return "Handicap Placard";
		}
	},
	
	RESIDENTIAL {
		@Override
		public String getQualifiedName() {
			return "Residential Permit";
		}
	},
	
	EMPLOYEE {
		@Override
		public String getQualifiedName() {
			return "Employee Permit";
		}
	},
	
	STUDENT {
		@Override
		public String getQualifiedName() {
			return "Student Permit";
		}
	},
	
	CARPOOL {
		@Override
		public String getQualifiedName() {
			return "Carpool Permit";
		}
	},
	
	OTHER {
		@Override
		public String getQualifiedName() {
			return "Other";
		}
	};
	
	public abstract String getQualifiedName();
	
}
