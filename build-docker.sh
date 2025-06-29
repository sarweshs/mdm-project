#!/bin/bash

echo "Building MDM modules for Docker..."

# Function to build a module
build_module() {
    local module=$1
    echo "Building $module..."
    cd $module
    mvn clean package spring-boot:repackage -DskipTests
    if [ $? -eq 0 ]; then
        echo "✅ $module built successfully"
    else
        echo "❌ Failed to build $module"
        exit 1
    fi
    cd ..
    echo ""
}

# Build modules in dependency order
echo "Step 1: Building mdm-global-rules..."
build_module "mdm-global-rules"

echo "Step 2: Building mdm-bot-core..."
build_module "mdm-bot-core"

echo "Step 3: Building mdm-ai-orchestration..."
build_module "mdm-ai-orchestration"

echo "Step 4: Building mdm-review-dashboard..."
build_module "mdm-review-dashboard"

echo "✅ All modules built successfully!"
echo ""
echo "JAR files created:"
echo "- mdm-global-rules/target/mdm-global-rules-0.0.1-SNAPSHOT.jar"
echo "- mdm-bot-core/target/mdm-bot-core-0.0.1-SNAPSHOT.jar"
echo "- mdm-ai-orchestration/target/mdm-ai-orchestration-0.0.1-SNAPSHOT.jar"
echo "- mdm-review-dashboard/target/mdm-review-dashboard-0.0.1-SNAPSHOT.jar"
echo ""
echo "Ready for Docker build!" 