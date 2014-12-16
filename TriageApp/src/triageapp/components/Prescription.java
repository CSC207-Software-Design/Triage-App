package triageapp.components;

import java.io.Serializable;


/**
 * The class Prescription stores the medication's name and instructions on using it.
 */
public class Prescription implements Serializable{

	/** A unique ID for serialization. */
	private static final long serialVersionUID = -9139417873374146063L;
	/**	The medication name. */
	private String medication;
	/**	The medication's instructions */
	private String instructions;

	/**
	 * Constructs Prescription with the name of the medication and its instructions.
	 * @param name The name of the medication.
	 * @param instructions The instructions for the medication.
	 * @throws InvalidUserInputException 
	 */
	public Prescription(String name, String instructions) throws InvalidUserInputException {
		// Checks for unwanted characters such as "~" 
		// or new line due to the file writing procedure.
		if(name.contains("~") ||
				instructions.contains("~") ||
				instructions.contains("\n"))
			throw new InvalidUserInputException();
		
		this.medication = name;
		this.instructions = instructions;
	}

	/**
	 * Returns @return The medication name.
	 * @return The medication name.
	 */
	public String getMedicationName() {
		return medication;
	}
	
	/**
	 * Returns the instructions for the medicine.
	 * @return The instructions for the medicine.
	 */
	public String getInstructions() { 
		return instructions;
	}

	/**
	 * Returns a String representation of the Prescription class.
	 * @return A String representation of the Prescription class.
	 */
	public String toString() {
		return "Prescription~"+medication+"~"+instructions + "\n";
	}
}
