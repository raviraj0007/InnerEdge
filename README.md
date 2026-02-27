# InnerEdge (Android)

InnerEdge is now an Android-only Jetpack Compose app using:

- Clean Architecture (data/domain/presentation/ui)
- MVVM with `StateFlow`
- Room for local persistence
- Hilt for dependency injection
- Navigation Compose for screen flow

## Run

```bash
./gradlew :app:assembleDebug
```

## Test

```bash
./gradlew :app:testDebugUnitTest
```
