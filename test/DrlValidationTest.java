import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.internal.io.ResourceFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Test class to validate DRL syntax locally before loading rules into the database.
 * This helps catch Drools compilation errors before they cause issues in the running application.
 */
public class DrlValidationTest {

    private static final KieServices kieServices = KieServices.Factory.get();

    public static void main(String[] args) {
        System.out.println("=== DRL Validation Test ===\n");
        
        // Test the current rules in the database
        testCurrentRules();
        
        // Test some problematic rules that were causing issues
        testProblematicRules();
        
        System.out.println("\n=== Validation Complete ===");
    }

    private static void testCurrentRules() {
        System.out.println("Testing current rules from database:");
        
        // Rule 1: SimpleExactNameMatch
        String rule1 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            import com.mdm.botcore.service.MergeService.MergeSuggestion;
            
            rule "SimpleExactNameMatch"
                salience 100
                when
                    $entity1 : MDMEntity(name != null)
                    $entity2 : MDMEntity(name != null, this != $entity1)
                    eval($entity1.name.equalsIgnoreCase($entity2.name))
                then
                    System.out.println("Rule 'SimpleExactNameMatch' fired for " + $entity1.getName() + " and " + $entity2.getName());
                    MergeSuggestion suggestion = new MergeSuggestion($entity1, $entity2, "SimpleExactNameMatch", 
                        "Names match exactly: " + $entity1.getName(), 
                        "{\\"mergedName\\":\\"" + $entity1.getName() + "\\"}");
                    insert(suggestion);
            end
            """;
        
        validateRule("SimpleExactNameMatch", rule1);
        
        // Rule 2: AddressBasedMatch
        String rule2 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            import com.mdm.botcore.service.MergeService.MergeSuggestion;
            
            rule "AddressBasedMatch"
                salience 90
                when
                    $entity1 : MDMEntity(address != null)
                    $entity2 : MDMEntity(address != null, this != $entity1)
                    eval($entity1.address.equalsIgnoreCase($entity2.address))
                then
                    System.out.println("Rule 'AddressBasedMatch' fired for " + $entity1.getName() + " and " + $entity2.getName());
                    MergeSuggestion suggestion = new MergeSuggestion($entity1, $entity2, "AddressBasedMatch", 
                        "Addresses match: " + $entity1.getAddress(), 
                        "{\\"mergedAddress\\":\\"" + $entity1.getAddress() + "\\"}");
                    insert(suggestion);
            end
            """;
        
        validateRule("AddressBasedMatch", rule2);
        
        // Rule 3: EmailDomainMatchTest
        String rule3 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            
            rule "EmailDomainMatchTest"
                salience 70
                when
                    $entity1 : MDMEntity(email != null)
                    $entity2 : MDMEntity(email != null, this != $entity1)
                then
                    System.out.println("Email1: " + $entity1.getEmail() + ", Email2: " + $entity2.getEmail());
            end
            """;
        
        validateRule("EmailDomainMatchTest", rule3);
    }

    private static void testProblematicRules() {
        System.out.println("\nTesting problematic rules (should fail):");
        
        // Problematic Rule 1: Complex boolean expression in when clause
        String problematicRule1 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            
            rule "ProblematicRule1"
                salience 100
                when
                    $entity1 : MDMEntity(type == "Organization", name != null)
                    $entity2 : MDMEntity(type == "Organization", name != null, this != $entity1, 
                        name.equalsIgnoreCase($entity1.name) && name.length() > 5)
                then
                    System.out.println("Problematic rule fired");
            end
            """;
        
        validateRule("ProblematicRule1", problematicRule1);
        
        // Problematic Rule 2: Chained method calls in when clause
        String problematicRule2 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            
            rule "ProblematicRule2"
                salience 100
                when
                    $entity1 : MDMEntity(phone != null)
                    $entity2 : MDMEntity(phone != null, this != $entity1)
                    eval($entity1.phone.replaceAll("[^0-9]", "").equals($entity2.phone.replaceAll("[^0-9]", "")))
                then
                    System.out.println("Problematic rule fired");
            end
            """;
        
        validateRule("ProblematicRule2", problematicRule2);
        
        // Problematic Rule 3: Undefined function
        String problematicRule3 = """
            package com.mdm.rules
            
            import com.mdm.botcore.domain.model.MDMEntity;
            
            rule "ProblematicRule3"
                salience 100
                when
                    $entity1 : MDMEntity(name != null)
                    $entity2 : MDMEntity(name != null, this != $entity1)
                    eval(calculateSimilarity($entity1.name, $entity2.name) > 0.8)
                then
                    System.out.println("Problematic rule fired");
            end
            """;
        
        validateRule("ProblematicRule3", problematicRule3);
    }

    private static void validateRule(String ruleName, String drlContent) {
        System.out.println("Validating: " + ruleName);
        
        try {
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write(ResourceFactory.newByteArrayResource(drlContent.getBytes())
                    .setTargetPath("src/main/resources/rules/" + ruleName + ".drl"));
            
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
                    .setTargetPath("src/main/resources/rules/" + ruleName + ".drl"));
            
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