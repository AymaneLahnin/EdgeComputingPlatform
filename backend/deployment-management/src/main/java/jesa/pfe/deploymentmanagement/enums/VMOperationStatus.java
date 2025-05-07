package jesa.pfe.deploymentmanagement.enums;

/**
 * Enum representing possible statuses for VM operations
 */
public enum VMOperationStatus {
    SUCCESS,                    // Operation completed successfully
    FAILED,                     // Operation failed
    ERROR,                      // Unexpected error occurred
    TIMEOUT,                    // Operation timed out
    VM_NOT_FOUND,               // VM not found in database
    VM_NOT_FOUND_IN_VIRTUALBOX, // VM found in database but not in VirtualBox
    VM_ALREADY_IN_DESIRED_STATE // VM is already in the state we want to transition to
}