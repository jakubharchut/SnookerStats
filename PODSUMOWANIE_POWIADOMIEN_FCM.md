# Podsumowanie Implementacji i Diagnostyki Powiadomień FCM

Ten dokument opisuje proces implementacji i rozwiązywania problemów z powiadomieniami push (Firebase Cloud Messaging - FCM) w projekcie SnookerStats.

## Cel
Celem było zaimplementowanie funkcji, która automatycznie wysyła powiadomienie push do użytkownika, gdy otrzyma on nowe zaproszenie do znajomych.

## Implementacja Krok po Kroku

### 1. Stworzenie Usługi Odbierającej Powiadomienia (Android)
- Utworzono plik `MyFirebaseMessagingService.kt`.
- Zaimplementowano w nim logikę odbierania wiadomości (`onMessageReceived`) i wyświetlania prostego powiadomienia systemowego.
- Zarejestrowano usługę w `AndroidManifest.xml`.

### 2. Implementacja Logiki Pobierania i Zapisywania Tokena FCM (Android)
- W `AuthViewModel.kt`, dodano logikę do pobierania unikalnego tokena FCM urządzenia po zalogowaniu.
- W `AuthRepositoryImpl.kt`, zaimplementowano funkcję `updateFcmToken`, która zapisuje pobrany token w dokumencie użytkownika w Firestore.

### 3. Stworzenie Funkcji Cloud Function (Firebase)
- Zainicjowano projekt Cloud Functions w osobnym katalogu (`snookerstats-functions`).
- Napisano funkcję `sendFriendRequestNotification` w `index.js`, która:
  - Uruchamia się (trigger) po każdej aktualizacji dokumentu w kolekcji `users`.
  - Sprawdza, czy do pola `friendRequestsReceived` został dodany nowy identyfikator użytkownika.
  - Pobiera `fcmToken` z dokumentu odbiorcy zaproszenia.
  - Wysyła powiadomienie push za pomocą Firebase Admin SDK.

## Proces Diagnostyczny i Rozwiązane Problemy

Początkowo powiadomienia nie pojawiały się na urządzeniu odbiorcy. Poniżej znajduje się lista zdiagnozowanych problemów i podjętych działań.

### Problem 1: Błędy konfiguracyjne Firebase CLI i Cloud Functions
- **Opis:** Problemy z instalacją `firebase-tools` z powodu braku `npm`, a następnie błędy zależności i uprawnień (`EACCES`).
- **Rozwiązanie:**
  - Instalacja `Node.js` i `npm`.
  - Użycie `sudo` do globalnej instalacji `firebase-tools`.
  - Naprawa uprawnień do katalogu cache `npm` za pomocą `sudo chown`.
  - Rozwiązanie konfliktów zależności `npm` za pomocą `npm install --legacy-peer-deps`.

### Problem 2: Błąd "Blaze Plan Required"
- **Opis:** Wdrożenie funkcji nie powiodło się, ponieważ darmowy plan "Spark" nie obsługuje Cloud Functions.
- **Rozwiązanie:** Przełączenie projektu Firebase na plan "Blaze (pay-as-you-go)".

### Problem 3: Błąd `TypeError: functions.firestore.document is not a function`
- **Opis:** Kod funkcji używał przestarzałego API Cloud Functions (v1).
- **Rozwiązanie:** Przepisanie funkcji na nowszy standard v2, używając `onDocumentUpdated` z `firebase-functions/v2/firestore`.

### Problem 4: Błąd `Permission denied while using the Eventarc Service Agent`
- **Opis:** Przy pierwszym wdrożeniu funkcji v2, Firebase potrzebował czasu na aktywację wewnętrznych usług i propagację uprawnień.
- **Rozwiązanie:** Odczekanie kilku minut i ponowne wdrożenie funkcji.

