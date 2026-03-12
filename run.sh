#!/bin/bash

echo "🐌 Gary Assistant - Starting..."
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 25"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java --version | head -n 1 | awk '{print $2}')
echo "✅ Java version: $JAVA_VERSION"
echo ""

# Build the application
echo "🔨 Building the application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Run the application
echo "🚀 Starting Gary Assistant..."
echo "📊 API Documentation: http://localhost:8080/swagger-ui.html"
echo "🏥 Health Check: http://localhost:8080/api/v1/health"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=dev
