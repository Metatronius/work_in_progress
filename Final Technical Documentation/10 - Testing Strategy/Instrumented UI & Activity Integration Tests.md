
The project implements comprehensive Android UI testing leveraging the Espresso framework to validate layout state, lifecycle flows, and cross-activity workflows: 


- **`MainActivity_IntegrationTest`**: Verifies the root task rendering container, validating empty-state logic, checking text layout elements, and ensuring correct interaction states.

- **`AddTask_IntegrationTest`**: Simulates complete data-entry forms by programmatically injecting titles, notes, switches, and radio groups, validating form-submission intents. 

- **`EditTask_IntegrationTest`**: Tests intent data parsing by asserting fields populate correctly from matching extras, and verifies data update cycles upon saving changes. 

- **`CalendarActivityTest`**: Validates the reactive calendar layout mechanics, testing view toggles, checking selection loops, and verifying date-indexed task filter queries.