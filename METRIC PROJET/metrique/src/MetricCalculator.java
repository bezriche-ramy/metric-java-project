import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;


public class MetricCalculator extends JFrame {

    private JComboBox<String> metricComboBox;
    private JButton selectFileButton;
    private JLabel resultLabel;
    private JTextField resultField;

    public MetricCalculator() {
        // Set up the frame
        setTitle("Metric Calculator");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.LIGHT_GRAY);

        // ComboBox for metric selection
        String[] metrics = {"EHI", "FM", "ODR", "RMI"};
        metricComboBox = new JComboBox<>(metrics);
        metricComboBox.setBounds(100, 20, 200, 40);

        // Button
        selectFileButton = new JButton("Select a .java file");
        selectFileButton.setBounds(100, 70, 200, 40);
        selectFileButton.setFocusable(false);
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Java Files", "java"));
                int result = fileChooser.showOpenDialog(MetricCalculator.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String metric = (String) metricComboBox.getSelectedItem();
                    double score = calculateMetric(metric, selectedFile);
                    resultField.setText(String.valueOf(score) + (metric.equals("RMI") ? " %" : ""));
                }
            }
        });

        // Label and TextField for result
        resultLabel = new JLabel("Result:");
        resultLabel.setBounds(100, 120, 200, 40);
        resultField = new JTextField(20);
        resultField.setBounds(100, 170, 200, 40);
        Font font = resultField.getFont();
        resultField.setFont(font.deriveFont(Font.BOLD, 22));
        resultField.setEditable(false);

        add(metricComboBox);
        add(selectFileButton);
        add(resultLabel);
        add(resultField);
        setVisible(true);
        
    }

    private double calculateMetric(String metric, File file) {
        switch (metric) {
            case "EHI":
                return calculateEHI(file);
            case "FM":
                return calculateFM(file);
            case "ODR":
                return calculateODR(file);
            case "RMI":
                return calculateRMI(file);
            default:
                return 0.0;
        }
    }

    private int calculateEHI(File file) {
        int tryCount = 0;
        int catchHandlingCount = 0;
        boolean insideCatch = false;
        boolean handled = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim(); // Remove whitespaces

                // Detect try block
                if (line.contains("try {")) {
                    tryCount++;
                    insideCatch = false;
                    handled = false;
                }

                // Detect start of catch block
                if (line.contains("catch")) {
                    insideCatch = true;
                    handled = false;
                }

                // Check for error handling inside catch block
                if (insideCatch) {
                    if (line.contains("log") || line.contains("throw") || line.contains("System.out.print") ||
                            line.contains("getMessage") || line.contains("printStackTrace")) {
                        handled = true;
                    }
                }

                // Reset insideCatch after finishing with a catch block
                if (insideCatch && line.equals("}")) {
                    insideCatch = false;
                    if (handled) {
                        catchHandlingCount++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate the EHI metric
        if (tryCount == 0) return 0; // To avoid division by zero
        return (catchHandlingCount * 100) / tryCount;
    }

    private static double calculateFM(File file) {
        int totalReusableMethods = 0;
        int totalMethods = 0;
        int braceCount = 0; // Track the balance of braces
        boolean inMethod = false; // Flag to track if inside a method
        boolean inMain = false; // Flag to track if inside the main method
        List<String> calledMethods = new ArrayList<>(); // List to store called methods in the main method

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Track brace balance to accurately detect method boundaries
                if (line.contains("{")) {
                    braceCount++;
                }
                if (line.contains("}")) {
                    braceCount--;
                }

                // Check if inside a method
                if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
                    inMethod = true;
                    totalMethods++;
                }

                // Check if inside the main method
                if (line.contains("public static void main")) {
                    inMain = true;
                }

                // Check for method calls within the main method
                if (inMain && braceCount > 0 && !line.startsWith("//") && !line.startsWith("/*") && line.contains("(") && line.contains(")") && !line.contains("main")) {
                    // Extract method name
                    String[] parts = line.split("\\(");
                    String methodName = parts[0].trim();
                    calledMethods.add(methodName);
                }

                // Check if exiting a method
                if (inMethod && braceCount == 0) {
                    inMethod = false;
                    if (inMain) {
                        inMain = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Debugging output
        System.out.println("Total methods: " + totalMethods);
        System.out.println("Called methods in main: " + calledMethods);

        // Count the number of times each method is called within the main method
        Map<String, Integer> methodCalls = new HashMap<>();
        for (String methodName : calledMethods) {
            methodCalls.put(methodName, methodCalls.getOrDefault(methodName, 0) + 1);
        }

        // Debugging output
        System.out.println("Method call counts: " + methodCalls);

        // Check if each method is called more than once in the main method
        for (Map.Entry<String, Integer> entry : methodCalls.entrySet()) {
            if (entry.getValue() > 1) {
                totalReusableMethods++;
            }
        }

        // Debugging output
        System.out.println("Total reusable methods: " + totalReusableMethods);

        return totalMethods > 0 ? (double) totalReusableMethods / totalMethods : 0.0;
    }
            
                private double calculateODR(File file) {
                    int totalDependencies = 0;
                    int totalObjects = 0;
            
                    Set<String> objectSet = new HashSet<>();
            
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Analyze each line of the file
                            line = line.trim();
                            // Check for method invocations (potential dependencies), excluding System.out.println and main method
                            if (line.contains("(") && line.contains(")") && !line.contains("System.out.println") && !line.contains("main(")) {
                                // Extract method name from the line
                                String methodName = line.substring(0, line.indexOf("(")).trim();
                                // Exclude method declarations and imports
                                if (!methodName.contains("class") && !methodName.contains("import")) {
                                    // Split the line to extract object name (if any)
                                    String[] parts = line.split("\\.");
                                    if (parts.length > 1) {
                                        // Extract object name from the method invocation
                                        String objectName = parts[0].trim();
                                        // Add object name to the set
                                        objectSet.add(objectName);
                                        // Increment the total dependencies
                                        totalDependencies++;
                                    }
                                }
                            }
            
                            // Check for class declarations
                            if (line.startsWith("class") || line.startsWith("interface")) {
                                // Extract class name from the line
                                String[] parts = line.split("\\s+");
                                if (parts.length > 1) {
                                    // Extract class name
                                    String className = parts[1].trim();
                                    objectSet.add(className);
                                    totalObjects++;
            
                                    // Check for extends
                                    if (line.contains("extends")) {
                                        // Split the line to extract the superclass name
                                        String[] extendsParts = line.split("extends");
                                        if (extendsParts.length > 1) {
                                            // Extract superclass name
                                            String superclassName = extendsParts[1].trim();
                                            // Add superclass name to the set
                                            objectSet.add(superclassName);
                                            // Increment the total dependencies
                                            totalDependencies++;
                                        }
                                    }
                                }
                            }
            
                            // Check for method overriding
                            if (line.startsWith("@Override")) {
                                // Check if the line is a method annotation for overriding
                                // Extract the method name from the next line
                                String nextLine = reader.readLine().trim();
                                if (nextLine.startsWith("public") || nextLine.startsWith("protected") || nextLine.startsWith("private")) {
                                    // Extract method name
                                    String[] methodParts = nextLine.split("\\s+");
                                    if (methodParts.length > 2) {
                                        // Extract method name from the method declaration
                                        String methodName = methodParts[2].substring(0, methodParts[2].indexOf("("));
                                        // Add method name to the set
                                        objectSet.add(methodName);
                                        // Increment the total dependencies
                                        totalDependencies++;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    totalObjects = objectSet.size();
                    return totalObjects > 0 ? (double) totalDependencies / totalObjects : 0.0;
                }
            
                private double calculateRMI(File file) {
                    double readabilityScore = calculateReadabilityScore(file);
                    double maintainabilityScore = calculateMaintainabilityScore(file);
            
                    return (readabilityScore + maintainabilityScore) / 2; // Average of both scores
                }
            
                private double calculateReadabilityScore(File file) {
                    double commentsScore = calculateCommentsScore(file);
                    double structureScore = calculateStructureScore(file);
            
                    // Calculate the average readability score based on the individual criteria
                    return (commentsScore + structureScore) / 2;
                }
            
                private double calculateCommentsScore(File file) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        double score = 100.0; // Initialize with the highest score
                        while ((line = reader.readLine()) != null) {
                            // Check for comments
                            if (line.matches("^\\s*//.*") || line.matches("^\\s*/\\*.*\\*/\\s*(//.*)?$")) {
                                score += 2.0; // Increase score for each comment line
                            }
                        }
                        reader.close();
                        return score;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0.0; // Return 0.0 in case of an error
                    }
                }
            
                private double calculateStructureScore(File file) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        double score = 100.0; // Initialize with the highest score
                        int openBraces = 0;
                        int closeBraces = 0;
                        while ((line = reader.readLine()) != null) {
                            // Count opening and closing braces
                            openBraces += line.length() - line.replace("{", "").length();
                            closeBraces += line.length() - line.replace("}", "").length();
                        }
                        reader.close();
                        // Check if the number of opening and closing braces matches
                        if (openBraces == closeBraces) {
                            return score;
                        } else {
                            // Penalize for mismatched braces
                            return score - 5.0;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0.0; // Return 0.0 in case of an error
                    }
                }
            
                private double calculateMaintainabilityScore(File file) {
                    double encapsulationScore = calculateEncapsulationScore(file);
                    double duplicationScore = calculateDuplicationScore(file);
            
                    // Calculate the average maintainability score based on the individual aspects
                    return (encapsulationScore + duplicationScore) / 2;
                }
            
                private double calculateEncapsulationScore(File file) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        boolean insideClass = false;
                        int accessModifierCount = 0;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("class ")) {
                                insideClass = true;
                            }
                            if (insideClass && (line.startsWith("private") || line.startsWith("protected") || line.startsWith("public"))) {
                                accessModifierCount++;
                            }
                        }
                        // Assuming higher access modifier count indicates better encapsulation
                        return accessModifierCount * 10.0;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0.0; // Return 0.0 in case of an error
                    }
                }
            
                private double calculateDuplicationScore(File file) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        Set<String> uniqueLines = new HashSet<>();
                        Set<String> duplicatedLines = new HashSet<>();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Normalize the line to ignore differences like whitespace and case
                            String normalizedLine = line.trim().toLowerCase();
                            if (!normalizedLine.isEmpty()) {
                                if (!uniqueLines.add(normalizedLine)) {
                                    // The line is already present, indicating duplication
                                    duplicatedLines.add(normalizedLine);
                                }
                            }
                        }
                        // Calculate the percentage of duplicated lines
                        double percentage = (double) duplicatedLines.size() / uniqueLines.size() * 100;
                        // Adjust the scoring logic as per your requirements
                        if (percentage >= 20) {
                            return 70.0;
                        } else if (percentage >= 10) {
                            return 80.0;
                        } else {
                            return 90.0;
                        }
                    } catch (IOException e) {
                        // Print the stack trace
                        e.printStackTrace();
                        // Display an error message
                        JOptionPane.showMessageDialog(this,
                                "An error occurred while reading the file.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        // Return a default value
                        return 0.0;
                    }
                }
                
            
        
            public static void main(String[] args) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MetricCalculator();
                    }
                });
            }
        }
        

                // Check if exiting
