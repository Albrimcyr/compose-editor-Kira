# Kotlin Multiplatform Desktop — Architecture Demo

## Overview
This is a Kotlin Multiplatform (KMP) desktop application built with Compose Multiplatform.

The purpose of this project is to demonstrate architecture rather than feature complexity. 
The structure intentionally mirrors patterns used in large-scale applications.
The application is intentionally over-structured relative to its size.

The application is a chapter-based note editor supporting:

- Create chapter
- Rename chapter
- Delete chapter
- Load chapter content
- Edit and save content (with debouncing)
- HTML content normalization

---

## Architectural Principles

- **MVVM (Model–View–ViewModel)**
- **Single Source of Truth**
- **Unidirectional Data Flow (UDF)**
- **Layered architecture (Presentation / Domain / Data)**
- **UseCase isolation for business logic**
- **Repository abstraction**
- **Reactive state via StateFlow**
- **Sequential command processing inside ViewModel**

The project is intentionally structured to be scalable and testable.

---

## Build & Run (Desktop JVM)

```bash
# macOS / Linux
./gradlew :composeApp:run

# Windows
.\gradlew.bat :composeApp:run
```

---

## Future Extensions

- Replace in-memory repository with persistent storage
- Add unit tests for UseCases
- Introduce integration tests
- Add CI pipeline (GitHub Actions)

## Author
Made by Kyrylo (Kira) in Bergen for demo-purposes, and to ease keeping track of characters, events and lore! 