### Problem 5: Błąd `404 Not Found` podczas wysyłania FCM z Cloud Function
- **Opis:** Funkcja uruchamiała się poprawnie, ale próba wysłania powiadomienia kończyła się błędem HTTP 404.
- **Kroki diagnostyczne:**
  1. **Sprawdzenie uprawnień IAM:** Nadano rolę `Administrator Firebase Cloud Messaging API` dla konta serwisowego `snookerstats-4fb93@appspot.gserviceaccount.com`. **Problem nadal występował.**
  2. **Sprawdzenie aktywacji API:** Potwierdzono, że `Firebase Cloud Messaging API` jest włączone w Google Cloud Console. **Problem nadal występował.**
  3. **Weryfikacja tokena FCM:** Potwierdzono, że token FCM generowany przez aplikację jest identyczny z tym zapisanym w Firestore. **Problem nadal występował.**
  4. **Identyfikacja błędu w setupie testowym:** Użytkownik testował oba konta na tym samym emulatorze, co powodowało, że oba profile miały ten sam token FCM.
  5. **Poprawa setupu testowego:** Uruchomiono dwa osobne emulatory, co poskutkowało wygenerowaniem unikalnych tokenów FCM dla każdego użytkownika. **Problem nadal występował.**
  6. **Reset konfiguracji klienta:** Pobrano najnowszy plik `google-services.json` i podmieniono go w projekcie Android. Wymuszono regenerację tokenów poprzez reinstalację aplikacji. **Problem nadal występował.**

### Problem 6: Brak prośby o zgodę na powiadomienia (Android 13+) - **NAJPRAWDOPODOBNIEJ GŁÓWNA PRZYCZYNA**
- **Opis:** Aplikacja celowała w `targetSdk = 34`, ale nie prosiła użytkownika o zgodę na powiadomienia, co jest wymagane od Androida 13 (API 33). Bez tej zgody system blokuje wszystkie powiadomienia.
- **Rozwiązanie:**
  - Dodano uprawnienie `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` do `AndroidManifest.xml`.
  - Zaimplementowano w `MainScreen.kt` logikę proszącą o zgodę na powiadomienia przy starcie aplikacji za pomocą `rememberLauncherForActivityResult`.
  - Użytkownik ręcznie zweryfikował i włączył uprawnienia w ustawieniach systemowych telefonu.

### Ostateczne Rozwiązanie Problemu `404 Not Found`

Po potwierdzeniu, że token FCM jest poprawny (dzięki udanemu testowi z konsoli Firebase), problem został ostatecznie rozwiązany poprzez zmianę metody wysyłki w Firebase Admin SDK.

- **Problem:** Metoda `admin.messaging().sendToDevice(token, payload)` z niewyjaśnionych przyczyn zwracała błąd `404 Not Found`, sugerując, że token jest nieprawidłowy, mimo że tak nie było. Prawdopodobnie jest to problem środowiskowy w Cloud Functions.

- **Rozwiązanie:** Zastąpiono metodę `sendToDevice()` bardziej fundamentalną metodą `admin.messaging().send(message)`. Wymagało to drobnej zmiany w strukturze kodu:
  - Zamiast przekazywać token jako osobny argument, stał się on polem `token` w głównym obiekcie wiadomości.
  - **Stary kod:** `admin.messaging().sendToDevice(recipientFcmToken, payload)`
  - **Nowy, działający kod:**
    ```javascript
    const message = {
      notification: { ... },
      token: recipientFcmToken,
    };
    admin.messaging().send(message);
    ```

- **Wniosek końcowy:** Metoda `send()` okazała się bardziej niezawodna w środowisku Cloud Functions. Ręczna funkcja testowa (`testFcm`) była kluczowa w potwierdzeniu, że ta zmiana rozwiązuje problem, zanim zaimplementowano ją w głównej logice aplikacji. Ten wzorzec powinien być stosowany przy tworzeniu kolejnych funkcji wysyłających powiadomienia.

---

## Dalsze Ulepszenia Systemu Powiadomień (2025-11-09)

