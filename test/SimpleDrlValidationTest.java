import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.internal.io.ResourceFactory;

/**
 * Simple DRL validation test that focuses on syntax validation without requiring domain classes.
 */
public class SimpleDrlValidationTest {

    private static final KieServices kieServices = KieServices.Factory.get();

    public static void main(String[] args) {
        System.out.println("=== Simple DRL Validation Test ===\n");
        
        // Test basic DRL syntax
        testBasicDrlSyntax();
        
        // Test problematic patterns
        testProblematicPatterns();
        
        System.out.println("\n=== Validation Complete ===");
    }

    private static void testBasicDrlSyntax() {
        System.out.println("Testing basic DRL syntax:");
        
        // Simple rule with basic syntax
        String simpleRule = """
            package com.mdm.rules
            
            rule "SimpleRule"
                salience 100
                when
                    $entity1 : String(this != null)
                    $entity2 : String(this != null, this != $entity1)
                then
                    System.out.println("Simple rule fired");
            end
            """;
        
        validateRule("SimpleRule", simpleRule);
        
        // Rule with eval
        String evalRule = """
            package com.mdm.rules
            
            rule "EvalRule"
                salience 100
                when
                    $entity1 : String(this != null)
                    $entity2 : String(this != null, this != $entity1)
                    eval($entity1.equals($entity2))
                then
                    System.out.println("Eval rule fired");
            end
            """;
        
        validateRule("EvalRule", evalRule);
    }

    private static void testProblematicPatterns() {
        System.out.println("\nTesting problematic patterns (should fail):");
        
        // Problematic: Complex boolean expression in when clause
        String complexBooleanRule = """
            package com.mdm.rules
            
            rule "ComplexBooleanRule"
                salience 100
                when
                    $entity1 : String(this != null)
                    $entity2 : String(this != null, this != $entity1, 
                        this.equals($entity1) && this.length() > 5)
                then
                    System.out.println("Complex boolean rule fired");
            end
            """;
        
        validateRule("ComplexBooleanRule", complexBooleanRule);
        
        // Problematic: Chained method calls in when clause
        String chainedMethodRule = """
            package com.mdm.rules
            
            rule "ChainedMethodRule"
                salience 100
                when
                    $entity1 : String(this != null)
                    $entity2 : String(this != null, this != $entity1)
                    eval($entity1.replaceAll("a", "b").equals($entity2.replaceAll("a", "b")))
                then
                    System.out.println("Chained method rule fired");
            end
            """;
        
        validateRule("ChainedMethodRule", chainedMethodRule);
        
        // Problematic: Undefined function
        String undefinedFunctionRule = """
            package com.mdm.rules
            
            rule "UndefinedFunctionRule"
                salience 100
                when
                    $entity1 : String(this != null)
                    $entity2 : String(this != null, this != $entity1)
                    eval(undefinedFunction($entity1, $entity2) > 0.8)
                then
                    System.out.println("Undefined function rule fired");
            end
            """;
        
        validateRule("UndefinedFunctionRule", undefinedFunctionRule);
    }

    private static void validateRule(String ruleName, String drlContent) {
        System.out.println("Validating: " + ruleName);
        
        try {
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write(ResourceFactory.newByteArrayResource(drlContent.getBytes())
                    .setTargetPath("src/main/resources/com/mdm/rules/" + ruleName + ".drl"));
            
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            
            Results results = kieBuilder.getResults();
            
            if (results.hasMessages(Message.Level.ERROR)) {
                System.out.println("❌ FAILED: " + ruleName);
                results.getMessages(Message.Level.ERROR).forEach(message -> 
                    System.out.println("   Error: " + message.getText()));
            } else {
                System.out.println("✅ PASSED: " + ruleName);
            }
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + ruleName + " - Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }

    /**
     * Test a specific DRL rule string
     */
    public static boolean testDrlRule(String ruleName, String drlContent) {
        try {
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write(ResourceFactory.newByteArrayResource(drlContent.getBytes())
                    .setTargetPath("src/main/resources/com/mdm/rules/" + ruleName + ".drl"));
            
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            
            Results results = kieBuilder.getResults();
            
            if (results.hasMessages(Message.Level.ERROR)) {
                System.out.println("❌ Rule validation failed: " + ruleName);
                results.getMessages(Message.Level.ERROR).forEach(message -> 
                    System.out.println("   Error: " + message.getText()));
                return false;
            } else {
                System.out.println("✅ Rule validation passed: " + ruleName);
                return true;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Rule validation failed: " + ruleName + " - Exception: " + e.getMessage());
            return false;
        }
    }
} 