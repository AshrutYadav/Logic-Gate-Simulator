package backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles saving and loading the simulator state to/from a text file.
 * Demonstrates: Java File I/O (BufferedReader, BufferedWriter, Path),
 * Validation, Error Handling, and Separation of Concerns.
 */
public class GateStorage {

    public static final String DEFAULT_FILEPATH = "saved_state.txt";

    /**
     * Data transfer object representing the simulator state.
     */
    public static class SimulatorState {
        private final GateType gateType;
        private final boolean inputA;
        private final boolean inputB;

        public SimulatorState(GateType gateType, boolean inputA, boolean inputB) {
            this.gateType = gateType;
            this.inputA = inputA;
            this.inputB = inputB;
        }

        public GateType getGateType() {
            return gateType;
        }

        public boolean isInputA() {
            return inputA;
        }

        public boolean isInputB() {
            return inputB;
        }
    }

    /**
     * Result object to encapsulate operation outcome without UI coupling.
     */
    public static class StorageResult {
        private final boolean success;
        private final String message;
        private final SimulatorState state;

        public StorageResult(boolean success, String message, SimulatorState state) {
            this.success = success;
            this.message = message;
            this.state = state;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public SimulatorState getState() {
            return state;
        }
    }

    /**
     * Saves the current simulator state to the default text file.
     */
    public static StorageResult saveSimulatorState(SimulatorState state) {
        return saveSimulatorState(state, DEFAULT_FILEPATH);
    }

    /**
     * Saves the current simulator state to a specified file path.
     *
     * @param state State to save
     * @param filepath Destination path
     * @return StorageResult indicating success or failure
     */
    public static StorageResult saveSimulatorState(SimulatorState state, String filepath) {
        if (state == null || state.getGateType() == null) {
            return new StorageResult(false, "Invalid simulator state: Gate type cannot be null.", state);
        }

        Path path = Paths.get(filepath);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("GATE: " + GateFactory.gateTypeToString(state.getGateType()));
            writer.newLine();
            writer.write("INPUT_A: " + (state.isInputA() ? "1" : "0"));
            writer.newLine();
            writer.write("INPUT_B: " + (state.isInputB() ? "1" : "0"));
            writer.newLine();

            return new StorageResult(true, "State successfully saved to " + filepath, state);
        } catch (IOException e) {
            return new StorageResult(false, "Failed to write state file: " + e.getMessage(), state);
        }
    }

    /**
     * Loads and validates the simulator state from the default text file.
     */
    public static StorageResult loadSimulatorState() {
        return loadSimulatorState(DEFAULT_FILEPATH);
    }

    /**
     * Loads and strictly validates the simulator state from the specified file.
     * Guarantees that invalid files do not corrupt the existing state.
     *
     * @param filepath Source path
     * @return StorageResult with loaded state on success, or error message on failure
     */
    public static StorageResult loadSimulatorState(String filepath) {
        Path path = Paths.get(filepath);

        if (!Files.exists(path)) {
            return new StorageResult(false, "Could not find file: " + filepath, null);
        }

        GateType loadedGate = null;
        Boolean loadedA = null;
        Boolean loadedB = null;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("[:=\\s]+", 2);
                if (parts.length < 2) {
                    continue;
                }

                String key = parts[0].trim().toUpperCase();
                String val = parts[1].trim().toUpperCase();

                if (key.equals("GATE") || key.equals("GATETYPE")) {
                    GateType parsedType = GateFactory.stringToGateType(val);
                    if (parsedType != null) {
                        loadedGate = parsedType;
                    } else {
                        return new StorageResult(false, "Unrecognized or invalid gate name in file: '" + val + "'", null);
                    }
                } else if (key.equals("INPUT_A") || key.equals("A")) {
                    Boolean parsedVal = parseBooleanValue(val);
                    if (parsedVal != null) {
                        loadedA = parsedVal;
                    } else {
                        return new StorageResult(false, "Invalid value for INPUT_A in file: '" + val + "'", null);
                    }
                } else if (key.equals("INPUT_B") || key.equals("B")) {
                    Boolean parsedVal = parseBooleanValue(val);
                    if (parsedVal != null) {
                        loadedB = parsedVal;
                    } else {
                        return new StorageResult(false, "Invalid value for INPUT_B in file: '" + val + "'", null);
                    }
                }
            }
        } catch (IOException e) {
            return new StorageResult(false, "Error reading file: " + e.getMessage(), null);
        }

        // Validate presence of required attributes
        if (loadedGate == null) {
            return new StorageResult(false, "File is missing a valid GATE name.", null);
        }

        if (loadedA == null) {
            return new StorageResult(false, "File is missing a valid INPUT_A value.", null);
        }

        // For two-input gates, verify that INPUT_B was provided
        if (loadedGate != GateType.NOT) {
            if (loadedB == null) {
                return new StorageResult(false, "File is missing INPUT_B for two-input gate (" + loadedGate + ").", null);
            }
        } else {
            // NOT gate only needs A, default B to false
            if (loadedB == null) {
                loadedB = false;
            }
        }

        SimulatorState newState = new SimulatorState(loadedGate, loadedA, loadedB);
        String details = "State loaded successfully: " + loadedGate + ", A=" + (loadedA ? "1" : "0");
        if (loadedGate != GateType.NOT) {
            details += ", B=" + (loadedB ? "1" : "0");
        }

        return new StorageResult(true, details, newState);
    }

    /**
     * Helper to parse flexible boolean formats: 1/0, TRUE/FALSE, HIGH/LOW.
     */
    private static Boolean parseBooleanValue(String val) {
        if (val == null) return null;
        val = val.trim().toUpperCase();
        if (val.equals("1") || val.equals("TRUE") || val.equals("HIGH") || val.equals("ON")) {
            return true;
        }
        if (val.equals("0") || val.equals("FALSE") || val.equals("LOW") || val.equals("OFF")) {
            return false;
        }
        return null;
    }
}