Wprowadzone następujące ulepszenia w obsłudze powiadomień po stronie klienta:

### 1. Niezawodne Odświeżanie Listy Powiadomień

- **Problem:** Po usunięciu powiadomienia lub oznaczeniu go jako przeczytane, lista w UI nie odświeżała się automatycznie, co wymagało ręcznego przeładowania zakładki.
- **Rozwiązanie:** Zastosowano wzorzec **"Ręcznego Odświeżania Stanu po Akcji"**. W `NotificationViewModel.kt`, po każdej operacji modyfikującej (usunięcie, oznaczenie jako przeczytane), jawnie wywoływana jest funkcja `loadNotifications()`, która pobiera najnowszą listę powiadomień z repozytorium. Zapewniono, że subskrypcja do `notificationRepository.getAllNotifications()` jest zawsze aktywna i aktualizuje `_notifications` `MutableStateFlow`.
- **Kluczowy element:** Utworzono `fun loadNotifications()` w `NotificationViewModel`, która subskrybuje do `notificationRepository.getAllNotifications().collect { ... }`. Funkcja ta jest wywoływana po każdej akcji modyfikującej listę powiadomień.

### 2. Poprawne Odczytywanie Statusu `isRead` z Firestore

- **Problem:** Powiadomienia oznaczone jako przeczytane w bazie danych nadal były wyświetlane jako nieprzeczytane w UI.
- **Rozwiązanie:** Zidentyfikowano problem z deserializacją pola `isRead` przez Firebase. W `data class Notification` (plik `Notification.kt`) dodano adnotację `@get:PropertyName("isRead")` do pola `val isRead: Boolean = false`. To zapewnia, że Firebase poprawnie mapuje pole `isRead` z dokumentu Firestore na właściwość `isRead` w obiekcie Kotlin.

### 3. Wizualne Rozróżnienie Powiadomień Odczytanych od Nieodczytanych

- **Problem:** Wszystkie powiadomienia na liście miały ten sam kolor, utrudniając szybkie rozróżnienie ich statusu.
- **Rozwiązanie:** W `NotificationsScreen.kt`, w komponencie `NotificationItem`, dostosowano kolory tła i styl tekstu na podstawie wartości `notification.isRead`:
  - **Nieodczytane:** Kolor tła `MaterialTheme.colorScheme.secondaryContainer` (oryginalny, bardziej wyrazisty), `FontWeight.Bold` dla tytułu.
  - **Odczytane:** Kolor tła `MaterialTheme.colorScheme.surfaceVariant` (neutralny, lekko szarawy), `FontWeight.Normal` dla tytułu.

### 4. Interaktywność Powiadomień o Zaproszeniach do Znajomych

- **Problem:** Powiadomienia były statyczne; kliknięcie nie prowadziło do żadnej akcji.
- **Rozwiązanie:** Zaimplementowano nawigację kontekstową:
  - Nieprzeczytane powiadomienia typu `NotificationType.FRIEND_REQUEST` (Zaproszenie do znajomych) są teraz klikalne.
  - Kliknięcie w takie powiadomienie powoduje nawigację do ekranu `CommunityScreen` z aktywną zakładką "Zaproszenia" (indeks 2).
  - Powiadomienia odczytane są wizualnie "wyłączone" (nieklikalne), a ich jedyną akcją pozostaje usunięcie.
  - Zmodyfikowano `NotificationsScreen.kt` (dodano `navController` i `LaunchedEffect` do obsługi zdarzeń nawigacyjnych) oraz `NotificationViewModel.kt` (dodano `sealed class NavigationEvent` i emitowanie zdarzenia `NavigateToCommunity` z `tabIndex`).
  - Zaktualizowano `MainScreen.kt`, aby poprawnie przekazywać `navController` do `NotificationsScreen` oraz obsługiwać argument `initialTabIndex` w trasie do `CommunityScreen`.
