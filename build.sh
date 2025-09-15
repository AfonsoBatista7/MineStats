#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}Building MineStats Plugin${NC}"
echo -e "${BLUE}====================================${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}ERROR: Maven is not installed or not in PATH${NC}"
    echo -e "${RED}Please install Maven and try again${NC}"
    exit 1
fi

echo -e "${YELLOW}Cleaning previous builds...${NC}"
mvn clean

if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Maven clean failed${NC}"
    exit 1
fi

echo -e "${YELLOW}Compiling and packaging...${NC}"
mvn package

if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Maven build failed${NC}"
    exit 1
fi

echo -e "${YELLOW}Renaming and moving JAR file...${NC}"
if [ -f "target/PluginStats-0.0.1-SNAPSHOT.jar" ]; then
    cp "target/PluginStats-0.0.1-SNAPSHOT.jar" "./MineStats.jar"
    echo -e "${GREEN}SUCCESS: MineStats.jar created successfully!${NC}"
else
    echo -e "${RED}ERROR: JAR file not found in target directory${NC}"
    echo -e "${YELLOW}Available files in target:${NC}"
    ls -la target/
    exit 1
fi

echo -e "${BLUE}====================================${NC}"
echo -e "${GREEN}Build completed successfully!${NC}"
echo -e "${BLUE}====================================${NC}"