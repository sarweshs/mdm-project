#!/bin/bash

# MDM Project Docker Build Script
# This script builds all JAR files and Docker images for the MDM project

echo "🚀 Building MDM Project Docker Images..."

# Build all JAR files first
echo "📦 Building JAR files..."
mvn clean package -DskipTests

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