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

### Ostateczny Test Diagnostyczny (w toku)
- **Problem:** Mimo wszystkich powyższych poprawek, błąd `404 Not Found` wciąż występuje w Cloud Function.
- **Krok:** Sprawdzenie, czy token FCM jest w ogóle ważny, poprzez wysłanie wiadomości testowej bezpośrednio z konsoli Firebase (Messaging -> New campaign -> Send test message).
- **Wynik:** **Test zakończył się sukcesem!** Powiadomienie wysłane ręcznie dotarło na urządzenie.
- **Wniosek:** Problem nie leży po stronie aplikacji klienckiej ani tokena, ale **w środowisku wykonawczym Cloud Function lub w sposobie, w jaki Admin SDK komunikuje się z FCM.**

### Aktualne Działanie (w toku)
- **Problem:** Błąd `404` w Cloud Function.
- **Rozwiązanie:** Stworzenie dodatkowej, ręcznie wywoływanej funkcji HTTP (`testFcm`) w celu całkowitego odizolowania problemu i sprawdzenia, czy `admin.messaging().sendToDevice()` działa w najprostszej formie w danym projekcie.
- **Status:** Jesteśmy w trakcie wdrażania i testowania tej funkcji.

---

## Ostateczne Rozwiązanie Problemu `404 Not Found`

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
