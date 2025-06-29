#!/bin/bash

# MDM Project Docker Build Script
# This script builds all JAR files and Docker images for the MDM project

echo "🚀 Building MDM Project Docker Images..."

# Build mdm-bot-core first to ensure its JAR is available for others
echo "📦 Building mdm-bot-core JAR with Spring Boot repackage..."
cd mdm-bot-core 
mvn clean install -DskipTests
mvn spring-boot:repackage -DskipTests
cd ..
sleep 1

# Build mdm-global-rules
echo "📦 Building mdm-global-rules JAR with Spring Boot repackage..."
cd mdm-global-rules 
mvn clean package spring-boot:repackage -DskipTests
cd ..
sleep 1

# Build mdm-review-dashboard
echo "📦 Building mdm-review-dashboard JAR with Spring Boot repackage..."
cd mdm-review-dashboard 
mvn clean package spring-boot:repackage -DskipTests
cd ..
sleep 1

# Build mdm-ai-orchestration
echo "📦 Building mdm-ai-orchestration JAR with Spring Boot repackage..."
cd mdm-ai-orchestration 
mvn clean package spring-boot:repackage -DskipTests
cd ..
sleep 1

if [ $? -ne 0 ]; then
    echo "❌ Maven build failed. Please fix the compilation errors first."
    exit 1
fi

echo "✅ JAR files built successfully!"

# Build Docker images
echo "🐳 Building Docker images..."
docker-compose build

if [ $? -ne 0 ]; then
    echo "❌ Docker build failed."
    exit 1
fi

echo "✅ Docker images built successfully!"
echo ""
echo "🎉 Ready to run with: docker-compose up"
echo "📋 Or run in background: docker-compose up -d"
echo "🛑 To stop: docker-compose down" 