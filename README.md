# MDM Project Setup & Troubleshooting

## Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven
- (Optional) psql client for manual DB checks
- (Optional) jq for JSON formatting

## Database Setup (PostgreSQL via Docker)

The project uses a PostgreSQL database running in a Docker container. The default credentials are:
- **User:** `mdmuser`
- **Password:** `mdm_password`
- **Database:** `mdm_db`

### Start the Database
```sh
docker-compose up -d
```

### If You Get `role "mdmuser" does not exist`
This usually means the Docker volume was created before the correct environment variables were set, or you have a local Postgres server running on the same port.

#### 1. Remove the old Docker volume (WARNING: deletes all DB data!)
```sh
docker-compose down
# Remove the volume (name may vary, check with `docker volume ls`)
docker volume rm mdm-project_mdm_pg_data
```

#### 2. Restart Docker container
```sh
docker-compose up -d
```

#### 3. If you have a local Postgres server running, stop it
```sh
# Stop local Postgres (choose the appropriate command for your setup)
brew services stop postgresql
# OR
pg_ctl -D /usr/local/var/postgres stop
# OR
launchctl unload ~/Library/LaunchAgents/homebrew.mxcl.postgresql.plist
```

#### 4. Test the connection
```sh
PGPASSWORD=mdm_password psql -h localhost -p 5432 -U mdmuser -d mdm_db -c "SELECT version();"
```

## Building the Project

### Build all modules
```sh
mvn clean install
```

### Start the Global Rules Service
```sh
cd mdm-global-rules
mvn spring-boot:run
```

## API Testing

**Note:** Run these commands from the project root directory (`/Users/sarweshsaurabh/personal/next_job/mdm-project`)

### Load Comprehensive Test Data
The project includes comprehensive test data with ReltIO-style entity merging rules:

```bash
# Load all test data at once
./test/load-test-data.sh
```

### Manual API Testing

#### Create Global Rule
```bash
curl -X POST http://localhost:8080/api/global-rules \
     -H "Content-Type: application/json" \
     -d @test/global-rule.json
```

#### Create Company Rule
```bash
curl -X POST http://localhost:8080/api/company-rules \
     -H "Content-Type: application/json" \
     -d @test/company-rule.json
```

#### Create Company Rule Overriding Global
```bash
curl -X POST http://localhost:8080/api/company-rules \
     -H "Content-Type: application/json" \
     -d @test/company-rule-override.json
```

#### Get Effective Rules for Company
```bash
curl http://localhost:8080/api/company-rules/effective/COMPANY_A/lifescience
```

## Test Data Overview

### Global Rules (6 rules)
- **ExactCompanyNameMatch** (Priority: 100) - Exact company name matching
- **AddressBasedMatch** (Priority: 90) - Address-based entity matching
- **PhoneNumberMatch** (Priority: 85) - Phone number matching with normalization
- **EmailDomainMatch** (Priority: 70) - Email domain matching
- **FuzzyNameMatch** (Priority: 60) - Fuzzy name matching with similarity threshold
- **TechCompanyNameMatch** (Priority: 95) - Technology-specific company matching

### Company Rules (4 rules)
- **CompanyASpecificAddressMatch** - Company A address matching with country validation
- **CompanyBPhoneMatch** - Company B phone matching with international format support
- **CompanyCIndustryMatch** - Company C industry-based entity matching
- **CompanyDWebsiteMatch** - Company D website-based entity matching

### Company Overrides (4 rules)
- **ExactCompanyNameMatch** (Priority: 110) - Company A's enhanced exact name matching
- **AddressBasedMatch** (Priority: 105) - Company B's address + postal code matching
- **PhoneNumberMatch** (Priority: 100) - Company C's phone + area code matching
- **FuzzyNameMatch** (Priority: 75) - Company D's fuzzy name + industry matching

## Rule Priority System

The system uses a priority-based rule resolution:
1. **Company Overrides** (highest priority) - Override global rules for specific companies
2. **Company Rules** (medium priority) - Company-specific rules that don't override globals
3. **Global Rules** (base priority) - Default rules for all companies

## Troubleshooting

### Common Issues

1. **Port 5432 already in use**
   - Stop local Postgres: `brew services stop postgresql`
   - Or change Docker port mapping in `docker-compose.yml`

2. **Maven build fails**
   - Ensure Java 17+ is installed: `java -version`
   - Clean and rebuild: `mvn clean install`

3. **Spring Boot parameter mapping error**
   - The `-parameters` compiler flag is already configured in `pom.xml`
   - Rebuild: `mvn clean compile`

4. **Database connection issues**
   - Check if Docker container is running: `docker ps`
   - Verify database is healthy: `docker logs mdm_postgres_db`

### Useful Commands

```bash
# Check running containers
docker ps

# View database logs
docker logs mdm_postgres_db

# Connect to database
PGPASSWORD=mdm_password psql -h localhost -p 5432 -U mdmuser -d mdm_db

# Check application health
curl http://localhost:8080/actuator/health

# View all global rules
curl http://localhost:8080/api/global-rules

# View all company rules
curl http://localhost:8080/api/company-rules
```