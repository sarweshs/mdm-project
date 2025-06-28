# Rule Engine Migration Guide

## Default Rule Engine

- **Easy Rules** is now the default rule engine for the MDM Bot Core Service.
- To use Drools or RuleBook, set the environment variable or property `rule.engine`:
  - `rule.engine=drools` for Drools
  - `rule.engine=rulebook` for RuleBook (Java implementation)
  - `rule.engine=easyrules` or leave unset for Easy Rules (default)

## How to Switch

- Example (Linux/macOS):
  - `export RULE_ENGINE=drools && mvn spring-boot:run`
  - `export RULE_ENGINE=rulebook && mvn spring-boot:run`
  - `export RULE_ENGINE=easyrules && mvn spring-boot:run` (or leave unset)

## Notes
- Drools and RuleBook code is still present and can be used for advanced or legacy rules.
- Easy Rules is recommended for most use cases due to its simplicity and maintainability.

## Overview

The system now supports two rule engines:
- **Drools**: Traditional DRL-based rules (default)
- **RuleBook**: Java-based rules with more flexibility

## Architecture

### RuleEngine Interface
```java
public interface RuleEngine {
    List<MergeService.MergeSuggestion> processEntities(List<MDMEntity> entities, List<String> rules);
}
```

### Implementations
- `DroolsRuleEngine`: Uses Drools for DRL rule processing
- `RuleBookRuleEngine`: Uses RuleBook for Java-based rule processing

## Configuration

### Environment Variable
Set the `rule.engine` property to choose the engine:

```bash
# Use Drools (default)
export RULE_ENGINE=drools

# Use RuleBook
export RULE_ENGINE=rulebook
```

### Application Properties
Add to `application.properties`:
```properties
# Use Drools
rule.engine=drools

# Use RuleBook
rule.engine=rulebook
```

## Rule Translation

### Drools to RuleBook Translation

#### 1. Exact Company Name Match

**Drools (DRL):**
```drl
rule "ExactCompanyNameMatch"
    salience 100
    when
        $entity1 : MDMEntity(type == "Organization", name != null)
        $entity2 : MDMEntity(type == "Organization", name != null, this != $entity1, name.equalsIgnoreCase($entity1.name))
    then
        // High confidence match - exact name match
end
```

**RuleBook (Java):**
```java
private List<MergeService.MergeSuggestion> executeExactCompanyNameMatch(NameValueReferableMap<MDMEntity> facts) {
    List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
    List<MDMEntity> entities = facts.getValue("entities");
    
    List<MDMEntity> organizations = entities.stream()
        .filter(e -> "Organization".equals(e.getType()) && e.getName() != null)
        .collect(Collectors.toList());
        
    for (int i = 0; i < organizations.size(); i++) {
        for (int j = i + 1; j < organizations.size(); j++) {
            MDMEntity entity1 = organizations.get(i);
            MDMEntity entity2 = organizations.get(j);
            
            if (entity1.getName().equalsIgnoreCase(entity2.getName())) {
                // Create merge suggestion
                String reasoning = "Company names match exactly: " + entity1.getName();
                String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                
                MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                    entity1, entity2, "ExactCompanyNameMatch", reasoning, mergedJson
                );
                suggestions.add(suggestion);
            }
        }
    }
    
    return suggestions;
}
```

#### 2. Phone Number Match

**Drools (DRL):**
```drl
rule "PhoneNumberMatch"
    salience 85
    when
        $entity1 : MDMEntity(phone != null, phone.length() >= 10)
        $entity2 : MDMEntity(phone != null, this != $entity1, phone.replaceAll("[^0-9]", "").equals($entity1.phone.replaceAll("[^0-9]", "")))
    then
        // Phone number matching with normalization
end
```

