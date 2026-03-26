package com.Payment.Shop.Validator;

//@Service
public class PasswordValidator {

    /**
     * Validates that a password meets the following criteria:
     * - At least one uppercase character
     * - At least one lowercase character
     * - At least one number
     * - At least one special character
     *
     * @param password The password to validate
     * @return true if the password meets all criteria, false otherwise
     */
    public boolean isValid(String password) {
        if (password == null) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*");

        return hasUppercase && hasLowercase && hasNumber && hasSpecialChar;
    }

    /**
     * Validates a password and returns a list of validation errors.
     *
     * @param password The password to validate
     * @return A string with validation errors or null if the password is valid
     */
    public String getValidationErrors(String password) {
        if (password == null) {
            return "Password cannot be null";
        }

        StringBuilder errors = new StringBuilder();

        if (!password.matches(".*[A-Z].*")) {
            errors.append("Password must contain at least one uppercase character. ");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.append("Password must contain at least one lowercase character. ");
        }

        if (!password.matches(".*\\d.*")) {
            errors.append("Password must contain at least one number. ");
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*")) {
            errors.append("Password must contain at least one special character. ");
        }

        return errors.length() > 0 ? errors.toString().trim() : null;
    }
}