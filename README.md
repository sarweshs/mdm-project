# MDM (Master Data Management) Project

A comprehensive Master Data Management system built with Spring Boot, featuring entity merging rules, AI orchestration, and human review workflows.

## Architecture

The system consists of four main microservices:

1. **mdm-global-rules** (Port 8080) - Manages global and company-specific merge rules
2. **mdm-bot-core** (Port 8081) - Core entity merging logic and rule execution
3. **mdm-review-dashboard** (Port 8082) - Web interface for human review of merge candidates
4. **mdm-ai-orchestration** (Port 8083) - AI-powered entity analysis and orchestration

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 16+
- Docker and Docker Compose (for containerized deployment)
- **LLM API Key** (OpenAI or Gemini for AI bot functionality)

## Quick Start

### Option 1: Local Development

1. **Start PostgreSQL Database**
   ```bash
   docker-compose up mdm_postgres_db -d
   ```

2. **Configure LLM API Key (Required for Bot Functionality)**
   
   The AI bot supports multiple LLM providers. Choose one:

   **Option A: OpenAI (Default)**
   - Visit [OpenAI Platform](https://platform.openai.com/api-keys)
   - Create a new API key
   - Edit `mdm-ai-orchestration/src/main/resources/application.properties`
   - Replace `YOUR_OPENAI_API_KEY_HERE` with your actual API key:
     ```properties
     llm.provider=openai
     openai.api.key=your_actual_openai_api_key_here
     ```

   **Option B: Gemini**
   - Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Create a new API key
   - Edit `mdm-ai-orchestration/src/main/resources/application.properties`
   - Replace `YOUR_GEMINI_API_KEY_HERE` with your actual API key:
     ```properties
     llm.provider=gemini
     gemini.api.key=your_actual_gemini_api_key_here
     ```

3. **Start the Services**
   
   Open separate terminal windows for each service:

   ```bash
   # Terminal 1 - Global Rules Service
   cd mdm-global-rules
   mvn spring-boot:run

   # Terminal 2 - Bot Core Service  
   cd mdm-bot-core
   mvn spring-boot:run

   # Terminal 3 - Review Dashboard
   cd mdm-review-dashboard
   mvn spring-boot:run

   # Terminal 4 - AI Orchestration Service
   cd mdm-ai-orchestration
   mvn spring-boot:run
   ```

4. **Access the Application**
   - Review Dashboard: http://localhost:8082
   - Global Rules API: http://localhost:8080
   - Bot Core API: http://localhost:8081
   - AI Orchestration API: http://localhost:8083

### Option 2: Docker Deployment

1. **Configure LLM API Key**
   - Edit `mdm-ai-orchestration/src/main/resources/application.properties`
   - Choose your preferred LLM provider and configure the API key

2. **Build and Run with Docker**
   ```bash
   # Build all services
   ./docker-build.sh
   
   # Start all services
   docker-compose up -d
   ```

3. **Access the Application**
   - Review Dashboard: http://localhost:8082
   - All services will be available on their respective ports

## Features

### 🤖 AI-Powered Bot
- **Multi-Provider Support**: Choose between OpenAI (default) or Gemini
- **Natural Language Processing**: Ask questions like "Show me merge candidates" or "Merge all pending"
- **Smart Actions**: The bot can show candidates, preview merges, approve all, and show audit logs
- **Configurable LLM**: Switch between providers via configuration

### 📊 Review Dashboard
- **Single Screen Layout**: Both dashboard and bot chat in one view with independent scrollbars
- **Real-time Updates**: See merge candidates and interact with the AI bot simultaneously
- **Human Review**: Approve or reject merge candidates with optional comments

### 🔧 Rule Engine
- **Multiple Engines**: Support for Easy Rules (default), Drools, and RuleBook
- **Company-Specific Rules**: Override global rules with company-specific logic
- **Audit Trail**: Complete logging of all rule decisions and reasoning

## Configuration

### Environment Variables
- `LLM_PROVIDER`: Set to "openai" (default) or "gemini"
- `OPENAI_API_KEY`: Your OpenAI API key
- `GEMINI_API_KEY`: Your Gemini API key
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment

### LLM Provider Configuration
```properties
# Choose your LLM provider
llm.provider=openai

# OpenAI Configuration
openai.api.key=your_openai_api_key_here
openai.model=gpt-4o-mini

# Gemini Configuration
gemini.api.key=your_gemini_api_key_here
```

### Database Configuration
The system uses PostgreSQL with the following default settings:
- Database: `mdm_db`
- Username: `mdmuser`
- Password: `mdm_password`
- Port: `5432`

## Troubleshooting

### Bot Communication Errors
If you see "Error communicating with the bot":
1. Ensure the API key is properly configured for your chosen LLM provider
2. Check that the AI Orchestration service is running on port 8083
3. Verify network connectivity between services
4. Check the `llm.provider` setting matches your configured API key

### Service Startup Issues
1. Ensure PostgreSQL is running and accessible
2. Check that all required ports (8080-8083) are available
3. Verify Java 17+ is installed and `JAVA_HOME` is set correctly

## API Endpoints

### Review Dashboard
- `GET /dashboard` - Main dashboard page
- `POST /dashboard/update-status` - Update candidate status

### Bot Core
- `GET /candidates/pending-review` - Get pending merge candidates
- `GET /candidates/{id}` - Get specific candidate details
- `PUT /candidates/{id}/status` - Update candidate status

### AI Orchestration
- `POST /api/bot/chat` - Chat with the AI bot

### Global Rules
- `GET /rules/global` - Get global rules
- `GET /rules/company/{companyId}` - Get company-specific rules

## Development

### Project Structure
```
mdm-project/
├── mdm-global-rules/          # Rule management service
├── mdm-bot-core/             # Core merging logic
├── mdm-review-dashboard/     # Web interface
├── mdm-ai-orchestration/     # AI bot service
├── docker-compose.yml        # Docker configuration
└── README.md                 # This file
```

### Building
```bash
# Build all modules
mvn clean compile

# Run tests
mvn test

# Package JARs
mvn package
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.