**RuleBook (Java):**
```java
private List<MergeService.MergeSuggestion> executePhoneNumberMatch(NameValueReferableMap<MDMEntity> facts) {
    List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
    List<MDMEntity> entities = facts.getValue("entities");
    
    List<MDMEntity> entitiesWithPhone = entities.stream()
        .filter(e -> e.getPhone() != null && e.getPhone().length() >= 10)
        .collect(Collectors.toList());
        
    for (int i = 0; i < entitiesWithPhone.size(); i++) {
        for (int j = i + 1; j < entitiesWithPhone.size(); j++) {
            MDMEntity entity1 = entitiesWithPhone.get(i);
            MDMEntity entity2 = entitiesWithPhone.get(j);
            
            String phone1 = entity1.getPhone().replaceAll("[^0-9]", "");
            String phone2 = entity2.getPhone().replaceAll("[^0-9]", "");
            
            if (phone1.equals(phone2)) {
                // Create merge suggestion
                String reasoning = "Phone numbers match: " + entity1.getPhone() + " = " + entity2.getPhone();
                String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                
                MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                    entity1, entity2, "PhoneNumberMatch", reasoning, mergedJson
                );
                suggestions.add(suggestion);
            }
        }
    }
    
    return suggestions;
}
```

## Running the System

### 1. Start Services
```bash
# Terminal 1: Global Rules Service
cd mdm-global-rules
mvn spring-boot:run

# Terminal 2: Bot Core Service
cd mdm-bot-core
mvn spring-boot:run

# Terminal 3: Review Dashboard (optional)
cd mdm-review-dashboard
mvn spring-boot:run
```

### 2. Test with Drools (Default)
```bash
# Test with sample entities
./test/test-rulebook.sh
```

### 3. Test with RuleBook
```bash
# Set environment variable
export RULE_ENGINE=rulebook

# Restart bot core service
cd mdm-bot-core
mvn spring-boot:run

# Test with sample entities
./test/test-rulebook.sh
```

## Advantages of Each Engine

### Drools
- **Pros:**
  - Declarative rule syntax
  - Built-in conflict resolution
  - Mature and well-documented
  - Good for complex business rules
- **Cons:**
  - Limited expression support in newer versions
  - DRL syntax can be complex
  - Compilation errors with complex expressions

### RuleBook
- **Pros:**
  - Full Java power and flexibility
  - Easy to debug and test
  - Can use any Java library
  - Better IDE support
  - No compilation issues
- **Cons:**
  - More verbose than DRL
  - Requires Java knowledge
  - Less declarative

## Adding New Rules

### For Drools
1. Create DRL rule in the global rules service
2. Ensure syntax is compatible with Drools 8.x
3. Avoid complex expressions in `when` clause

### For RuleBook
1. Add new method in `RuleBookRuleEngine`
2. Implement the rule logic in Java
3. Add the method call to `processEntities`

Example:
```java
private List<MergeService.MergeSuggestion> executeCustomRule(NameValueReferableMap<MDMEntity> facts) {
    // Implement your rule logic here
    // Return list of MergeService.MergeSuggestion objects
}
```

## Troubleshooting

### Common Issues

1. **Drools Compilation Errors**
   - Check DRL syntax
   - Avoid complex expressions in `when` clause
   - Use only supported Drools 8.x features

2. **RuleBook Not Working**
   - Ensure `rule.engine=rulebook` is set
   - Check that RuleBook dependency is included
   - Verify Java rule implementation

3. **No Merge Suggestions**
   - Check entity data format
   - Verify rule conditions are met
   - Check logs for errors

### Debugging

Enable debug logging in `application.properties`:
```properties
logging.level.com.mdm.botcore.service=DEBUG
logging.level.org.drools=DEBUG
```

## Migration Path

1. **Start with Drools** (current default)
2. **Identify problematic rules** that don't compile
3. **Switch to RuleBook** for those specific rules
4. **Gradually migrate** complex rules to RuleBook
5. **Use RuleBook as default** once migration is complete

## Testing

Use the provided test script:
```bash
./test/test-rulebook.sh
```

This script tests:
- Exact company name matching
- Phone number matching
- Address matching
- Email domain matching

Check the logs for merge suggestions and any errors. 