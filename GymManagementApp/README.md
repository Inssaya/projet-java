# Gym Management System

This is a complete offline desktop application for managing gym memberships and access, built with **JavaFX** and **SQLite**.

## 📌 Project Overview

The application provides a full-featured system for:
*   **Member Management:** Adding, editing, and renewing member subscriptions.
*   **Access Control:** QR code generation for members and a camera-based QR scanner for check-in.
*   **Data Management:** Local data storage using SQLite.
*   **Reporting:** Revenue and attendance reports with export to Excel and simulated PDF.
*   **Licensing:** Simple PC hardware ID binding for license protection.
*   **Localization:** Multi-language support (English, French, Arabic).

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Backend** | Java | 17+ | Core application logic |
| **Database** | SQLite | JDBC | Local, file-based data storage |
| **UI** | JavaFX | 21.0.1 | Modern desktop user interface (FXML/MVC) |
| **QR Code** | ZXing | 3.5.3 | QR code generation and decoding |
| **Camera** | JavaCV | 1.5.9 | Camera access and frame processing |
| **Export** | Apache POI | 5.2.5 | Excel (.xlsx) report generation |

## ⚙️ Setup Instructions

This project uses **Maven** for dependency management.

### Prerequisites

You must have the following installed:

1.  **Java Development Kit (JDK) 17 or higher:**
    *   Download link: [Oracle JDK] or [Adoptium Temurin]
2.  **Maven:**
    *   Download link: [Apache Maven]
3.  **An IDE with Maven support** (e.g., IntelliJ IDEA, VSCode with Java extensions, NetBeans).

### Step 1: Clone the Repository

\`\`\`bash
git clone [YOUR_REPOSITORY_URL]
cd GymManagementApp
\`\`\`
*(Note: Since this is a file-based delivery, assume the user has the project folder \`GymManagementApp\`)*

### Step 2: Install Dependencies

The \`pom.xml\` file contains all necessary dependencies. Maven will download them automatically.

\`\`\`bash
# Run this command in the GymManagementApp directory
mvn clean install
\`\`\`

### Step ⚠️: JavaCV Native Libraries

JavaCV relies on native libraries (OpenCV). The \`javacv-platform\` dependency in \`pom.xml\` is configured to automatically download the correct native binaries for your operating system. **No manual installation of OpenCV is required.**

## ▶️ How to Run the App

### Option 1: Run from IDE (Recommended for Development)

1.  **Open the Project:** Import the \`GymManagementApp\` folder as a Maven project into your IDE.
2.  **Run the Main Class:** Locate \`src/main/java/com/gym/app/MainApp.java\` and run it directly.

### Option 2: Run from Command Line

1.  **Compile and Package:**
    \`\`\`bash
    mvn clean javafx:run
    \`\`\`

2.  **Run the Bundled JAR (After Packaging)**
    First, create the executable JAR:
    \`\`\`bash
    mvn clean package
    \`\`\`
    The executable JAR will be in the \`target\` directory (e.g., \`target/GymManagementApp-1.0-SNAPSHOT.jar\`).

    To run the JAR, you will need to specify the JavaFX modules on the module path:
    \`\`\`bash
    # Replace [PATH_TO_FX] with the path to your JavaFX SDK lib directory
    java --module-path [PATH_TO_FX] --add-modules javafx.controls,javafx.fxml -jar target/GymManagementApp-1.0-SNAPSHOT.jar
    \`\`\`

## 🐛 Common Errors and Fixes

| Error | Cause | Fix |
| :--- | :--- | :--- |
| **JavaFX runtime components missing** | Missing JavaFX modules on the module path when running the JAR. | Use the \`mvn clean javafx:run\` command, or ensure you include \`--module-path\` and \`--add-modules\` when running the JAR directly (see above). |
| **Camera not detected** | JavaCV/OpenCV failed to initialize the camera (often due to driver issues or camera being in use). | 1. Ensure no other application is using the camera. 2. Try restarting the application. 3. Check your system's camera privacy settings. |
| **SQLite database locked** | Multiple threads or processes are trying to write to the database simultaneously. | This is usually a temporary issue. Restart the application. The application is designed to use a single source for DB connections to minimize this. |
| **ClassNotFound: org.sqlite.JDBC** | The SQLite JDBC driver was not loaded. | Ensure the project was built successfully with \`mvn clean install\` and the \`sqlite-jdbc\` dependency is correctly included in the classpath. |
| **QR not scanning** | Poor lighting, camera focus issues, or the QR code is not visible/clear. | Ensure the QR code is clear, well-lit, and fully visible to the camera feed. |
| **File permissions error** | The application cannot read/write to the database file (\`gym_management.db\`) or the error log (\`error_log.txt\`). | Ensure the user running the application has read/write permissions to the project directory. |
| **License mismatch error** | The application was run on a different PC than the one where it was first initialized. | This is the intended behavior of the license protection system. The application is bound to the first PC's hardware ID. |

## 📝 Error Logging System

The application includes a robust error handling system (\`com.gym.app.util.ErrorLogger\`) that ensures stability and provides clear debugging information:

1.  **Log to File:** Every exception is logged to a file named \`error_log.txt\` in the application's root directory, including a timestamp and full stack trace.
2.  **Console Output:** The full stack trace is printed to the console for immediate developer review.
3.  **User Popup:** A friendly, non-crashing popup is shown to the user with a simple message and a suggestion to check the \`error_log.txt\` file.

---
*This project was built by **Manus AI** as a Senior Full-Stack Java Developer Agent.*
