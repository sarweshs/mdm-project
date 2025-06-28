package com.mdm.botcore.domain.model;

import java.util.Map;
import java.util.Objects;

/**
 * A generic representation of an entity within the MDM system.
 * This class serves as a "Fact" for the Drools engine.
 * It uses a Map to hold dynamic attributes, allowing flexibility for various entity types.
 *
 * NOTE: For Drools to work effectively with dynamic properties, you might need to
 * use a different approach (e.g., MVEL expressions directly on a Map, or a more
 * structured hierarchy of specific entity classes). For simplicity, we start
 * with common fields and a map.
 * Rules might directly access properties like `name` or `type`, or use Map access.
 */
public class MDMEntity {
    private String id;
    private String type; // e.g., "Organization", "Person"
    private String name; // e.g., Company Name, Person Name
    private String address;
    private String email;
    private String phone;
    private String sourceSystem; // e.g., "CRM", "ERP"
    private Map<String, Object> attributes; // For additional, dynamic attributes

    // Fields to be populated by Drools rules to indicate a merge suggestion
    private boolean mergeCandidate = false;
    private String mergeReason;
    private String proposedMergedEntityJson; // JSON representation of the proposed merged entity

    // Constructors
    public MDMEntity() {}

    public MDMEntity(String id, String type, String name, String address, String email, String phone, String sourceSystem, Map<String, Object> attributes) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.sourceSystem = sourceSystem;
        this.attributes = attributes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public boolean isMergeCandidate() {
        return mergeCandidate;
    }

    public void setMergeCandidate(boolean mergeCandidate) {
        this.mergeCandidate = mergeCandidate;
    }

    public String getMergeReason() {
        return mergeReason;
    }

    public void setMergeReason(String mergeReason) {
        this.mergeReason = mergeReason;
    }

    public String getProposedMergedEntityJson() {
        return proposedMergedEntityJson;
    }

    public void setProposedMergedEntityJson(String proposedMergedEntityJson) {
        this.proposedMergedEntityJson = proposedMergedEntityJson;
    }

    // Utility to get a specific attribute from the map
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MDMEntity mdmEntity = (MDMEntity) o;
        return Objects.equals(id, mdmEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MDMEntity{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", sourceSystem='" + sourceSystem + '\'' +
                '}';
    }
}