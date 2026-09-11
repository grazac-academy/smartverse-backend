# HANDOFF.md

## What's Done
- **Sizing Calculation Engine**: Created modules (`LoadCalculator`, `EnergyCalculator`, `SurgeCalculator`, `InverterCalculator`, `BatteryCalculator`, `SolarCalculator`) to size solar panels, battery capacity (Ah/kWh), and inverter ratings.
- **Relational DB Schema**: Configured Flyway migrations (`V1` and `V2`) seeding appliances, categories, inverter specifications, and calculation settings.
- **Unified Rest Controllers**: Created public routes for categories/appliances, computation endpoints, and basic authentication secure endpoints for admins.
- **Global Error Handler**: MapStruct-like custom mapping and global exception translation for resource validation and constraints.
- **Test Coverage**: Added a 17-test suite verifying calculators and MockMvc security.

---

## What's Pushed vs. Not Yet Pushed
- **Pushed to `origin/main`**: Base setup (entities, initial Flyway structure, raw battery calculation stubs).
- **Not Yet Pushed (Local Changes)**: Full multi-calculator engine, Flyway V2 seed data, OpenAPI configurations, Security configuration, DTOs, Controllers, and full test suite.

---

## Work Package A: Calculation Querying & Config Administration
*Owner: Teammate A*
- [ ] Create `GET /api/v1/calculations/{id}` in `CalculationController` to fetch archived reports.
- [ ] Create administrative config endpoints (`POST`, `PUT`, `GET`) under `/api/v1/admin/configs` in `AdminController`.
- [ ] Implement service validation to ensure that at least one `CalculationConfiguration` is active.
- [ ] Write integration and routing tests for configuration CRUD routes.
- [ ] Register config schemas in Swagger UI.

**Primary Files**: `CalculationController.java`, `AdminController.java`, `CalculationService.java`, `CalculationServiceImpl.java`, `ControllerTests.java`.  
**Dependencies**: Wait for Teammate B's security configuration tests to be fully merged.

---

## Work Package B: Appliance Catalog Lifecycle & Status Control
*Owner: Teammate B*
- [ ] Create category administration endpoints (`POST`, `PUT`, `DELETE`) under `/api/v1/admin/categories`.
- [ ] Implement soft delete/archiving rules for appliances to prevent database constraint failures.
- [ ] Write MockMvc test assertions for `PUT /api/v1/admin/appliances/{id}` and `PATCH /api/v1/admin/appliances/{id}/status`.
- [ ] Build constraint validations to prevent registering duplicate category/appliance codes.

**Primary Files**: `AdminController.java`, `ApplianceService.java`, `ApplianceServiceImpl.java`, `ControllerTests.java`.  
**Dependencies**: None. Can start immediately.

---

## How to Get Started
1. Run cleanup commands to untrack IDE and build directories:
   ```bash
   git rm -r --cached target
   git rm -r --cached .idea
   git commit -m "chore: untrack build and IDE settings files from version control"
   ```
2. Pull latest main:
   ```bash
   git pull origin main
   ```
3. Check out a branch:
   ```bash
   git checkout -b feat/sizing-config-admin # Teammate A
   git checkout -b feat/appliance-lifecycle  # Teammate B
   ```
4. Verify tests run:
   ```bash
   mvn clean test -o
   ```
5. Submit PR when checklist is complete.

---

## Don't Touch Yet
- `src/main/resources/application.yml` (database variables are in flux).
- `pom.xml` (dependency versions are locked; check in before changing).
